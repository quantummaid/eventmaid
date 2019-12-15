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
import de.quantummaid.messagemaid.channel.givenWhenThen.FilterPosition;
import de.quantummaid.messagemaid.filtering.Filter;
import de.quantummaid.messagemaid.identification.CorrelationId;
import de.quantummaid.messagemaid.identification.MessageId;
import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.messageBus.MessageBusStatusInformation;
import de.quantummaid.messagemaid.messageBus.exception.MessageBusExceptionListener;
import de.quantummaid.messagemaid.messageBus.statistics.MessageBusStatistics;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.processingContext.ProcessingContext;
import de.quantummaid.messagemaid.shared.environment.TestEnvironment;
import de.quantummaid.messagemaid.shared.exceptions.TestException;
import de.quantummaid.messagemaid.shared.pipeChannelMessageBus.testActions.*;
import de.quantummaid.messagemaid.shared.subscriber.SimpleTestSubscriber;
import de.quantummaid.messagemaid.shared.testMessages.TestMessage;
import de.quantummaid.messagemaid.shared.testMessages.TestMessageOfInterest;
import de.quantummaid.messagemaid.shared.utils.FilterTestUtils;
import de.quantummaid.messagemaid.subscribing.Subscriber;
import de.quantummaid.messagemaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static de.quantummaid.messagemaid.identification.CorrelationId.newUniqueCorrelationId;
import static de.quantummaid.messagemaid.messageBus.givenWhenThen.MessageBusTestProperties.CORRELATION_SUBSCRIPTION_ID;
import static de.quantummaid.messagemaid.messageBus.givenWhenThen.MessageBusTestProperties.MESSAGE_RECEIVED_BY_ERROR_LISTENER;
import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.RESULT;
import static de.quantummaid.messagemaid.shared.eventType.TestEventType.testEventType;
import static de.quantummaid.messagemaid.shared.pipeChannelMessageBus.testActions.TestFilter.*;
import static de.quantummaid.messagemaid.shared.properties.SharedTestProperties.*;
import static de.quantummaid.messagemaid.shared.subscriber.SimpleTestSubscriber.testSubscriber;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
final class MessageBusTestActions implements SendingAndReceivingActions, RawSubscribeActions, ProcessingContextSendingActions,
        CorrelationIdSendingActions, FilterTestActions, SimplifiedFilterTestActions {
    private final MessageBus messageBus;

    static MessageBusTestActions messageBusTestActions(final MessageBus messageBus) {
        return new MessageBusTestActions(messageBus);
    }

    static void addSubscriberForACorrelationId(final MessageBus messageBus,
                                               final TestEnvironment testEnvironment) {
        final CorrelationId correlationId = newUniqueCorrelationId();
        final SimpleTestSubscriber<ProcessingContext<Object>> subscriber = testSubscriber();
        final SubscriptionId subscriptionId = messageBus.subscribe(correlationId, subscriber);
        testEnvironment.setProperty(EXPECTED_CORRELATION_ID, correlationId);
        testEnvironment.setProperty(CORRELATION_SUBSCRIPTION_ID, subscriptionId);
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
    }

    static void addDynamicErrorListenerForEventType(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final SubscriptionId subscriptionId = messageBus.onException(eventType, (m, e) -> {
            testEnvironment.setPropertyIfNotSet(RESULT, e);
            testEnvironment.setPropertyIfNotSet(MESSAGE_RECEIVED_BY_ERROR_LISTENER, m);
        });
        testEnvironment.setProperty(USED_SUBSCRIPTION_ID, subscriptionId);
    }

    static void addTwoDynamicErrorListenerForEventType_whereTheFirstWillBeRemoved(final MessageBus messageBus,
                                                                                  final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final SubscriptionId subscriptionId = messageBus.onException(eventType, (m, e) -> {
            throw new RuntimeException("Should not be called");
        });
        testEnvironment.setProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        messageBus.onException(eventType, (m, e) -> {
            testEnvironment.setPropertyIfNotSet(RESULT, e);
            testEnvironment.setPropertyIfNotSet(MESSAGE_RECEIVED_BY_ERROR_LISTENER, m);
        });
    }

    static List<MessageBusExceptionListener> queryListOfDynamicExceptionListener(final MessageBus messageBus) {
        return messageBus.getStatusInformation().getAllExceptionListener();
    }

    public static void removeDynamicExceptionHandler(final MessageBus messageBus,
                                                     final TestEnvironment testEnvironment) {
        final SubscriptionId subscriptionId = testEnvironment.getPropertyAsType(USED_SUBSCRIPTION_ID, SubscriptionId.class);
        messageBus.unregisterExceptionListener(subscriptionId);
    }

    static void addTwoFilterOnSpecificPositions(final MessageBus messageBus,
                                                final TestEnvironment testEnvironment) {
        final String firstAppend = "1nd";
        final String secondAppend = "2nd";
        testEnvironment.setProperty(EXPECTED_CHANGED_CONTENT, TestMessageOfInterest.CONTENT + firstAppend + secondAppend);
        final Filter<TestMessage> filter1 = aContentAppendingFilter(secondAppend);
        final MessageBusTestActions testActions = MessageBusTestActions.messageBusTestActions(messageBus);
        testActions.addNotRawFilter(filter1, 0);
        testEnvironment.addToListProperty(EXPECTED_FILTER, filter1);
        final Filter<TestMessage> filter2 = aContentAppendingFilter(firstAppend);
        testActions.addNotRawFilter(filter2, 0);
        testEnvironment.addToListProperty(EXPECTED_FILTER, filter2);
    }

    public static void addAnExceptionThrowingFilterInChannelOf(final MessageBus messageBus,
                                                               final TestEnvironment testEnvironment,
                                                               final EventType eventType) {
        final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
        testEnvironment.setPropertyIfNotSet(EVENT_TYPE, eventType);
        final Channel<Object> channel = statusInformation.getChannelFor(eventType);
        final RuntimeException exception = new TestException();
        final Filter<ProcessingContext<Object>> filter = anErrorThrowingFilter(exception);
        channel.addProcessFilter(filter);
    }

    static void addARawFilterThatChangesTheContentOfEveryMessage(final MessageBus messageBus) {
        final Filter<ProcessingContext<Object>> filter = aRawFilterThatChangesTheCompleteProcessingContext();
        messageBus.addRaw(filter);
    }

    static void removeAFilter(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final MessageBusTestActions testActions = MessageBusTestActions.messageBusTestActions(messageBus);
        FilterTestUtils.removeAFilter(testActions, testEnvironment);
    }

    public static Channel<Object> queryChannelForEventType(final MessageBus messageBus, final EventType eventType) {
        final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
        final Channel<Object> channel = statusInformation.getChannelFor(eventType);
        return channel;
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        messageBus.close(finishRemainingTasks);
    }

    @Override
    public boolean await(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return messageBus.awaitTermination(timeout, timeUnit);
    }

    @Override
    public boolean isClosed() {
        return messageBus.isClosed();
    }

    @Override
    public MessageId send(final EventType eventType, final TestMessage message) {
        return messageBus.send(eventType, message);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MessageId send(final ProcessingContext<TestMessage> processingContext) {
        final ProcessingContext<?> testMessageProcessingContext = processingContext;
        final ProcessingContext<Object> objectProcessingContext = (ProcessingContext<Object>) testMessageProcessingContext;
        return messageBus.send(objectProcessingContext);
    }

    @Override
    public MessageId send(final EventType eventType, final TestMessage testMessage, final CorrelationId correlationId) {
        return messageBus.send(eventType, testMessage, correlationId);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void subscribe(final EventType eventType, final Subscriber<TestMessage> subscriber) {
        final Subscriber degenerifiedSubscriber = subscriber;
        messageBus.subscribe(eventType, degenerifiedSubscriber);
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        messageBus.unsubcribe(subscriptionId);
    }

    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        return messageBus.getStatusInformation().getAllSubscribers();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public SubscriptionId subscribeRaw(final EventType eventType, final Subscriber<ProcessingContext<TestMessage>> subscriber) {
        final Subscriber degenerifiedSubscriber = subscriber;
        return messageBus.subscribeRaw(eventType, degenerifiedSubscriber);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addFilter(final Filter<ProcessingContext<TestMessage>> filter, final FilterPosition filterPosition) {
        final Filter degenerifiedFilter = filter;
        messageBus.add(degenerifiedFilter);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addFilter(final Filter<ProcessingContext<TestMessage>> filter,
                          final FilterPosition filterPosition,
                          final int position) {
        final Filter degenerifiedFilter = filter;
        messageBus.add(degenerifiedFilter);
    }

    @Override
    public List<?> getFilter(final FilterPosition filterPosition) {
        return messageBus.getFilter();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeFilter(final Filter<?> filter, final FilterPosition filterPosition) {
        final Filter<Object> castedFilter = (Filter<Object>) filter;
        messageBus.remove(castedFilter);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addNotRawFilter(final Filter<TestMessage> filter) {
        final Filter degenerifiedFilter = filter;
        messageBus.add(degenerifiedFilter);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addNotRawFilter(final Filter<TestMessage> filter, final int position) {
        final Filter degenerifiedFilter = filter;
        messageBus.add(degenerifiedFilter, position);
    }

    @Override
    public long numberOfQueuedMessages() {
        return queryTheNumberOfQueuedMessages();
    }

    long queryTheNumberOfAcceptedMessages() {
        return queryMessageStatistics(MessageBusStatistics::getAcceptedMessages);
    }

    long queryTheNumberOfQueuedMessages() {
        return queryMessageStatistics(MessageBusStatistics::getQueuedMessages);
    }

    long queryTheNumberOfSuccessfulDeliveredMessages() {
        return queryMessageStatistics(MessageBusStatistics::getSuccessfulMessages);
    }

    long queryTheNumberOfFailedDeliveredMessages() {
        return queryMessageStatistics(MessageBusStatistics::getFailedMessages);
    }

    long queryTheNumberOfBlockedMessages() {
        return queryMessageStatistics(MessageBusStatistics::getBlockedMessages);
    }

    long queryTheNumberOfForgottenMessages() {
        return queryMessageStatistics(MessageBusStatistics::getForgottenMessages);
    }

    private long queryMessageStatistics(final Function<MessageBusStatistics, BigInteger> query) {
        final MessageBusStatistics messageBusStatistics = getMessageBusStatistics();
        final BigInteger statistic = query.apply(messageBusStatistics);
        final long longValueExact = statistic.longValueExact();
        return longValueExact;
    }

    Date queryTheTimestampOfTheMessageStatistics() {
        final MessageBusStatistics messageBusStatistics = getMessageBusStatistics();
        final Date timestamp = messageBusStatistics.getTimestamp();
        return timestamp;
    }

    private MessageBusStatistics getMessageBusStatistics() {
        final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
        return statusInformation.getCurrentMessageStatistics();
    }

}
