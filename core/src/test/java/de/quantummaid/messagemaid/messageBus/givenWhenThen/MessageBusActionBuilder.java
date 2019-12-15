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

package de.quantummaid.messagemaid.messageBus.givenWhenThen;

import de.quantummaid.messagemaid.channel.Channel;
import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.messageBus.MessageBusStatusInformation;
import de.quantummaid.messagemaid.messageBus.exception.MessageBusExceptionListener;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.shared.environment.TestEnvironment;
import de.quantummaid.messagemaid.shared.givenWhenThen.TestAction;
import de.quantummaid.messagemaid.shared.subscriber.BlockingTestSubscriber;
import de.quantummaid.messagemaid.shared.testMessages.TestMessage;
import de.quantummaid.messagemaid.subscribing.Subscriber;
import de.quantummaid.messagemaid.subscribing.SubscriptionId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static de.quantummaid.messagemaid.messageBus.config.MessageBusTestConfig.ASYNCHRONOUS_DELIVERY_POOL_SIZE;
import static de.quantummaid.messagemaid.messageBus.givenWhenThen.MessageBusTestActions.*;
import static de.quantummaid.messagemaid.messageBus.givenWhenThen.MessageBusTestProperties.CORRELATION_SUBSCRIPTION_ID;
import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.RESULT;
import static de.quantummaid.messagemaid.shared.eventType.TestEventType.testEventType;
import static de.quantummaid.messagemaid.shared.properties.SharedTestProperties.*;
import static de.quantummaid.messagemaid.shared.polling.PollingUtils.pollUntilEquals;
import static de.quantummaid.messagemaid.shared.subscriber.BlockingTestSubscriber.blockingTestSubscriber;
import static de.quantummaid.messagemaid.shared.utils.FilterTestUtils.queryFilter;
import static de.quantummaid.messagemaid.shared.utils.SendingTestUtils.*;
import static de.quantummaid.messagemaid.shared.utils.ShutdownTestUtils.*;
import static de.quantummaid.messagemaid.shared.utils.SubscriptionTestUtils.*;

public final class MessageBusActionBuilder {
    private List<TestAction<MessageBus>> actions = new ArrayList<>();

    private MessageBusActionBuilder(final TestAction<MessageBus> action) {
        this.actions.add(action);
    }

    public static MessageBusActionBuilder aSingleMessageIsSend() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            sendSingleMessage(testActions, testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder aMessageWithoutPayloadIsSend() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            sendMessageAsProcessingContext(testActions, testEnvironment, null);
            return null;
        });
    }

    public static MessageBusActionBuilder aMessageWithoutEventType() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            sendSingleMessage(testActions, testEnvironment, null);
            return null;
        });
    }

    public static MessageBusActionBuilder theMessageIsSend(final TestMessage message) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            sendSingleMessage(testActions, testEnvironment, eventType, message);
            return null;
        });
    }

    public static MessageBusActionBuilder aMessageWithCorrelationIdIsSend() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = MessageBusTestActions.messageBusTestActions(messageBus);
            sendMessageWithCorrelationId(testActions, testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder severalMessagesAreSend(final int numberOfMessages) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            sendSeveralMessages(testActions, numberOfMessages, testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder severalMessagesAreSendAsynchronously(final int numberOfSender,
                                                                               final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            final boolean cleanShutdown = true;
            sendValidMessagesAsynchronouslyNew(testActions, testEnvironment, numberOfSender,
                    numberOfMessagesPerSender, cleanShutdown);
            return null;
        });
    }

    public static MessageBusActionBuilder severalMessagesAreSendAsynchronouslyButWillBeBlocked(final int numberOfMessages,
                                                                                               final int expectedBlockedThreads) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final Semaphore semaphore = new Semaphore(0);
            final BlockingTestSubscriber<TestMessage> subscriber = blockingTestSubscriber(semaphore);
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            addABlockingSubscriberAndThenSendXMessagesInEachThread(testActions, subscriber, numberOfMessages, 1,
                    testEnvironment, expectedBlockedThreads);
            return null;
        });
    }

    public static MessageBusActionBuilder severalMessagesAreSendAsynchronouslyButWillBeBlocked(final int numberOfMessages) {
        return severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfMessages, numberOfMessages);
    }

    public static MessageBusActionBuilder sendSeveralMessagesBeforeTheBusIsShutdown(final int numberOfSender,
                                                                                    final boolean finishRemainingTasks) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            final int expectedNumberOfBlockedThreads = determineExpectedNumberOfBlockedThreads(numberOfSender, testEnvironment);
            sendMessagesBeforeShutdownAsynchronously(testActions, testEnvironment, numberOfSender, finishRemainingTasks,
                    expectedNumberOfBlockedThreads);
            return null;
        });
    }

    private static int determineExpectedNumberOfBlockedThreads(final int numberOfMessages,
                                                               final TestEnvironment testEnvironment) {
        final int expectedBlockedThreads;
        if (testEnvironment.getPropertyAsType(IS_ASYNCHRONOUS, Boolean.class)) {
            expectedBlockedThreads = ASYNCHRONOUS_DELIVERY_POOL_SIZE;
        } else {
            expectedBlockedThreads = numberOfMessages;
        }
        return expectedBlockedThreads;
    }

    public static MessageBusActionBuilder aSingleMessageWithErrorPayloadIsSend() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = MessageBusTestActions.messageBusTestActions(messageBus);
            sendMessageWithErrorPayloadIsSend(testActions, testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder aSubscriberIsAdded(final EventType eventType) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            addASingleSubscriber(testActions, testEnvironment, eventType);
            return null;
        });
    }

    public static MessageBusActionBuilder oneSubscriberUnsubscribesSeveralTimes(final int numberOfUnsubscriptions) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            unsubscribeASubscriberXTimes(testActions, testEnvironment, numberOfUnsubscriptions);
            return null;
        });
    }

    public static MessageBusActionBuilder oneSubscriberUnsubscribes() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            unsubscribeASubscriberXTimes(testActions, testEnvironment, 1);
            return null;
        });
    }

    public static MessageBusActionBuilder theSubscriberForTheCorrelationIdUnsubscribes() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final SubscriptionId subscriptionId = getUsedSubscriptionId(testEnvironment);
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            unsubscribe(testActions, subscriptionId);
            return null;
        });
    }

    private static SubscriptionId getUsedSubscriptionId(final TestEnvironment testEnvironment) {
        return testEnvironment.getPropertyAsType(CORRELATION_SUBSCRIPTION_ID, SubscriptionId.class);
    }

    public static MessageBusActionBuilder halfValidAndInvalidMessagesAreSendAsynchronously(final int numberOfSender,
                                                                                           final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            sendMixtureOfValidAndInvalidMessagesAsynchronouslyNew(testActions, testEnvironment,
                    numberOfSender, numberOfMessagesPerSender);
            return null;
        });
    }

    public static MessageBusActionBuilder severalInvalidMessagesAreSendAsynchronously(final int numberOfSender,
                                                                                      final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            sendInvalidMessagesAsynchronouslyNew(testActions, testEnvironment, numberOfSender, numberOfMessagesPerSender);
            return null;
        });
    }

    public static MessageBusActionBuilder theNumberOfAcceptedMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            final Object expectedNumberOfSendMessages = testEnvironment.getProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND);
            pollUntilEquals(testActions::queryTheNumberOfAcceptedMessages, expectedNumberOfSendMessages);
            final long result = messageBusTestActions(messageBus).queryTheNumberOfAcceptedMessages();
            testEnvironment.setProperty(RESULT, result);
            return null;
        });
    }

    public static MessageBusActionBuilder theNumberOfQueuedMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final long result = messageBusTestActions(messageBus).queryTheNumberOfQueuedMessages();
            testEnvironment.setProperty(RESULT, result);
            return null;
        });
    }

    public static MessageBusActionBuilder theNumberOfSuccessfulMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            final Object expectedNumberOfSendMessages = testEnvironment.getProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND);
            pollUntilEquals(testActions::queryTheNumberOfSuccessfulDeliveredMessages, expectedNumberOfSendMessages);
            final long result = messageBusTestActions(messageBus).queryTheNumberOfSuccessfulDeliveredMessages();
            testEnvironment.setProperty(RESULT, result);
            return null;
        });
    }

    public static MessageBusActionBuilder theNumberOfFailedMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final Object expectedNumberOfMessages = testEnvironment.getProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND);
            pollUntilEquals(() -> testEnvironment.getPropertyAsType(MESSAGES_SEND, List.class).size(), expectedNumberOfMessages);
            final long result = messageBusTestActions(messageBus).queryTheNumberOfFailedDeliveredMessages();
            testEnvironment.setProperty(RESULT, result);
            return null;
        });
    }

    public static MessageBusActionBuilder theNumberOfBlockedMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            final Object expectedNumberOfSendMessages = testEnvironment.getProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND);
            pollUntilEquals(testActions::queryTheNumberOfBlockedMessages, expectedNumberOfSendMessages);
            final long result = messageBusTestActions(messageBus).queryTheNumberOfBlockedMessages();
            testEnvironment.setProperty(RESULT, result);
            return null;
        });
    }

    public static MessageBusActionBuilder theNumberOfForgottenMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            final Object expectedNumberOfSendMessages = testEnvironment.getProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND);
            pollUntilEquals(testActions::queryTheNumberOfForgottenMessages, expectedNumberOfSendMessages);
            final long result = messageBusTestActions(messageBus).queryTheNumberOfForgottenMessages();
            testEnvironment.setProperty(RESULT, result);
            return null;
        });
    }

    public static MessageBusActionBuilder theTimestampOfTheStatisticsIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final Date timestamp = messageBusTestActions(messageBus).queryTheTimestampOfTheMessageStatistics();
            testEnvironment.setProperty(RESULT, timestamp);
            return null;
        });
    }

    public static MessageBusActionBuilder theSubscriberAreQueriedPerType() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
            final Map<EventType, List<Subscriber<?>>> subscribersPerType = statusInformation.getSubscribersPerType();
            testEnvironment.setProperty(RESULT, subscribersPerType);
            return null;
        });
    }

    public static MessageBusActionBuilder allSubscribersAreQueriedAsList() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final List<Subscriber<?>> allSubscribers = messageBus.getStatusInformation().getAllSubscribers();
            testEnvironment.setProperty(RESULT, allSubscribers);
            return null;
        });
    }

    public static MessageBusActionBuilder theChannelForTheTypeIsQueried(final EventType eventType) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final Channel<Object> channel = MessageBusTestActions.queryChannelForEventType(messageBus, eventType);
            testEnvironment.setProperty(RESULT, channel);
            return null;
        });
    }

    public static MessageBusActionBuilder severalMessagesAreSendAsynchronouslyBeforeTheMessageBusIsShutdown(
            final int numberOfMessages) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            sendMessagesBeforeShutdownAsynchronously(testActions, testEnvironment, numberOfMessages, false);
            return null;
        });
    }

    public static MessageBusActionBuilder theMessageBusIsShutdownAsynchronouslyXTimes(final int numberOfThreads) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            shutdownTheSutAsynchronouslyXTimes(testActions, numberOfThreads);
            return null;
        });
    }

    public static MessageBusActionBuilder theMessageBusIsShutdown() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions sutActions = messageBusTestActions(messageBus);
            shutdownTheSut(sutActions, true);
            return null;
        });
    }

    public static MessageBusActionBuilder theMessageBusShutdownIsExpectedForTimeoutInSeconds(final int timeoutInSeconds) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions sutActions = messageBusTestActions(messageBus);
            closeAndThenAwaitTermination(sutActions, testEnvironment, timeoutInSeconds);
            return null;
        });
    }

    public static MessageBusActionBuilder theMessageBusShutdownIsAwaitedWithoutCloseCall() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions sutActions = messageBusTestActions(messageBus);
            callAwaitWithoutACloseIsCalled(sutActions, testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder theListOfFiltersIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = MessageBusTestActions.messageBusTestActions(messageBus);
            final List<?> filter = queryFilter(testActions, testEnvironment);
            testEnvironment.setProperty(RESULT, filter);
            return null;
        });
    }

    public static MessageBusActionBuilder aFilterIsRemoved() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            removeAFilter(messageBus, testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder anExceptionThrowingFilterIsAddedInChannelOf(final EventType eventType) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            addAnExceptionThrowingFilterInChannelOf(messageBus, testEnvironment, eventType);
            return null;
        });
    }

    public static MessageBusActionBuilder anExceptionThrowingSubscriberIsAdded() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusTestActions testActions = messageBusTestActions(messageBus);
            addAnExceptionThrowingSubscriber(testActions, testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder theDynamicExceptionHandlerToBeRemoved() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            removeDynamicExceptionHandler(messageBus, testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder allDynamicExceptionListenerAreQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final List<MessageBusExceptionListener> listeners = queryListOfDynamicExceptionListener(messageBus);
            testEnvironment.setPropertyIfNotSet(RESULT, listeners);
            return null;
        });
    }

    public MessageBusActionBuilder andThen(final MessageBusActionBuilder followUpBuilder) {
        actions.addAll(followUpBuilder.actions);
        return this;
    }

    public List<TestAction<MessageBus>> build() {
        return actions;
    }
}
