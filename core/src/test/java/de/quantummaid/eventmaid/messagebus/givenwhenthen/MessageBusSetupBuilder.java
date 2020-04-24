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

package de.quantummaid.eventmaid.messagebus.givenwhenthen;

import de.quantummaid.eventmaid.channel.Channel;
import de.quantummaid.eventmaid.channel.ChannelBuilder;
import de.quantummaid.eventmaid.configuration.AsynchronousConfiguration;
import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.messagebus.MessageBusBuilder;
import de.quantummaid.eventmaid.messagebus.MessageBusType;
import de.quantummaid.eventmaid.messagebus.config.MessageBusTestConfig;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.givenwhenthen.SetupAction;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

import static de.quantummaid.eventmaid.channel.action.Subscription.subscription;
import static de.quantummaid.eventmaid.identification.CorrelationId.newUniqueCorrelationId;
import static de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusTestActions.addARawFilterThatChangesTheContentOfEveryMessage;
import static de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusTestActions.messageBusTestActions;
import static de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusTestProperties.MESSAGE_RECEIVED_BY_ERROR_LISTENER;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironment.emptyTestEnvironment;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.EXPECTED_RESULT;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.RESULT;
import static de.quantummaid.eventmaid.shared.eventtype.TestEventType.testEventType;
import static de.quantummaid.eventmaid.shared.properties.SharedTestProperties.*;
import static de.quantummaid.eventmaid.shared.utils.FilterTestUtils.*;
import static de.quantummaid.eventmaid.shared.utils.SubscriptionTestUtils.*;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusSetupBuilder {
    private final TestEnvironment testEnvironment = emptyTestEnvironment();
    private final List<SetupAction<MessageBus>> setupActions = new LinkedList<>();
    private final MessageBusBuilder messageBusBuilder = MessageBusBuilder.aMessageBus();

    public static MessageBusSetupBuilder aConfiguredMessageBus(final MessageBusTestConfig testConfig) {
        return new MessageBusSetupBuilder()
                .configuredWith(testConfig);
    }

    private MessageBusSetupBuilder configuredWith(final MessageBusTestConfig testConfig) {
        final MessageBusType type = testConfig.getType();
        final AsynchronousConfiguration asynchronousConfiguration = testConfig.getAsynchronousConfiguration();
        messageBusBuilder.forType(type)
                .withAsynchronousConfiguration(asynchronousConfiguration);
        final boolean asynchronous = testConfig.isAsynchronous();
        testEnvironment.setProperty(IS_ASYNCHRONOUS, asynchronous);
        return this;
    }

    public <T> MessageBusSetupBuilder withASubscriberForTyp(final EventType eventType) {
        setupActions.add((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            addASingleSubscriber(testActions, testEnvironment, eventType);
        });
        return this;
    }

    public MessageBusSetupBuilder withACustomChannelFactory() {
        messageBusBuilder.withAChannelFactory((eventType, subscriber, exceptionHandler) -> {
            final Channel<Object> channel = ChannelBuilder.aChannel(Object.class)
                    .withDefaultAction(subscription())
                    .build();
            testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, channel);
            return channel;
        });
        return this;
    }

    public MessageBusSetupBuilder withoutASubscriber() {
        return this;
    }

    public MessageBusSetupBuilder withASingleSubscriber() {
        setupActions.add((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            addASingleSubscriber(testActions, testEnvironment);
        });
        return this;
    }

    public MessageBusSetupBuilder withASingleRawSubscriber() {
        setupActions.add((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            addASingleRawSubscriber(testActions, testEnvironment);
        });
        return this;
    }

    public MessageBusSetupBuilder withARawSubscriberForType(final EventType eventType) {
        setupActions.add((t, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(t);
            addASingleRawSubscriber(testActions, testEnvironment, eventType);
        });
        return this;
    }

    public MessageBusSetupBuilder withASubscriberForACorrelationId() {
        setupActions.add(MessageBusTestActions::addSubscriberForACorrelationId);
        return this;
    }

    public MessageBusSetupBuilder withSeveralSubscriber(final int numberOfSubscribers) {
        setupActions.add((t, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(t);
            addSeveralSubscriber(testActions, testEnvironment, numberOfSubscribers);
        });
        return this;
    }

    public MessageBusSetupBuilder withAFilterThatChangesTheContentOfEveryMessage() {
        setupActions.add((messageBus, testEnvironment1) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            addAFilterThatChangesTheContentOfEveryMessage(testActions, testEnvironment1);
        });
        return this;
    }

    public MessageBusSetupBuilder withAFilterThatDropsMessages() {
        setupActions.add((t, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(t);
            addFilterThatBlocksMessages(testActions, null);
        });
        return this;
    }

    public MessageBusSetupBuilder withAnInvalidFilterThatDoesNotUseAnyFilterMethods() {
        setupActions.add((t, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(t);
            addFilterThatForgetsMessages(testActions, null);
        });
        return this;
    }

    public MessageBusSetupBuilder withTwoFilterOnSpecificPositions() {
        setupActions.add(MessageBusTestActions::addTwoFilterOnSpecificPositions);
        return this;
    }

    public MessageBusSetupBuilder withAFilterAtAnInvalidPosition(final int position) {
        setupActions.add((t, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(t);
            testActions.addNotRawFilter(null, position);
        });
        return this;
    }

    public MessageBusSetupBuilder withAnExceptionThrowingFilter() {
        setupActions.add((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            addFilterThatThrowsException(testActions, null);
        });
        return this;
    }

    public MessageBusSetupBuilder withARawFilterThatChangesCompleteProcessingContext() {
        setupActions.add((t, testEnvironment) -> addARawFilterThatChangesTheContentOfEveryMessage(t));
        return this;
    }

    public MessageBusSetupBuilder withASubscriberThatBlocksWhenAccepting() {
        setupActions.add((messageBus, testEnvironment1) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            addASubscriberThatBlocksWhenAccepting(testActions, testEnvironment1);
        });
        return this;
    }

    public MessageBusSetupBuilder withAnExceptionAcceptingSubscriber() {
        setupActions.add((t, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(t);
            addAnExceptionAcceptingSubscriber(testActions, testEnvironment);
        });
        return this;
    }

    public MessageBusSetupBuilder withAnExceptionThrowingSubscriber() {
        setupActions.add((messageBus, testEnvironment1) -> {
            final EventType eventType = testEnvironment1.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            addAnExceptionThrowingSubscriber(messageBusTestActions(messageBus), testEnvironment1, eventType);
        });
        return this;
    }

    public MessageBusSetupBuilder withACustomExceptionHandler() {
        messageBusBuilder.withExceptionHandler(MessageBusTestExceptionHandler.allExceptionAsResultHandlingTestExceptionHandler(testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withACustomExceptionHandlerMarkingExceptionAsIgnored() {
        messageBusBuilder.withExceptionHandler(MessageBusTestExceptionHandler.testExceptionAllowingExceptionHandler(testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withADynamicExceptionListenerForEventType() {
        messageBusBuilder.withExceptionHandler(MessageBusTestExceptionHandler.allExceptionIgnoringExceptionHandler());
        setupActions.add(MessageBusTestActions::addDynamicErrorListenerForEventType);
        return this;
    }

    public MessageBusSetupBuilder withTwoDynamicExceptionListenerForEventType() {
        messageBusBuilder.withExceptionHandler(MessageBusTestExceptionHandler.allExceptionIgnoringExceptionHandler());
        setupActions.add(MessageBusTestActions::addTwoDynamicErrorListenerForEventType_whereTheFirstWillBeRemoved);
        return this;
    }

    public MessageBusSetupBuilder withADynamicErrorListenerAndAnErrorThrowingExceptionHandler() {
        setupActions.add(MessageBusTestActions::addDynamicErrorListenerForEventType);
        return this;
    }

    public MessageBusSetupBuilder withAnErrorThrowingExceptionHandler() {
        return this;
    }

    public MessageBusSetupBuilder withADynamicCorrelationIdBasedExceptionListener() {
        messageBusBuilder.withExceptionHandler(MessageBusTestExceptionHandler.allExceptionIgnoringExceptionHandler());
        setupActions.add((messageBus, testEnvironment) -> {
            final CorrelationId correlationId = newUniqueCorrelationId();
            testEnvironment.setProperty(EXPECTED_CORRELATION_ID, correlationId);
            final SubscriptionId subscriptionId = messageBus.onException(correlationId, (m, e) -> {
                this.testEnvironment.setPropertyIfNotSet(RESULT, e);
                this.testEnvironment.setPropertyIfNotSet(MESSAGE_RECEIVED_BY_ERROR_LISTENER, m);
            });
            this.testEnvironment.setProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public MessageBusSetupBuilder withTwoDynamicCorrelationBasedExceptionListener() {
        messageBusBuilder.withExceptionHandler(MessageBusTestExceptionHandler.allExceptionIgnoringExceptionHandler());
        setupActions.add((messageBus, testEnvironment) -> {
            final CorrelationId correlationId = CorrelationId.newUniqueCorrelationId();
            testEnvironment.setProperty(EXPECTED_CORRELATION_ID, correlationId);

            final SubscriptionId subscriptionId = messageBus.onException(correlationId, (m, e) -> {
                throw new RuntimeException("Should not be called");
            });
            this.testEnvironment.setProperty(USED_SUBSCRIPTION_ID, subscriptionId);

            messageBus.onException(correlationId, (m, e) -> {
                this.testEnvironment.setProperty(RESULT, e);
                this.testEnvironment.setPropertyIfNotSet(MESSAGE_RECEIVED_BY_ERROR_LISTENER, m);
            });
        });
        return this;
    }

    public MessageBusSetup build() {
        final MessageBus messageBus = messageBusBuilder.build();
        return MessageBusSetup.setup(messageBus, testEnvironment, setupActions);
    }

}
