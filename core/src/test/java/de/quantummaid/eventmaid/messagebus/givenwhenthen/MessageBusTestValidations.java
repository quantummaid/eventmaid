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

import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.identification.MessageId;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.processingcontext.ProcessingContext;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.subscriber.TestSubscriber;
import de.quantummaid.eventmaid.shared.validations.SharedTestValidations;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.RESULT;
import static de.quantummaid.eventmaid.shared.polling.PollingUtils.pollUntilListHasSize;
import static de.quantummaid.eventmaid.shared.properties.SharedTestProperties.*;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
final class MessageBusTestValidations {
    static void assertAmountOfSubscriberForType(final int expectedNumberOfSubscribers,
                                                final EventType eventType,
                                                final TestEnvironment testEnvironment) {
        @SuppressWarnings("unchecked")
        final Map<EventType, List<Subscriber<?>>> resultMap =
                (Map<EventType, List<Subscriber<?>>>) testEnvironment.getProperty(RESULT);
        final List<Subscriber<?>> subscribersForType = resultMap.get(eventType);
        SharedTestValidations.assertListOfSize(subscribersForType, expectedNumberOfSubscribers);
    }

    static void assertTheExceptionHandled(final Class<?> expectedExceptionClass,
                                          final TestEnvironment testEnvironment) {
        SharedTestValidations.assertResultOfClass(testEnvironment, expectedExceptionClass);
        final ProcessingContext<?> processingContext = getReceivedErrorMessage(testEnvironment);
        final Object message = processingContext.getPayload();
        final Object expectedPayload = testEnvironment.getProperty(SINGLE_SEND_MESSAGE);
        SharedTestValidations.assertEquals(message, expectedPayload);
        final MessageId messageId = processingContext.getMessageId();
        final Object expectedMessageId = testEnvironment.getProperty(SEND_MESSAGE_ID);
        SharedTestValidations.assertEquals(messageId, expectedMessageId);
    }

    private static ProcessingContext<?> getReceivedErrorMessage(final TestEnvironment testEnvironment) {
        return testEnvironment.getPropertyAsType(MessageBusTestProperties.MESSAGE_RECEIVED_BY_ERROR_LISTENER, ProcessingContext.class);
    }

    static void assertAllReceiverReceivedProcessingContextWithCorrectCorrelationId(final TestEnvironment testEnvironment) {
        final List<TestSubscriber<ProcessingContext<Object>>> receivers =
                getExpectedReceiverAsCorrelationBasedSubscriberList(testEnvironment);

        for (final TestSubscriber<ProcessingContext<Object>> receiver : receivers) {
            pollUntilListHasSize(receiver::getReceivedMessages, 1);
            final List<ProcessingContext<Object>> receivedMessages = receiver.getReceivedMessages();
            final ProcessingContext<Object> processingContext = receivedMessages.get(0);
            final CorrelationId expectedCorrelationId = getExpectedCorrelationId(testEnvironment);
            SharedTestValidations.assertEquals(processingContext.getCorrelationId(), expectedCorrelationId);
            final Object expectedResult = testEnvironment.getProperty(SINGLE_SEND_MESSAGE);
            SharedTestValidations.assertEquals(processingContext.getPayload(), expectedResult);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<TestSubscriber<ProcessingContext<Object>>> getExpectedReceiverAsCorrelationBasedSubscriberList(
            final TestEnvironment testEnvironment) {
        return (List<TestSubscriber<ProcessingContext<Object>>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
    }

    private static CorrelationId getExpectedCorrelationId(final TestEnvironment testEnvironment) {
        return testEnvironment.getPropertyAsType(EXPECTED_CORRELATION_ID, CorrelationId.class);
    }
}
