/*
 * Copyright (c) 2020 Richard Hauswald - https://quantummaid.de/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.quantummaid.eventmaid.messageFunction.givenWhenThen;

import de.quantummaid.eventmaid.messageFunction.testResponses.SimpleErrorResponse;
import de.quantummaid.eventmaid.messageFunction.testResponses.SimpleTestRequest;
import de.quantummaid.eventmaid.messageFunction.testResponses.SimpleTestResponse;
import de.quantummaid.eventmaid.shared.exceptions.TestException;
import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.messageBus.MessageBus;
import de.quantummaid.eventmaid.messageBus.MessageBusBuilder;
import de.quantummaid.eventmaid.messageFunction.MessageFunction;
import de.quantummaid.eventmaid.messageFunction.MessageFunctionBuilder;
import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static de.quantummaid.eventmaid.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static de.quantummaid.eventmaid.messageBus.MessageBusBuilder.aMessageBus;
import static de.quantummaid.eventmaid.messageBus.MessageBusType.ASYNCHRONOUS;
import static de.quantummaid.eventmaid.messageBus.givenWhenThen.MessageBusTestExceptionHandler.allExceptionIgnoringExceptionHandler;
import static de.quantummaid.eventmaid.processingContext.ProcessingContext.processingContextForPayloadAndError;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.EXPECTED_RESULT;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.MOCK;
import static de.quantummaid.eventmaid.shared.eventType.TestEventType.differentTestEventType;
import static de.quantummaid.eventmaid.shared.eventType.TestEventType.testEventType;
import static de.quantummaid.eventmaid.shared.properties.SharedTestProperties.EVENT_TYPE;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestMessageFunctionSetupBuilder {
    private static final int MB_TEST_POOL_SIZE = 5;
    private final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();
    private final List<Consumer<MessageBus>> setupActions = new LinkedList<>();
    private MessageBusBuilder messageBusBuilder = aMessageBus()
            .forType(ASYNCHRONOUS)
            .withAsynchronousConfiguration(constantPoolSizeAsynchronousConfiguration(MB_TEST_POOL_SIZE));
    private Function<MessageBusBuilder, MessageBus> messageBusCreation = MessageBusBuilder::build;

    public static TestMessageFunctionSetupBuilder aMessageFunction() {
        return new TestMessageFunctionSetupBuilder();
    }

    public TestMessageFunctionSetupBuilder withTheRequestAnsweredByACorrelatedResponse() {
        return answerWith(SimpleTestResponse::testResponse);
    }

    public TestMessageFunctionSetupBuilder withTheRequestAnsweredByANull() {
        return answerWith(request -> null);
    }

    public TestMessageFunctionSetupBuilder withTheRequestAnsweredByAErrorResponse() {
        return answerWithPayloadAndError(request -> null, SimpleErrorResponse::simpleErrorResponse);
    }

    public TestMessageFunctionSetupBuilder withTheRequestAnsweredByANormalAndAErrorResponse() {
        return answerWithPayloadAndError(SimpleTestResponse::testResponse, SimpleErrorResponse::simpleErrorResponse);
    }

    private TestMessageFunctionSetupBuilder answerWith(final Function<Object, Object> responseCreator) {
        setupActions.add(messageBus -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            messageBus.subscribeRaw(eventType, processingContext -> {
                final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
                final SimpleTestRequest request = (SimpleTestRequest) processingContext.getPayload();
                final Object response = responseCreator.apply(request);
                final EventType differentTestEventType = differentTestEventType();
                messageBus.send(differentTestEventType, response, correlationId);
            });
            testEnvironment.setProperty(MessageFunctionTestProperties.NUMBER_OF_INITIAL_SUBSCRIBERS, 1);
        });
        return this;
    }

    private TestMessageFunctionSetupBuilder answerWithPayloadAndError(final Function<Object, Object> responseCreator,
                                                                      final Function<Object, Object> errorResponseCreator) {
        setupActions.add(messageBus -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            messageBus.subscribeRaw(eventType, processingContext -> {
                final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
                final SimpleTestRequest request = (SimpleTestRequest) processingContext.getPayload();
                final Object response = responseCreator.apply(request);
                final Object errorResponse = errorResponseCreator.apply(request);
                final EventType differentTestEventType = differentTestEventType();
                final ProcessingContext<Object> responseProcessingContext =
                        processingContextForPayloadAndError(differentTestEventType, correlationId, response, errorResponse);
                testEnvironment.setPropertyIfNotSet(MessageFunctionTestProperties.RESPONSE_PROCESSING_CONTEXT, responseProcessingContext);
                messageBus.send(responseProcessingContext);
            });
        });
        return this;
    }

    public TestMessageFunctionSetupBuilder acceptingTwoDifferentResponsesForTheTestRequest() {
        return this;
    }

    public TestMessageFunctionSetupBuilder definedWithAnUnansweredResponse() {
        testEnvironment.setProperty(MessageFunctionTestProperties.NUMBER_OF_INITIAL_SUBSCRIBERS, 0);
        return this;
    }

    public TestMessageFunctionSetupBuilder definedWithResponseThrowingAnException() {
        messageBusBuilder.withExceptionHandler(allExceptionIgnoringExceptionHandler());
        setupActions.add(messageBus -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            messageBus.subscribe(eventType, simpleTestRequest -> {
                throw new RuntimeException("Expected exception in subscriber");
            });
            testEnvironment.setProperty(MessageFunctionTestProperties.NUMBER_OF_INITIAL_SUBSCRIBERS, 1);
        });
        return this;
    }

    public TestMessageFunctionSetupBuilder withFulfillingResponseSendTwice() {
        setupActions.add(messageBus -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            messageBus.subscribeRaw(eventType, processingContext -> {
                final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
                final SimpleTestRequest request = (SimpleTestRequest) processingContext.getPayload();
                final SimpleTestResponse simpleTestResponse = SimpleTestResponse.testResponse(request);
                testEnvironment.setProperty(EXPECTED_RESULT, simpleTestResponse);
                final EventType answerEventType = differentTestEventType();
                messageBus.send(answerEventType, simpleTestResponse, correlationId);
                messageBus.send(answerEventType, simpleTestResponse, correlationId);
            });
        });
        return this;
    }

    public TestMessageFunctionSetupBuilder withRequestAnsweredByResponseThenByException() {
        messageBusBuilder.withExceptionHandler(allExceptionIgnoringExceptionHandler());
        setupActions.add(messageBus -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            messageBus.subscribeRaw(eventType, processingContext -> {
                final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
                final SimpleTestRequest request = (SimpleTestRequest) processingContext.getPayload();
                final SimpleTestResponse simpleTestResponse = SimpleTestResponse.testResponse(request);
                final EventType answerEventType = differentTestEventType();
                messageBus.send(answerEventType, simpleTestResponse, correlationId);
                testEnvironment.setProperty(EXPECTED_RESULT, simpleTestResponse);
                throw new TestException();
            });
        });
        return this;
    }

    public TestMessageFunctionSetupBuilder withRequestAnsweredByExceptionThenByMessage() {
        messageBusBuilder.withExceptionHandler(allExceptionIgnoringExceptionHandler());
        setupActions.add(messageBus -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            messageBus.subscribeRaw(eventType, processingContext -> {
                try {
                    throw new TestException();
                } finally {
                    final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
                    final SimpleTestRequest request = (SimpleTestRequest) processingContext.getPayload();
                    final SimpleTestResponse simpleTestResponse = SimpleTestResponse.testResponse(request);

                    final EventType answerEventType = differentTestEventType();
                    messageBus.send(answerEventType, simpleTestResponse, correlationId);
                    testEnvironment.setProperty(EXPECTED_RESULT, simpleTestResponse);
                }
            });
        });
        return this;
    }

    public TestMessageFunctionSetupBuilder throwingAnExceptionDuringSend() {
        messageBusCreation = ignored -> MessageBusMock.createMessageBusMock();
        return this;
    }

    public TestEnvironment getTestEnvironment() {
        return testEnvironment;
    }

    public MessageFunction build() {
        final MessageBus messageBus = messageBusCreation.apply(messageBusBuilder);
        setupActions.forEach(f -> f.accept(messageBus));
        testEnvironment.setProperty(MOCK, messageBus);
        return MessageFunctionBuilder.aMessageFunction(messageBus);
    }

    private static final class MessageBusMock {
        static MessageBus createMessageBusMock() {
            final ClassLoader classLoader = MessageBusMock.class.getClassLoader();
            final Class<?>[] interfaces = {MessageBus.class};
            return (MessageBus) Proxy.newProxyInstance(classLoader, interfaces, (proxy, method, args) -> {
                if (method.getName().equals("send")) {
                    throw new TestException();
                }
                return null;
            });
        }
    }
}
