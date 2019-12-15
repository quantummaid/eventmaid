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

package de.quantummaid.messagemaid.channel.givenWhenThen;

import de.quantummaid.messagemaid.shared.validations.SharedTestValidations;
import de.quantummaid.messagemaid.channel.Channel;
import de.quantummaid.messagemaid.channel.action.*;
import de.quantummaid.messagemaid.filtering.Filter;
import de.quantummaid.messagemaid.processingContext.ProcessingContext;
import de.quantummaid.messagemaid.shared.environment.TestEnvironment;
import de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty;
import de.quantummaid.messagemaid.shared.givenWhenThen.TestValidation;
import de.quantummaid.messagemaid.shared.polling.PollingUtils;
import de.quantummaid.messagemaid.shared.testMessages.TestMessage;
import de.quantummaid.messagemaid.subscribing.Subscriber;
import de.quantummaid.messagemaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

import static de.quantummaid.messagemaid.channel.givenWhenThen.ChannelTestValidations.*;
import static de.quantummaid.messagemaid.channel.givenWhenThen.ProcessingFrameHistoryMatcher.aProcessingFrameHistory;
import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.*;
import static de.quantummaid.messagemaid.shared.properties.SharedTestProperties.*;
import static de.quantummaid.messagemaid.shared.pipeChannelMessageBus.PipeChannelMessageBusSharedTestValidations.*;
import static de.quantummaid.messagemaid.shared.polling.PollingUtils.pollUntil;
import static de.quantummaid.messagemaid.shared.validations.SharedTestValidations.*;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelValidationBuilder {
    private final TestValidation testValidation;

    private static ChannelValidationBuilder aValidation(final TestValidation testValidation) {
        return new ChannelValidationBuilder(testValidation);
    }

    public static ChannelValidationBuilder expectTheMessageToBeConsumed() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectTheMessageToBeConsumedByTheSecondChannel() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectAllChannelsToBeContainedInTheHistory() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final List<Channel<TestMessage>> expectedTraversedChannels = getTestPropertyAsListOfChannel(testEnvironment,
                    ChannelTestProperties.ALL_CHANNELS);
            assertResultTraversedAllChannelBasedOnTheirDefaultActions(testEnvironment, expectedTraversedChannels);
        });
    }

    public static ChannelValidationBuilder expectTheMessageToHaveReturnedSuccessfully() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final Channel<TestMessage> firstChannel = getTestPropertyAsChannel(testEnvironment, SUT);
            final Channel<TestMessage> callTargetChannel = getTestPropertyAsChannel(testEnvironment, ChannelTestProperties.CALL_TARGET_CHANNEL);
            final Channel<TestMessage> returningTargetChannel = getTestPropertyAsChannel(testEnvironment, ChannelTestProperties.RETURNING_CHANNEL);
            assertMessageFollowedChannelWithActions(testEnvironment, aProcessingFrameHistory()
                    .withAFrameFor(firstChannel, Call.class)
                    .withAFrameFor(callTargetChannel, Jump.class)
                    .withAFrameFor(returningTargetChannel, Return.class)
                    .withAFrameFor(firstChannel, Consume.class));
        });
    }

    public static ChannelValidationBuilder expectTheMessageToHaveReturnedFromAllCalls() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final Channel<TestMessage> initialChannel = getTestPropertyAsChannel(testEnvironment, SUT);
            final List<Channel<TestMessage>> callTargetLists = getTestPropertyAsListOfChannel(testEnvironment,
                    ChannelTestProperties.CALL_TARGET_CHANNEL);
            final Channel<TestMessage> firstCallTargetChannel = callTargetLists.get(0);
            final Channel<TestMessage> secondCallTargetChannel = callTargetLists.get(1);
            final Channel<TestMessage> returningTargetChannel = getTestPropertyAsChannel(testEnvironment, ChannelTestProperties.RETURNING_CHANNEL);
            assertMessageFollowedChannelWithActions(testEnvironment, aProcessingFrameHistory()
                    .withAFrameFor(initialChannel, Call.class)
                    .withAFrameFor(firstCallTargetChannel, Call.class)
                    .withAFrameFor(secondCallTargetChannel, Jump.class)
                    .withAFrameFor(returningTargetChannel, Return.class)
                    .withAFrameFor(firstCallTargetChannel, Return.class)
                    .withAFrameFor(initialChannel, Consume.class));
        });
    }

    public static ChannelValidationBuilder expectNoMessageToBeDelivered() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            pollUntil(() -> testEnvironment.has(SINGLE_SEND_MESSAGE));
            SharedTestValidations.assertNoResultSet(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectAExceptionOfType(final Class<?> expectedExceptionClass) {
        return aValidation(testEnvironment -> SharedTestValidations.assertExceptionThrownOfType(testEnvironment, expectedExceptionClass));
    }

    public static ChannelValidationBuilder expectADeliveryExceptionOfType(final Class<?> expectedExceptionClass) {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertExceptionThrownOfType(testEnvironment, expectedExceptionClass);
            assertPropertyTrue(testEnvironment, EXCEPTION_OCCURRED_DURING_DELIVERY);
        });
    }

    public static ChannelValidationBuilder expectTheChangedActionToBeExecuted() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectAllFilterToBeInCorrectOrderInChannel() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = getTestPropertyAsListOfFilter(testEnvironment,
                    EXPECTED_RESULT);
            assertFilterAsExpected(testEnvironment, expectedFilter);
        });
    }

    public static ChannelValidationBuilder expectTheFilterInOrderAsAdded() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertResultEqualToExpectedFilter(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectTheAllRemainingFilter() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = getTestPropertyAsListOfFilter(testEnvironment,
                    EXPECTED_FILTER);
            assertFilterAsExpected(testEnvironment, expectedFilter);
        });
    }

    public static ChannelValidationBuilder expectTheMetaDataChangePersist() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertMetaDatumOfResultSetAsExpected(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectTheResult(final Object expectedResult) {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultEqualsExpected(testEnvironment, expectedResult);
        });
    }

    public static ChannelValidationBuilder expectTheDeliveryExceptionCatched(final Class<?> expectedResultClass) {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultOfClass(testEnvironment, expectedResultClass);
            assertPropertyTrue(testEnvironment, EXCEPTION_OCCURRED_DURING_DELIVERY);
        });
    }

    public static ChannelValidationBuilder expectTheFilterExceptionCatched(final Class<?> expectedResultClass) {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultOfClass(testEnvironment, expectedResultClass);
            assertPropertyTrue(testEnvironment, EXCEPTION_OCCURRED_INSIDE_FILTER);
        });
    }

    public static ChannelValidationBuilder expectTheException(final Class<?> expectedExceptionClass) {
        return aValidation(testEnvironment -> SharedTestValidations.assertExceptionThrownOfType(testEnvironment, expectedExceptionClass));
    }

    public static ChannelValidationBuilder expectTheMessageToBeReceivedByAllRemainingSubscriber() {
        return expectTheMessageToBeReceivedByAllSubscriber();
    }

    public static ChannelValidationBuilder expectTheMessageToBeReceivedByAllSubscriber() {
        return aValidation(testEnvironment -> {
            final ProcessingContext<?> processingContext = getExpectedProcessingContext(testEnvironment);
            final Object expectedMessage = processingContext.getPayload();
            final List<?> expectedTestMessages = Collections.singletonList(expectedMessage);
            assertExpectedReceiverReceivedAllMessages(testEnvironment, expectedTestMessages);
        });
    }

    public static ChannelValidationBuilder expectTheProcessingContextObjectToBeReceivedByAllSubscriber() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final ProcessingContext<?> processingContext = getExpectedProcessingContext(testEnvironment);
            final List<?> expectedTestMessages = Collections.singletonList(processingContext);
            assertExpectedReceiverReceivedAllMessages(testEnvironment, expectedTestMessages);
        });
    }

    private static ProcessingContext<?> getExpectedProcessingContext(final TestEnvironment testEnvironment) {
        return testEnvironment.getPropertyAsType(EXPECTED_RESULT, ProcessingContext.class);
    }

    public static ChannelValidationBuilder expectRemainingSubscriber() {
        return aValidation(testEnvironment -> {
            final Channel<TestMessage> channel = getTestPropertyAsChannel(testEnvironment, SUT);
            final Subscription<TestMessage> subscription = (Subscription<TestMessage>) channel.getDefaultAction();
            final List<Subscriber<?>> subscribers = subscription.getAllSubscribers();
            final List<Subscriber<?>> expectedSubscribers = getPropertyAsListOfSubscriber(testEnvironment);
            SharedTestValidations.assertEquals(subscribers.size(), expectedSubscribers.size());
            for (final Subscriber<?> expectedSubscriber : expectedSubscribers) {
                final SubscriptionId expectedSubscriptionId = expectedSubscriber.getSubscriptionId();
                subscribers.stream()
                        .filter(s -> s.getSubscriptionId().equals(expectedSubscriptionId))
                        .findAny()
                        .orElseThrow(AssertionError::new);
            }
        });
    }

    public static ChannelValidationBuilder expectTheChannelToBeShutdown() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertIsShutdown(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectTheShutdownToBeSucceededInTime() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertIsShutdown(testEnvironment);
            SharedTestValidations.assertResultEqualsExpected(testEnvironment, true);
        });
    }

    public static ChannelValidationBuilder expectTheShutdownToBeFailed() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertIsShutdown(testEnvironment);
            SharedTestValidations.assertResultEqualsExpected(testEnvironment, false);
        });
    }

    public static ChannelValidationBuilder expectOnlyTheFirstSubscriberToBeCalled() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertOnlyFirstSubscriberReceivedMessage(testEnvironment);
        });
    }

    public static ChannelValidationBuilder expectTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final ProcessingContext<?> result = getResultProcessingContext(testEnvironment);
            assertTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId(testEnvironment, result);
        });
    }

    public static ChannelValidationBuilder expectTheCorrelationIdToBeSetWhenReceived() {
        return aValidation(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final ProcessingContext<?> result = getResultProcessingContext(testEnvironment);
            assertTheCorrelationIdToBeSetWhenReceived(testEnvironment, result);
        });
    }

    private static ProcessingContext<?> getResultProcessingContext(final TestEnvironment testEnvironment) {
        PollingUtils.pollUntil(() -> testEnvironment.has(RESULT));
        return testEnvironment.getPropertyAsType(RESULT, ProcessingContext.class);
    }

    private static void assertIsShutdown(final TestEnvironment testEnvironment) {
        final Channel<TestMessage> channel = getTestPropertyAsChannel(testEnvironment, SUT);
        final boolean isShutdown = channel.isClosed();
        assertTrue(isShutdown);
    }

    private static Channel<TestMessage> getTestPropertyAsChannel(final TestEnvironment testEnvironment,
                                                                 final TestEnvironmentProperty property) {
        return getTestPropertyAsChannel(testEnvironment, property.name());
    }

    @SuppressWarnings("unchecked")
    private static Channel<TestMessage> getTestPropertyAsChannel(final TestEnvironment testEnvironment,
                                                                 final String property) {
        return (Channel<TestMessage>) testEnvironment.getProperty(property);
    }

    @SuppressWarnings("unchecked")
    private static List<Channel<TestMessage>> getTestPropertyAsListOfChannel(final TestEnvironment testEnvironment,
                                                                             final String property) {
        return (List<Channel<TestMessage>>) testEnvironment.getProperty(property);
    }

    private static List<Filter<ProcessingContext<TestMessage>>> getTestPropertyAsListOfFilter(
            final TestEnvironment testEnvironment,
            final TestEnvironmentProperty property) {
        return getTestPropertyAsListOfFilter(testEnvironment, property.name());
    }

    @SuppressWarnings("unchecked")
    private static List<Filter<ProcessingContext<TestMessage>>> getTestPropertyAsListOfFilter(
            final TestEnvironment testEnvironment,
            final String property) {
        return (List<Filter<ProcessingContext<TestMessage>>>) testEnvironment.getProperty(property);
    }

    @SuppressWarnings("unchecked")
    private static List<Subscriber<?>> getPropertyAsListOfSubscriber(final TestEnvironment testEnvironment) {
        return (List<Subscriber<?>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
    }

    public ChannelValidationBuilder and(final ChannelValidationBuilder other) {
        return new ChannelValidationBuilder(testEnvironment -> {
            this.testValidation.validate(testEnvironment);
            other.testValidation.validate(testEnvironment);
        });
    }

    public TestValidation build() {
        return testValidation;
    }
}
