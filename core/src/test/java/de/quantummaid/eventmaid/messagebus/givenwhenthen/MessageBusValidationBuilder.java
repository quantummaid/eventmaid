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

import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.messagebus.exception.MessageBusExceptionListener;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.processingcontext.ProcessingContext;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.exceptions.TestException;
import de.quantummaid.eventmaid.shared.givenwhenthen.TestValidation;
import de.quantummaid.eventmaid.shared.subscriber.TestSubscriber;
import de.quantummaid.eventmaid.shared.testmessages.TestMessage;
import de.quantummaid.eventmaid.shared.validations.SharedTestValidations;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

import static de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusTestActions.queryListOfDynamicExceptionListener;
import static de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusTestValidations.*;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.RESULT;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.SUT;
import static de.quantummaid.eventmaid.shared.pipechannelmessagebus.PipeChannelMessageBusSharedTestValidations.*;
import static de.quantummaid.eventmaid.shared.polling.PollingUtils.pollUntilListHasSize;
import static de.quantummaid.eventmaid.shared.properties.SharedTestProperties.*;
import static de.quantummaid.eventmaid.shared.validations.SharedTestValidations.assertPropertyTrue;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusValidationBuilder {
    private final TestValidation testValidation;

    private static MessageBusValidationBuilder asValidation(final TestValidation testValidation) {
        return new MessageBusValidationBuilder(testValidation);
    }

    public static MessageBusValidationBuilder expectTheMessageToBeReceived() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertExpectedReceiverReceivedSingleMessage(testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectTheMessagesToBeReceivedByAllSubscriber(final TestMessage... testMessages) {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final List<TestMessage> expectedResult = Arrays.asList(testMessages);
            assertExpectedReceiverReceivedAllMessages(testEnvironment, expectedResult);
        });
    }

    public static MessageBusValidationBuilder expectAllMessagesToBeReceivedByAllSubscribers() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertExpectedReceiverReceivedAllMessages(testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectTheErrorPayloadToBeReceived() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertExpectedReceiverReceivedMessageWithErrorPayload(testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectAllRemainingSubscribersToStillBeSubscribed() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final MessageBusTestActions testActions = getMessageBusTestActions(testEnvironment);
            assertSutStillHasExpectedSubscriber(testActions, testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectAllMessagesToHaveTheContentChanged() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final MessageBusTestActions sutActions = getMessageBusTestActions(testEnvironment);
            assertAllMessagesHaveContentChanged(sutActions, testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectAllProcessingContextsToBeReplaced() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final MessageBus messageBus = getMessageBus(testEnvironment);
            assertAllReceivedProcessingContextsWereChanged(messageBus, testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final ProcessingContext<?> result = getOnlyMessageFromSingleReceiver(testEnvironment);
            assertTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId(testEnvironment, result);
        });
    }

    public static MessageBusValidationBuilder expectTheCorrelationIdToBeSetWhenReceived() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final ProcessingContext<?> result = getOnlyMessageFromSingleReceiver(testEnvironment);
            assertTheCorrelationIdToBeSetWhenReceived(testEnvironment, result);
        });
    }

    public static MessageBusValidationBuilder expectXMessagesToBeDelivered(final int expectedNumberOfDeliveredMessages) {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertNumberOfMessagesReceived(testEnvironment, expectedNumberOfDeliveredMessages);
        });
    }

    public static MessageBusValidationBuilder expectNoMessagesToBeDelivered() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertNoMessagesReceived(testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectResultToBe(final Object expectedResult) {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultEqualsExpected(testEnvironment, expectedResult);
        });
    }

    public static MessageBusValidationBuilder expectTimestampToBeInTheLastXSeconds(final long maximumSecondsDifference) {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertTimestampToBeInTheLastXSeconds(testEnvironment, maximumSecondsDifference);
        });
    }

    public static MessageBusValidationBuilder expectAListOfSize(final int expectedSize) {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultIsListOfSize(testEnvironment, expectedSize);
        });
    }

    public static MessageBusValidationBuilder expectTheCorrectChannel() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectSubscriberOfType(final int expectedNumberOfSubscribers,
                                                                     final EventType eventType) {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertAmountOfSubscriberForType(expectedNumberOfSubscribers, eventType, testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectTheMessageBusToBeShutdownInTime() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final MessageBusTestActions testActions = getMessageBusTestActions(testEnvironment);
            assertSutWasShutdownInTime(testActions, testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectTheMessageBusToBeShutdown() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final MessageBusTestActions testActions = getMessageBusTestActions(testEnvironment);
            assertSutIsShutdown(testActions);
        });
    }

    public static MessageBusValidationBuilder expectNoException() {
        return asValidation(SharedTestValidations::assertNoExceptionThrown);
    }

    public static MessageBusValidationBuilder expectTheException(final Class<?> expectedExceptionClass) {
        return asValidation(testEnvironment -> SharedTestValidations.assertExceptionThrownOfType(testEnvironment, expectedExceptionClass));
    }

    public static MessageBusValidationBuilder expectTheExceptionHandled(final Class<?> expectedExceptionClass) {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertTheExceptionHandled(expectedExceptionClass, testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectTheExceptionHandledAsFilterException(final Class<?> expectedExceptionClass) {
        return expectTheExceptionHandledAs(expectedExceptionClass, EXCEPTION_OCCURRED_INSIDE_FILTER);
    }

    public static MessageBusValidationBuilder expectTheExceptionHandledAsDeliverException(final Class<?> expectedExceptionClass) {
        return expectTheExceptionHandledAs(expectedExceptionClass, EXCEPTION_OCCURRED_DURING_DELIVERY);
    }

    private static MessageBusValidationBuilder expectTheExceptionHandledAs(final Class<?> expectedExceptionClass,
                                                                           final String expectedExceptionProperty) {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultOfClass(testEnvironment, expectedExceptionClass);
            assertPropertyTrue(testEnvironment, expectedExceptionProperty);
        });
    }

    public static MessageBusValidationBuilder expectTheExceptionHandledOnlyByTheRemainingHandlers(
            final Class<?> expectedExceptionClass) {
        return expectTheExceptionHandled(expectedExceptionClass);
    }

    public static MessageBusValidationBuilder expectTheExceptionHandledAndTheErrorToBeThrown(
            final Class<?> expectedExceptionClass) {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertExceptionThrownOfType(testEnvironment, TestException.class, RESULT);
            SharedTestValidations.assertResultOfClass(testEnvironment, expectedExceptionClass);
        });
    }

    public static MessageBusValidationBuilder expectAListWithAllFilters() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertResultEqualToExpectedFilter(testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectTheRemainingFilter() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertSutHasExpectedFilter(testEnvironment);
        });
    }

    private static void assertSutHasExpectedFilter(final TestEnvironment testEnvironment) {
        final List<?> expectedFilter = (List<?>) testEnvironment.getProperty(EXPECTED_FILTER);
        final MessageBus messageBus = getMessageBus(testEnvironment);
        final List<?> list = messageBus.getFilter();
        assertThat(list, containsInAnyOrder(expectedFilter.toArray()));
    }

    public static MessageBusValidationBuilder expectTheMessageWrappedInProcessingContextWithCorrectCorrelationIdToBeReceived() {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertAllReceiverReceivedProcessingContextWithCorrectCorrelationId(testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectNumberOfErrorListener(final int expectedNumberOfListener) {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final MessageBus messageBus = getMessageBus(testEnvironment);
            final List<MessageBusExceptionListener> listeners = queryListOfDynamicExceptionListener(messageBus);
            SharedTestValidations.assertCollectionOfSize(listeners, expectedNumberOfListener);
        });
    }

    private static MessageBus getMessageBus(final TestEnvironment testEnvironment) {
        final MessageBus messageBus = testEnvironment.getPropertyAsType(SUT, MessageBus.class);
        return messageBus;
    }

    private static ProcessingContext<?> getOnlyMessageFromSingleReceiver(final TestEnvironment testEnvironment) {
        final List<TestSubscriber<Object>> testSubscribers = getExpectedReceiver(testEnvironment);
        assertThat(testSubscribers.size(), equalTo(1));
        final TestSubscriber<?> testSubscriber = testSubscribers.get(0);
        pollUntilListHasSize(testSubscriber::getReceivedMessages, 1);
        final List<?> receivedMessages = testSubscriber.getReceivedMessages();
        return (ProcessingContext<?>) receivedMessages.get(0);
    }

    @SuppressWarnings("unchecked")
    private static List<TestSubscriber<Object>> getExpectedReceiver(final TestEnvironment testEnvironment) {
        return (List<TestSubscriber<Object>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
    }

    private static MessageBusTestActions getMessageBusTestActions(final TestEnvironment testEnvironment) {
        final MessageBus messageBus = getMessageBus(testEnvironment);
        return MessageBusTestActions.messageBusTestActions(messageBus);
    }

    public MessageBusValidationBuilder and(final MessageBusValidationBuilder messageBusValidationBuilder) {
        return new MessageBusValidationBuilder(testEnvironment -> {
            testValidation.validate(testEnvironment);
            messageBusValidationBuilder.testValidation.validate(testEnvironment);
        });
    }

    public TestValidation build() {
        return testValidation;
    }
}
