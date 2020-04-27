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

package de.quantummaid.eventmaid.channel.givenwhenthen;

import de.quantummaid.eventmaid.channel.Channel;
import de.quantummaid.eventmaid.channel.statistics.ChannelStatistics;
import de.quantummaid.eventmaid.filtering.Filter;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.processingcontext.ProcessingContext;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.givenwhenthen.TestAction;
import de.quantummaid.eventmaid.shared.subscriber.TestSubscriber;
import de.quantummaid.eventmaid.shared.testmessages.TestMessage;
import de.quantummaid.eventmaid.shared.testmessages.TestMessageOfInterest;
import de.quantummaid.eventmaid.shared.utils.SubscriptionTestUtils;

import java.util.LinkedList;
import java.util.List;

import static de.quantummaid.eventmaid.channel.config.ChannelTestConfig.ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE;
import static de.quantummaid.eventmaid.channel.givenwhenthen.ChannelTestActions.channelTestActions;
import static de.quantummaid.eventmaid.channel.givenwhenthen.ChannelTestActions.sendMessage;
import static de.quantummaid.eventmaid.channel.givenwhenthen.FilterPosition.*;
import static de.quantummaid.eventmaid.processingcontext.ProcessingContext.processingContextForPayloadAndError;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.*;
import static de.quantummaid.eventmaid.shared.polling.PollingUtils.pollUntilEquals;
import static de.quantummaid.eventmaid.shared.properties.SharedTestProperties.*;
import static de.quantummaid.eventmaid.shared.testmessages.TestMessageOfInterest.messageOfInterest;
import static de.quantummaid.eventmaid.shared.utils.FilterTestUtils.*;
import static de.quantummaid.eventmaid.shared.utils.SendingTestUtils.*;
import static de.quantummaid.eventmaid.shared.utils.ShutdownTestUtils.*;
import static de.quantummaid.eventmaid.shared.utils.SubscriptionTestUtils.addSeveralRawSubscriber;
import static de.quantummaid.eventmaid.shared.utils.SubscriptionTestUtils.addSeveralSubscriber;

public final class ChannelActionBuilder {
    private final List<TestAction<Channel<TestMessage>>> testActions;

    private ChannelActionBuilder(final TestAction<Channel<TestMessage>> testAction) {
        this.testActions = new LinkedList<>();
        testActions.add(testAction);
    }

    private static ChannelActionBuilder anAction(final TestAction<Channel<TestMessage>> testAction) {
        return new ChannelActionBuilder(testAction);
    }

    public static ChannelActionBuilder aMessageIsSend() {
        return anAction((channel, testEnvironment) -> {
            final TestMessageOfInterest message = ChannelTestActions.DEFAULT_TEST_MESSAGE;
            final ProcessingContext<TestMessage> processingContext = sendMessage(channel, testEnvironment, message);
            testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, processingContext);
            return null;
        });
    }

    public static ChannelActionBuilder aMessageWithoutPayloadIsSend() {
        return anAction((channel, testEnvironment) -> {
            final ProcessingContext<TestMessage> processingContext = sendMessage(channel, testEnvironment, null);
            testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, processingContext);
            return null;
        });
    }

    public static ChannelActionBuilder aMessageWithCorrelationIdIsSend() {
        return anAction((channel, testEnvironment) -> {
            final ChannelTestActions testActions = channelTestActions(channel);
            sendMessageWithCorrelationId(testActions, testEnvironment);
            return null;
        });
    }

    public static ChannelActionBuilder aProcessingContextObjectIsSend() {
        return anAction((channel, testEnvironment) -> {
            final ProcessingContext<TestMessage> processingContext =
                    sendMessage(channel, testEnvironment, ChannelTestActions.DEFAULT_TEST_MESSAGE);
            testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, processingContext);
            return null;
        });
    }

    public static ChannelActionBuilder aMessageWithoutPayloadAndErrorPayloadIsSend() {
        return anAction((channel, testEnvironment) -> {
            final EventType eventType = ChannelTestActions.DEFAULT_EVENT_TYPE;
            final ProcessingContext<TestMessage> processingContext = processingContextForPayloadAndError(
                    eventType, null, null);
            testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, processingContext);
            final ChannelTestActions testActions = channelTestActions(channel);
            sendProcessingContext(testActions, testEnvironment, processingContext);
            return null;
        });
    }

    public static ChannelActionBuilder severalMessagesAreSendAsynchronously(final int numberOfMessages) {
        return anAction((channel, testEnvironment) -> {
            final ChannelTestActions sendingActions = channelTestActions(channel);
            sendValidMessagesAsynchronouslyNew(sendingActions, testEnvironment, numberOfMessages, 1, false);
            return null;
        });
    }

    public static ChannelActionBuilder severalMessagesAreSendAsynchronouslyThatWillBeBlocked(final int numberOfMessages) {
        return anAction((channel, testEnvironment) -> {
            final ChannelTestActions sutActions = channelTestActions(channel);
            final int expectedNumberOfBlockedThreads = determineExpectedNumberOfBlockedThreads(numberOfMessages, testEnvironment);
            addABlockingSubscriberAndThenSendXMessagesInEachThread(sutActions, numberOfMessages, expectedNumberOfBlockedThreads, testEnvironment);
            return null;
        });
    }

    public static ChannelActionBuilder severalMessagesAreSendAsynchronouslyBeforeTheChannelIsClosedWithoutFinishingRemainingTasks(
            final int numberOfMessages) {
        return anAction((channel, testEnvironment) -> {
            final ChannelTestActions testActions = channelTestActions(channel);
            final int expectedBlockedThreads = determineExpectedNumberOfBlockedThreads(numberOfMessages, testEnvironment);
            sendMessagesBeforeShutdownAsynchronously(testActions, testEnvironment, numberOfMessages, false, expectedBlockedThreads);
            return null;
        });
    }

    private static int determineExpectedNumberOfBlockedThreads(final int numberOfMessages,
                                                               final TestEnvironment testEnvironment) {
        final int expectedBlockedThreads;
        if (testEnvironment.getPropertyAsType(IS_ASYNCHRONOUS, Boolean.class)) {
            expectedBlockedThreads = ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE;
        } else {
            expectedBlockedThreads = numberOfMessages;
        }
        return expectedBlockedThreads;
    }

    public static ChannelActionBuilder sendMessagesBeforeTheShutdownIsAwaitedWithoutFinishingTasks(final int numberOfMessages) {
        return anAction((channel, testEnvironment) -> {
            final ChannelTestActions testActions = channelTestActions(channel);
            callCloseThenAwaitWithBlockedSubscriberWithoutReleasingLock(testActions, testEnvironment, numberOfMessages,
                    ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE);
            return null;
        });
    }

    public static ChannelActionBuilder aCallToTheSecondChannelIsExecuted() {
        return anAction((channel, testEnvironment) -> {
            final Channel<TestMessage> callTargetChannel = getCallTargetChannel(testEnvironment);
            ChannelTestActions.addFilterExecutingACall(channel, callTargetChannel);

            final ProcessingContext<TestMessage> sendProcessingFrame = sendMessage(channel, testEnvironment, ChannelTestActions.DEFAULT_TEST_MESSAGE);
            testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, sendProcessingFrame);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private static Channel<TestMessage> getCallTargetChannel(final TestEnvironment testEnvironment) {
        return (Channel<TestMessage>) testEnvironment.getProperty(ChannelTestProperties.CALL_TARGET_CHANNEL);
    }

    public static ChannelActionBuilder severalPreFilterOnDifferentPositionAreAdded() {
        final int[] positions = new int[]{0, 1, 0, 0, 3, 2};
        return addSeveralFilter(positions, PRE);
    }

    public static ChannelActionBuilder severalProcessFilterOnDifferentPositionAreAdded() {
        final int[] positions = new int[]{0, 0, 1, 0, 2, 4, 4};
        return addSeveralFilter(positions, PROCESS);
    }

    public static ChannelActionBuilder severalPostFilterOnDifferentPositionAreAdded() {
        final int[] positions = new int[]{0, 1, 2, 3, 4, 5, 6};
        return addSeveralFilter(positions, POST);
    }

    private static ChannelActionBuilder addSeveralFilter(final int[] positions,
                                                         final FilterPosition filterPosition) {
        return anAction((channel, testEnvironment) -> {
            final ChannelTestActions testActions1 = channelTestActions(channel);
            final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = addSeveralNoopFilter(testActions1, positions, filterPosition);
            testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, expectedFilter);
            testEnvironment.setPropertyIfNotSet(FILTER_POSITION, filterPosition);
            return null;
        });
    }

    public static ChannelActionBuilder theFilterAreQueried() {
        return anAction((channel, testEnvironment) -> {
            final ChannelTestActions testActions = channelTestActions(channel);
            final List<?> filter = queryFilter(testActions, testEnvironment);
            testEnvironment.setPropertyIfNotSet(RESULT, filter);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    public static ChannelActionBuilder oneFilterIsRemoved() {
        return anAction((channel, testEnvironment) -> {
            final ChannelTestActions testActions = channelTestActions(channel);
            removeAFilter(testActions, testEnvironment);
            return null;
        });
    }

    public static ChannelActionBuilder whenTheMetaDataIsModified() {
        return anAction((channel, testEnvironment) -> {
            final String changedMetaDatum = "changed";
            ChannelTestActions.addAFilterChangingMetaData(channel, changedMetaDatum);
            testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, changedMetaDatum);
            sendMessage(channel, testEnvironment, messageOfInterest());
            return null;
        });
    }

    public static ChannelActionBuilder theNumberOfAcceptedMessagesIsQueried() {
        return anAction((channel, testEnvironment) -> {
            final Object expectedResult = testEnvironment.getProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND);
            pollUntilEquals(() -> ChannelTestActions.queryChannelStatistics(channel, ChannelStatistics::getAcceptedMessages), expectedResult);
            final long result = ChannelTestActions.queryChannelStatistics(channel, ChannelStatistics::getAcceptedMessages);
            testEnvironment.setPropertyIfNotSet(RESULT, result);
            return null;
        });
    }

    public static ChannelActionBuilder theNumberOfQueuedMessagesIsQueried() {
        return anAction((channel, testEnvironment) -> {
            final int messagesSend = testEnvironment.getPropertyAsType(NUMBER_OF_MESSAGES_SHOULD_BE_SEND, Integer.class);
            final int expectedResult = determineNumberOfExpectedQueuedMessages(testEnvironment, messagesSend);
            pollUntilEquals(() -> ChannelTestActions.queryChannelStatistics(channel, ChannelStatistics::getQueuedMessages), expectedResult);
            final long result = ChannelTestActions.queryChannelStatistics(channel, ChannelStatistics::getQueuedMessages);
            testEnvironment.setPropertyIfNotSet(RESULT, result);
            return null;
        });
    }

    private static int determineNumberOfExpectedQueuedMessages(final TestEnvironment testEnvironment, final int messagesSend) {
        final int expectedResult;
        if (testEnvironment.getPropertyAsType(IS_ASYNCHRONOUS, Boolean.class)) {
            expectedResult = messagesSend - ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE;
        } else {
            expectedResult = 0;
        }
        return expectedResult;
    }

    public static ChannelActionBuilder theNumberOfBlockedMessagesIsQueried() {
        return anAction((channel, testEnvironment) -> {
            final Object expectedResult = testEnvironment.getProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND);
            pollUntilEquals(() -> ChannelTestActions.queryChannelStatistics(channel, ChannelStatistics::getBlockedMessages), expectedResult);
            final long result = ChannelTestActions.queryChannelStatistics(channel, ChannelStatistics::getBlockedMessages);
            testEnvironment.setPropertyIfNotSet(RESULT, result);
            return null;
        });
    }

    public static ChannelActionBuilder theNumberOfForgottenMessagesIsQueried() {
        return anAction((channel, testEnvironment) -> {
            final Object expectedResult = testEnvironment.getProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND);
            pollUntilEquals(() -> ChannelTestActions.queryChannelStatistics(channel, ChannelStatistics::getForgottenMessages), expectedResult);
            final long result = ChannelTestActions.queryChannelStatistics(channel, ChannelStatistics::getForgottenMessages);
            testEnvironment.setPropertyIfNotSet(RESULT, result);
            return null;
        });
    }

    public static ChannelActionBuilder theNumberOfSuccessfulDeliveredMessagesIsQueried() {
        return anAction((channel, testEnvironment) -> {
            final Object expectedResult = testEnvironment.getProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND);
            pollUntilEquals(() -> ChannelTestActions.queryChannelStatistics(channel, ChannelStatistics::getSuccessfulMessages), expectedResult);
            final long result = ChannelTestActions.queryChannelStatistics(channel, ChannelStatistics::getSuccessfulMessages);
            testEnvironment.setPropertyIfNotSet(RESULT, result);
            return null;
        });
    }

    public static ChannelActionBuilder theNumberOfMessagesIsQueriedThatAreStillDeliveredSuccessfully() {
        return anAction((channel, testEnvironment) -> {
            final Integer messagesSend = testEnvironment.getPropertyAsType(NUMBER_OF_MESSAGES_SHOULD_BE_SEND, Integer.class);
            final int expectedResult = determineExpectedNumberOfBlockedThreads(messagesSend, testEnvironment);
            pollUntilEquals(() -> ChannelTestActions.queryChannelStatistics(channel, ChannelStatistics::getSuccessfulMessages), expectedResult);
            final long result = ChannelTestActions.queryChannelStatistics(channel, ChannelStatistics::getSuccessfulMessages);
            testEnvironment.setPropertyIfNotSet(RESULT, result);
            return null;
        });
    }

    public static ChannelActionBuilder theNumberOfFailedDeliveredMessagesIsQueried() {
        return anAction((channel, testEnvironment) -> {
            final Object expectedResult = testEnvironment.getProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND);
            pollUntilEquals(() -> ChannelTestActions.queryChannelStatistics(channel, ChannelStatistics::getFailedMessages), expectedResult);
            final long result = ChannelTestActions.queryChannelStatistics(channel, ChannelStatistics::getFailedMessages);
            testEnvironment.setPropertyIfNotSet(RESULT, result);
            return null;
        });
    }

    public static ChannelActionBuilder severalSubscriberAreAdded() {
        return anAction((channel, testEnvironment) -> {
            final int numberOfSubscribers = 5;
            final ChannelTestActions testActions = channelTestActions(channel);
            addSeveralSubscriber(testActions, testEnvironment, numberOfSubscribers);
            return null;
        });
    }

    public static ChannelActionBuilder severalSubscriberWithAccessToProcessingContextAreAdded() {
        return anAction((channel, testEnvironment) -> {
            final int numberOfSubscribers = 5;
            final ChannelTestActions channelTestActions = channelTestActions(channel);
            addSeveralRawSubscriber(channelTestActions, testEnvironment, numberOfSubscribers);
            return null;
        });
    }

    public static ChannelActionBuilder oneSubscriberIsRemoved() {
        return anAction((channel, testEnvironment) -> {
            final List<TestSubscriber<TestMessage>> currentReceiver = getExpectedReceivers(testEnvironment);
            final TestSubscriber<TestMessage> subscriberToRemove = currentReceiver.remove(0);
            final ChannelTestActions testActions1 = channelTestActions(channel);
            SubscriptionTestUtils.unsubscribe(testActions1, subscriberToRemove);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private static List<TestSubscriber<TestMessage>> getExpectedReceivers(final TestEnvironment testEnvironment) {
        return (List<TestSubscriber<TestMessage>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
    }

    public static ChannelActionBuilder theChannelIsClosedSeveralTimes() {
        return anAction((channel, testEnvironment) -> {
            final ChannelTestActions sutActions = channelTestActions(channel);
            final int shutdownCalls = 5;
            shutdownTheSutAsynchronouslyXTimes(sutActions, shutdownCalls);
            return null;
        });
    }

    public static ChannelActionBuilder theChannelIsClosedAndTheShutdownIsAwaited() {
        return anAction((channel, testEnvironment) -> {
            final ChannelTestActions sutActions = channelTestActions(channel);
            closeAndThenAwaitTermination(sutActions, testEnvironment);
            return null;
        });
    }

    public static ChannelActionBuilder theShutdownIsAwaited() {
        return anAction((channel, testEnvironment) -> {
            final ChannelTestActions testActions = channelTestActions(channel);
            awaitTermination(testActions, testEnvironment);
            return null;
        });
    }

    public List<TestAction<Channel<TestMessage>>> build() {
        return testActions;
    }

    public ChannelActionBuilder andThen(final ChannelActionBuilder followUpBuilder) {
        this.testActions.addAll(followUpBuilder.testActions);
        return this;
    }
}
