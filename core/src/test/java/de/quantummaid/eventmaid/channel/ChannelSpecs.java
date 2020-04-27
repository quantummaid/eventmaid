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

package de.quantummaid.eventmaid.channel;

import de.quantummaid.eventmaid.channel.action.CallNotAllowedAsFinalChannelAction;
import de.quantummaid.eventmaid.channel.action.NoHandlerForUnknownActionException;
import de.quantummaid.eventmaid.channel.action.ReturnWithoutCallException;
import de.quantummaid.eventmaid.channel.config.ChannelTestConfig;
import de.quantummaid.eventmaid.channel.givenwhenthen.ChannelSetupBuilder;
import de.quantummaid.eventmaid.shared.exceptions.TestException;
import org.junit.jupiter.api.Test;

import static de.quantummaid.eventmaid.channel.givenwhenthen.ChannelActionBuilder.*;
import static de.quantummaid.eventmaid.channel.givenwhenthen.ChannelSetupBuilder.aConfiguredChannel;
import static de.quantummaid.eventmaid.channel.givenwhenthen.ChannelValidationBuilder.*;
import static de.quantummaid.eventmaid.channel.givenwhenthen.Given.given;

public interface ChannelSpecs {

    //actions
    //actions: consume
    @Test
    default void testChannel_canConsumeMessage(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withDefaultActionConsume())
                .when(aMessageIsSend())
                .then(expectTheMessageToBeConsumed());
    }

    //actions: jump
    @Test
    default void testChannel_letMessagesJumpToDifferentChannel(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withDefaultActionJumpToDifferentChannel())
                .when(aMessageIsSend())
                .then(expectTheMessageToBeConsumedByTheSecondChannel());
    }

    @Test
    default void testChannel_continuesHistoryWhenChannelsAreChanged(final ChannelTestConfig channelTestConfig) {
        given(ChannelSetupBuilder.threeChannelsConnectedWithJumps(channelTestConfig))
                .when(aMessageIsSend())
                .then(expectAllChannelsToBeContainedInTheHistory());
    }

    //actions: call and return
    @Test
    default void testChannel_canReturnFromACall(final ChannelTestConfig channelTestConfig) {
        given(ChannelSetupBuilder.aChannelCallingASecondThatReturnsBack(channelTestConfig))
                .when(aCallToTheSecondChannelIsExecuted())
                .then(expectTheMessageToHaveReturnedSuccessfully());
    }

    @Test
    default void testChannel_canExecuteNestedCalls(final ChannelTestConfig channelTestConfig) {
        given(ChannelSetupBuilder.aChannelSetupWithNestedCalls(channelTestConfig))
                .when(aMessageIsSend())
                .then(expectTheMessageToHaveReturnedFromAllCalls());
    }

    @Test
    default void testChannel_failsForReturnWithoutACall(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withDefaultActionReturn()
                .withAnExceptionCatchingHandler())
                .when(aMessageIsSend())
                .then(expectADeliveryExceptionOfType(ReturnWithoutCallException.class));
    }

    @Test
    default void testChannel_failsForCallAsFinalAction(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withDefaultActionCall()
                .withAnExceptionCatchingHandler())
                .when(aMessageIsSend())
                .then(expectADeliveryExceptionOfType(CallNotAllowedAsFinalChannelAction.class));
    }

    @Test
    default void testChannel_failsForUnknownAction(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withAnUnknownAction()
                .withAnExceptionCatchingHandler())
                .when(aMessageIsSend())
                .then(expectAExceptionOfType(NoHandlerForUnknownActionException.class));
    }

    //actions: subscription
    @Test
    default void testChannel_subscriptionActionSendsToAllSubscriber(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withSubscriptionAsAction())
                .when(severalSubscriberAreAdded()
                        .andThen(aMessageIsSend()))
                .then(expectTheMessageToBeReceivedByAllSubscriber());
    }

    @Test
    default void testChannel_subscriptionCanUnsubscribeSubscriber(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withSubscriptionAsAction())
                .when(severalSubscriberAreAdded()
                        .andThen(oneSubscriberIsRemoved())
                        .andThen(aMessageIsSend()))
                .then(expectRemainingSubscriber()
                        .and(expectTheMessageToBeReceivedByAllRemainingSubscriber()));
    }

    @Test
    default void testChannel_subscriptionCanStopDeliveryEarly(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withOnPreemptiveSubscriberAndOneSubscriberThatShouldNeverBeCalled())
                .when(aMessageIsSend())
                .then(expectOnlyTheFirstSubscriberToBeCalled());
    }

    @Test
    default void testChannel_subscriptionActionGetsAccessToProcessingContextObject(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withSubscriptionAsAction())
                .when(severalSubscriberWithAccessToProcessingContextAreAdded()
                        .andThen(aProcessingContextObjectIsSend()))
                .then(expectTheProcessingContextObjectToBeReceivedByAllSubscriber());
    }

    //send
    @Test
    default void testChannel_canSendNull(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withDefaultActionConsume())
                .when(aMessageWithoutPayloadIsSend())
                .then(expectTheMessageToBeConsumed());
    }

    @Test
    default void testChannel_canSendBothNormalAndErrorPayload(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withDefaultActionConsume())
                .when(aMessageWithoutPayloadAndErrorPayloadIsSend())
                .then(expectTheMessageToBeConsumed());
    }

    //filter
    @Test
    default void testChannel_allowsFilterToChangeAction_forPreFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withAPreFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }

    @Test
    default void testChannel_allowsFilterToBlockMessage_forPreFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withAPreFilterThatBlocksMessages())
                .when(aMessageIsSend())
                .then(expectNoMessageToBeDelivered());
    }

    @Test
    default void testChannel_dropsMessageWhenMessageIsForgotten_forPreFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withAPreFilterThatForgetsMessages())
                .when(aMessageIsSend())
                .then(expectNoMessageToBeDelivered());
    }

    @Test
    default void testChannel_allowsAddingFilterWithPosition_forPreFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig))
                .when(severalPreFilterOnDifferentPositionAreAdded())
                .then(expectAllFilterToBeInCorrectOrderInChannel());
    }

    @Test
    default void testChannel_canQueryFilter_forPreFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withSeveralPreFilter())
                .when(theFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    default void testChannel_canRemoveAFilter_forPreFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withSeveralPreFilter())
                .when(oneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    default void testChannel_throwsExceptionForPositionBelowZero_forPreFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withAPreFilterAtAnInvalidPosition(-1))
                .when(aMessageIsSend())
                .then(expectTheExceptionClass(IndexOutOfBoundsException.class));
    }

    @Test
    default void testChannel_throwsExceptionForPositionGreaterThanAllowed_forPreFilter(final ChannelTestConfig config) {
        given(aConfiguredChannel(config)
                .withAPreFilterAtAnInvalidPosition(100))
                .when(aMessageIsSend())
                .then(expectTheExceptionClass(IndexOutOfBoundsException.class));
    }

    @Test
    default void testChannel_allowsFilterToChangeAction_forProcessFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withAProcessFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }

    @Test
    default void testChannel_allowsFilterToBlockMessage_forProcessFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withAProcessFilterThatBlocksMessages())
                .when(aMessageIsSend())
                .then(expectNoMessageToBeDelivered());
    }

    @Test
    default void testChannel_dropsMessageWhenMessageIsForgotten_forProcessFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withAProcessFilterThatForgetsMessages())
                .when(aMessageIsSend())
                .then(expectNoMessageToBeDelivered());
    }

    @Test
    default void testChannel_allowsAddingFilterWithPosition_forProcessFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig))
                .when(severalProcessFilterOnDifferentPositionAreAdded())
                .then(expectAllFilterToBeInCorrectOrderInChannel());
    }

    @Test
    default void testChannel_canQueryFilter_forProcessFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withSeveralProcessFilter())
                .when(theFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    default void testChannel_canRemoveAFilter_forProcessFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withSeveralProcessFilter())
                .when(oneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    default void testChannel_throwsExceptionForPositionBelowZero_forProcessFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withAProcessFilterAtAnInvalidPosition(-1))
                .when(aMessageIsSend())
                .then(expectTheExceptionClass(IndexOutOfBoundsException.class));
    }

    @Test
    default void testChannel_throwsExceptionForPositionGreaterThanAllowed_forProcessFilter(final ChannelTestConfig config) {
        given(aConfiguredChannel(config)
                .withAProcessFilterAtAnInvalidPosition(100))
                .when(aMessageIsSend())
                .then(expectTheExceptionClass(IndexOutOfBoundsException.class));
    }

    @Test
    default void testChannel_allowsFilterToChangeAction_forPostFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withAPostFilterThatChangesTheAction())
                .when(aMessageIsSend())
                .then(expectTheChangedActionToBeExecuted());
    }

    @Test
    default void testChannel_allowsFilterToBlockMessage_forPostFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withAPostFilterThatBlocksMessages())
                .when(aMessageIsSend())
                .then(expectNoMessageToBeDelivered());
    }

    @Test
    default void testChannel_dropsMessageWhenMessageIsForgotten_forPostFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withAPostFilterThatForgetsMessages())
                .when(aMessageIsSend())
                .then(expectNoMessageToBeDelivered());
    }

    @Test
    default void testChannel_allowsAddingFilterWithPosition_forPostFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig))
                .when(severalPostFilterOnDifferentPositionAreAdded())
                .then(expectAllFilterToBeInCorrectOrderInChannel());
    }

    @Test
    default void testChannel_canQueryFilter_forPostFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withSeveralPostFilter())
                .when(theFilterAreQueried())
                .then(expectTheFilterInOrderAsAdded());
    }

    @Test
    default void testChannel_canRemoveAFilter_forPostFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withSeveralPostFilter())
                .when(oneFilterIsRemoved())
                .then(expectTheAllRemainingFilter());
    }

    @Test
    default void testChannel_throwsExceptionForPositionBelowZero_forPostFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withAPostFilterAtAnInvalidPosition(-1))
                .when(aMessageIsSend())
                .then(expectTheExceptionClass(IndexOutOfBoundsException.class));
    }

    @Test
    default void testChannel_throwsExceptionForPositionGreaterThanAllowed_forPostFilter(final ChannelTestConfig config) {
        given(aConfiguredChannel(config)
                .withAPostFilterAtAnInvalidPosition(100))
                .when(aMessageIsSend())
                .then(expectTheExceptionClass(IndexOutOfBoundsException.class));
    }

    //correlationId
    @Test
    default void testChannel_sendMessageHasConstantMessageIdAndCanGenerateMatchingCorrelationId(final ChannelTestConfig config) {
        given(aConfiguredChannel(config)
                .withDefaultActionConsume())
                .when(aMessageIsSend())
                .then(expectTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId());
    }

    @Test
    default void testChannel_canSetCorrelationIdWhenSend(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withDefaultActionConsume())
                .when(aMessageWithCorrelationIdIsSend())
                .then(expectTheCorrelationIdToBeSetWhenReceived());
    }

    //metadata
    @Test
    default void testChannel_filterCanModifyMetaData(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withDefaultActionConsume())
                .when(whenTheMetaDataIsModified())
                .then(expectTheMetaDataChangePersist());
    }

    //statistics
    @Test
    default void testChannel_canQueryAcceptedMessages(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withNoopConsumeAsDefaultAction())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfAcceptedMessagesIsQueried()))
                .then(expectTheResult(numberOfSendMessages));
    }

    // queued statistics config dependent

    @Test
    default void testChannel_canQueryBlockedMessages_whenDroppedInPreFilter(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withAPreFilterThatBlocksMessages())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfBlockedMessagesIsQueried()))
                .then(expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQueryBlockedMessages_whenDroppedInProcessFilter(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withAProcessFilterThatBlocksMessages())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfBlockedMessagesIsQueried()))
                .then(expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQueryBlockedMessages_whenDroppedInPostFilter(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withAPostFilterThatBlocksMessages())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfBlockedMessagesIsQueried()))
                .then(expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQueryForgottenMessages_whenForgottenInPreFilter(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withAPreFilterThatForgetsMessages())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfForgottenMessagesIsQueried()))
                .then(expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQueryForgottenMessages_whenForgottenInProcessFilter(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withAProcessFilterThatForgetsMessages())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfForgottenMessagesIsQueried()))
                .then(expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQueryForgottenMessages_whenForgottenInPostFilter(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withAPostFilterThatForgetsMessages())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfForgottenMessagesIsQueried()))
                .then(expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQuerySuccessfulDeliveredMessages(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withNoopConsumeAsDefaultAction())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfSuccessfulDeliveredMessagesIsQueried()))
                .then(expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQueryFailedDeliveredMessages_forErrorInSubscriber(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withAnExceptionInFinalAction()
                .withAnExceptionHandlerIgnoringExceptions())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfFailedDeliveredMessagesIsQueried()))
                .then(expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQueryFailedDeliveredMessages_forErrorInFilter(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withAnExceptionHandlerIgnoringExceptions()
                .withAnErrorThrowingFilter())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfFailedDeliveredMessagesIsQueried()))
                .then(expectTheResult(numberOfSendMessages));
    }

    //error handling
    @Test
    default void testChannel_callsErrorHandler_forErrorInSubscriber(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withAnExceptionInFinalAction()
                .withACustomErrorHandler())
                .when(aMessageIsSend())
                .then(expectTheDeliveryExceptionCatched(TestException.class));
    }

    @Test
    default void testChannel_callsErrorHandler_forErrorInFilter(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig)
                .withACustomErrorHandler()
                .withAnErrorThrowingFilter())
                .when(aMessageIsSend())
                .then(expectTheFilterExceptionCatched(TestException.class));
    }

    @Test
    default void testChannel_errorHandlerCanDeclareExceptionAsIgnoredDuringDelivery(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withAnExceptionInFinalAction()
                .withAnErrorHandlerDeclaringErrorsInDeliveryAsNotDeliveryAborting())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfSuccessfulDeliveredMessagesIsQueried()))
                .then(expectTheResult(numberOfSendMessages));
    }

    //shutdown
    @Test
    default void testChannel_shutdownCallIsIdempotent(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig))
                .when(theChannelIsClosedSeveralTimes())
                .then(expectTheChannelToBeShutdown());
    }

    // close without finishRemainingTasks config dependent

    //await
    @Test
    default void testChannel_awaitsIsSuccessfulWhenAllTasksAreFinished(final ChannelTestConfig channelTestConfig) {
        given(aConfiguredChannel(channelTestConfig))
                .when(theChannelIsClosedAndTheShutdownIsAwaited())
                .then(expectTheShutdownToBeSucceededInTime());
    }

    //provoking await returning false config dependent
}
