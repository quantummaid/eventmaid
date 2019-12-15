/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
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

package de.quantummaid.messagemaid.shared.pipeChannelMessageBus;

import de.quantummaid.messagemaid.shared.pipeChannelMessageBus.testActions.CloseActions;
import de.quantummaid.messagemaid.shared.pipeChannelMessageBus.testActions.SubscribeActions;
import de.quantummaid.messagemaid.shared.pipeChannelMessageBus.testActions.TestFilter;
import de.quantummaid.messagemaid.shared.validations.SharedTestValidations;
import de.quantummaid.messagemaid.identification.CorrelationId;
import de.quantummaid.messagemaid.identification.MessageId;
import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.processingContext.ProcessingContext;
import de.quantummaid.messagemaid.shared.environment.TestEnvironment;
import de.quantummaid.messagemaid.shared.polling.PollingUtils;
import de.quantummaid.messagemaid.shared.subscriber.SimpleTestSubscriber;
import de.quantummaid.messagemaid.shared.subscriber.TestSubscriber;
import de.quantummaid.messagemaid.shared.testMessages.TestMessageOfInterest;
import de.quantummaid.messagemaid.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.RESULT;
import static de.quantummaid.messagemaid.shared.polling.PollingUtils.pollUntil;
import static de.quantummaid.messagemaid.shared.polling.PollingUtils.pollUntilEquals;
import static de.quantummaid.messagemaid.shared.properties.SharedTestProperties.*;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeChannelMessageBusSharedTestValidations {

    public static void assertExpectedReceiverReceivedSingleMessage(final TestEnvironment testEnvironment) {
        final List<SimpleTestSubscriber<?>> receivers = getExpectedReceiversAsSubscriber(testEnvironment);
        for (final SimpleTestSubscriber<?> receiver : receivers) {
            final List<?> receivedMessages = receiver.getReceivedMessages();
            pollUntilEquals(receivedMessages::size, 1);
            final Object receivedMessage = receivedMessages.get(0);
            final Object expectedMessage = testEnvironment.getProperty(SINGLE_SEND_MESSAGE);
            SharedTestValidations.assertEquals(receivedMessage, expectedMessage);
        }
    }

    public static void assertExpectedReceiverReceivedAllMessages(final TestEnvironment testEnvironment) {
        final List<?> expectedReceivedMessages = testEnvironment.getPropertyAsType(MESSAGES_SEND, List.class);
        assertExpectedReceiverReceivedAllMessages(testEnvironment, expectedReceivedMessages);
    }

    public static void assertExpectedReceiverReceivedAllMessages(final TestEnvironment testEnvironment,
                                                                 final List<?> expectedMessages) {
        final List<SimpleTestSubscriber<?>> receivers = getExpectedReceiversAsSubscriber(testEnvironment);
        for (final SimpleTestSubscriber<?> receiver : receivers) {
            final List<?> receivedMessages = receiver.getReceivedMessages();
            pollUntilEquals(receivedMessages::size, expectedMessages.size());
            final Object[] ar = expectedMessages.toArray();
            assertThat(receivedMessages, containsInAnyOrder(ar));
        }
    }

    public static void assertExpectedReceiverReceivedMessageWithErrorPayload(final TestEnvironment testEnvironment) {
        final List<SimpleTestSubscriber<?>> receivers = getExpectedReceiversAsSubscriber(testEnvironment);
        for (final SimpleTestSubscriber<?> receiver : receivers) {
            @SuppressWarnings("unchecked")
            final List<ProcessingContext<?>> receivedMessages = (List<ProcessingContext<?>>) receiver.getReceivedMessages();
            pollUntilEquals(receivedMessages::size, 1);
            final ProcessingContext<?> receivedMessage = receivedMessages.get(0);
            final Object errorPayload = receivedMessage.getErrorPayload();
            final Object expectedMessage = testEnvironment.getProperty(SEND_ERROR_PAYLOAD);
            SharedTestValidations.assertEquals(errorPayload, expectedMessage);
        }
    }

    public static void assertSutStillHasExpectedSubscriber(final SubscribeActions subscribeActions,
                                                           final TestEnvironment testEnvironment) {
        final List<Subscriber<?>> expectedSubscriber = getExpectedSubscriber(testEnvironment);
        assertSutStillHasExpectedSubscriber(subscribeActions, expectedSubscriber);
    }

    private static void assertSutStillHasExpectedSubscriber(final SubscribeActions subscribeActions,
                                                            final List<Subscriber<?>> expectedSubscriber) {
        final List<Subscriber<?>> allSubscribers = subscribeActions.getAllSubscribers();
        assertThat(allSubscribers, containsInAnyOrder(expectedSubscriber.toArray()));
    }

    public static void assertResultEqualsCurrentSubscriber(final TestEnvironment testEnvironment) {
        final Object subscriber = testEnvironment.getProperty(RESULT);
        final Object expectedSubscriber = testEnvironment.getProperty(INITIAL_SUBSCRIBER);
        SharedTestValidations.assertEquals(subscriber, expectedSubscriber);
    }

    public static void assertAllMessagesHaveContentChanged(final SubscribeActions subscribeActions,
                                                           final TestEnvironment testEnvironment) {
        final List<?> expectedMessages = (List<?>) testEnvironment.getProperty(MESSAGES_SEND);
        final List<Subscriber<?>> subscribers = subscribeActions.getAllSubscribers();
        final String expectedContent = testEnvironment.getPropertyAsType(EXPECTED_CHANGED_CONTENT, String.class);
        for (final Subscriber<?> subscriber : subscribers) {
            final TestSubscriber<TestMessageOfInterest> testSubscriber = castToTestSubscriber(subscriber);
            final List<TestMessageOfInterest> receivedMessages = testSubscriber.getReceivedMessages();
            PollingUtils.pollUntilEquals(receivedMessages::size, expectedMessages.size());
            for (final TestMessageOfInterest receivedMessage : receivedMessages) {
                assertThat(receivedMessage.getContent(), equalTo(expectedContent));
            }
        }
    }

    public static void assertAllReceivedProcessingContextsWereChanged(final MessageBus messageBus,
                                                                      final TestEnvironment testEnvironment) {
        final List<?> expectedMessages = (List<?>) testEnvironment.getProperty(MESSAGES_SEND);
        final List<Subscriber<?>> subscribers = messageBus.getStatusInformation().getAllSubscribers();
        for (final Subscriber<?> subscriber : subscribers) {
            final TestSubscriber<ProcessingContext<Object>> testSubscriber = castToRawTestSubscriber(subscriber);
            final List<ProcessingContext<Object>> receivedContexts = testSubscriber.getReceivedMessages();
            pollUntilEquals(receivedContexts::size, expectedMessages.size());
            for (final ProcessingContext<Object> processingContext : receivedContexts) {
                assertThat(processingContext.getPayload(), equalTo(TestFilter.CHANGED_CONTENT));
                assertThat(processingContext.getErrorPayload(), equalTo(TestFilter.ADDED_ERROR_CONTENT));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static TestSubscriber<TestMessageOfInterest> castToTestSubscriber(final Subscriber<?> subscriber) {
        return (TestSubscriber<TestMessageOfInterest>) subscriber;
    }

    @SuppressWarnings("unchecked")
    private static TestSubscriber<ProcessingContext<Object>> castToRawTestSubscriber(final Subscriber<?> subscriber) {
        return (TestSubscriber<ProcessingContext<Object>>) subscriber;
    }

    @SuppressWarnings("unchecked")
    public static void assertNumberOfMessagesReceived(final TestEnvironment testEnvironment,
                                                      final int expectedNumberOfDeliveredMessages) {
        final List<TestSubscriber<?>> receiver;
        if (testEnvironment.has(SINGLE_RECEIVER)) {
            final TestSubscriber<?> singleSubscriber = testEnvironment.getPropertyAsType(SINGLE_RECEIVER, TestSubscriber.class);
            receiver = Collections.singletonList(singleSubscriber);
        } else {
            receiver = (List<TestSubscriber<?>>) testEnvironment.getPropertyAsType(EXPECTED_RECEIVERS, List.class);
        }
        for (final TestSubscriber<?> currentReceiver : receiver) {
            final List<?> receivedMessages = currentReceiver.getReceivedMessages();
            pollUntilEquals(receivedMessages::size, expectedNumberOfDeliveredMessages);
        }
    }

    public static void assertNoMessagesReceived(final TestEnvironment testEnvironment) {
        pollUntil(() -> {
            if (testEnvironment.has(SINGLE_SEND_MESSAGE)) {
                return true;
            } else {
                if (testEnvironment.has(NUMBER_OF_MESSAGES_SHOULD_BE_SEND)) {
                    final Object expectedNumberOfMessages = testEnvironment.getProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND);
                    final List<?> actualSendMessages = testEnvironment.getPropertyAsType(MESSAGES_SEND, List.class);
                    return SharedTestValidations.testEquals(expectedNumberOfMessages, actualSendMessages.size());
                } else {
                    return false;
                }
            }
        });
        assertNumberOfMessagesReceived(testEnvironment, 0);
    }

    public static void assertSutWasShutdownInTime(final CloseActions closeActions,
                                                  final TestEnvironment testEnvironment) {
        pollUntil(() -> testEnvironment.has(RESULT));
        final boolean wasTerminatedInTime = testEnvironment.getPropertyAsType(RESULT, Boolean.class);
        final boolean isShutdown = closeActions.isClosed();
        assertTrue(isShutdown);
        assertTrue(wasTerminatedInTime);
    }

    public static void assertSutIsShutdown(final CloseActions closeActions) {
        final boolean isShutdown = closeActions.isClosed();
        assertTrue(isShutdown);
    }

    public static void assertEachMessagesToBeReceivedByOnlyOneSubscriber(final TestEnvironment testEnvironment) {
        final List<?> expectedMessages = (List<?>) testEnvironment.getProperty(MESSAGES_SEND);
        final List<SimpleTestSubscriber<?>> receivers = getPotentialReceiver(testEnvironment);
        for (final Object expectedMessage : expectedMessages) {
            final List<SimpleTestSubscriber<?>> subscribersThatReceivedMessage = subscribersThatReceivedMessage(receivers,
                    expectedMessage);
            assertThat(subscribersThatReceivedMessage.size(), equalTo(1));
        }
    }

    private static List<SimpleTestSubscriber<?>> subscribersThatReceivedMessage(final List<SimpleTestSubscriber<?>> receivers,
                                                                                final Object expectedMessage) {
        return receivers.stream()
                .filter(subscriber -> subscriber.getReceivedMessages().contains(expectedMessage))
                .collect(Collectors.toList());
    }

    public static void assertResultEqualToExpectedFilter(final TestEnvironment testEnvironment) {
        final List<?> expectedFilter = (List<?>) testEnvironment.getProperty(EXPECTED_FILTER);
        final List<?> list = testEnvironment.getPropertyAsType(RESULT, List.class);
        assertThat(list, containsInAnyOrder(expectedFilter.toArray()));
    }

    public static void assertTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId(
            final TestEnvironment testEnvironment,
            final ProcessingContext<?> result) {
        final MessageId messageId = testEnvironment.getPropertyAsType(SEND_MESSAGE_ID, MessageId.class);
        final MessageId receivedMessageId = result.getMessageId();
        assertThat(messageId, notNullValue());
        assertThat(receivedMessageId, equalTo(messageId));

        final CorrelationId correlationIdForAnswer = result.generateCorrelationIdForAnswer();
        assertTrue(correlationIdForAnswer.matches(messageId));
    }

    public static void assertTheCorrelationIdToBeSetWhenReceived(final TestEnvironment testEnvironment,
                                                                 final ProcessingContext<?> result) {
        final MessageId messageId = testEnvironment.getPropertyAsType(SEND_MESSAGE_ID, MessageId.class);
        final MessageId receivedMessageId = result.getMessageId();
        assertThat(messageId, notNullValue());
        assertThat(receivedMessageId, equalTo(messageId));

        final CorrelationId expectedCorrelationId = getExpectedCorrelationId(testEnvironment);
        final CorrelationId correlationId = result.getCorrelationId();
        assertThat(correlationId, equalTo(expectedCorrelationId));
    }

    private static CorrelationId getExpectedCorrelationId(final TestEnvironment testEnvironment) {
        return testEnvironment.getPropertyAsType(EXPECTED_CORRELATION_ID, CorrelationId.class);
    }

    @SuppressWarnings("unchecked")
    private static List<SimpleTestSubscriber<?>> getExpectedReceiversAsSubscriber(final TestEnvironment testEnvironment) {
        return (List<SimpleTestSubscriber<?>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
    }

    @SuppressWarnings("unchecked")
    private static List<Subscriber<?>> getExpectedSubscriber(final TestEnvironment testEnvironment) {
        return (List<Subscriber<?>>) testEnvironment.getProperty(EXPECTED_SUBSCRIBER);
    }

    @SuppressWarnings("unchecked")
    private static List<SimpleTestSubscriber<?>> getPotentialReceiver(final TestEnvironment testEnvironment) {
        return (List<SimpleTestSubscriber<?>>) testEnvironment.getProperty(POTENTIAL_RECEIVERS);
    }
}
