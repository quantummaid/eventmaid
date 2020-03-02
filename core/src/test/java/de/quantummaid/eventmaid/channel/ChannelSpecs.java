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

import de.quantummaid.eventmaid.channel.givenWhenThen.ChannelActionBuilder;
import de.quantummaid.eventmaid.channel.givenWhenThen.ChannelSetupBuilder;
import de.quantummaid.eventmaid.channel.givenWhenThen.ChannelValidationBuilder;
import de.quantummaid.eventmaid.channel.givenWhenThen.Given;
import de.quantummaid.eventmaid.shared.exceptions.TestException;
import de.quantummaid.eventmaid.channel.action.CallNotAllowedAsFinalChannelAction;
import de.quantummaid.eventmaid.channel.action.NoHandlerForUnknownActionException;
import de.quantummaid.eventmaid.channel.action.ReturnWithoutCallException;
import de.quantummaid.eventmaid.channel.config.ChannelTestConfig;
import org.junit.jupiter.api.Test;

public interface ChannelSpecs {

    //actions
    //actions: consume
    @Test
    default void testChannel_canConsumeMessage(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withDefaultActionConsume())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheMessageToBeConsumed());
    }

    //actions: jump
    @Test
    default void testChannel_letMessagesJumpToDifferentChannel(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withDefaultActionJumpToDifferentChannel())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheMessageToBeConsumedByTheSecondChannel());
    }

    @Test
    default void testChannel_continuesHistoryWhenChannelsAreChanged(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.threeChannelsConnectedWithJumps(channelTestConfig))
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectAllChannelsToBeContainedInTheHistory());
    }

    //actions: call and return
    @Test
    default void testChannel_canReturnFromACall(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aChannelCallingASecondThatReturnsBack(channelTestConfig))
                .when(ChannelActionBuilder.aCallToTheSecondChannelIsExecuted())
                .then(ChannelValidationBuilder.expectTheMessageToHaveReturnedSuccessfully());
    }

    @Test
    default void testChannel_canExecuteNestedCalls(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aChannelSetupWithNestedCalls(channelTestConfig))
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheMessageToHaveReturnedFromAllCalls());
    }

    @Test
    default void testChannel_failsForReturnWithoutACall(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withDefaultActionReturn()
                .withAnExceptionCatchingHandler())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectADeliveryExceptionOfType(ReturnWithoutCallException.class));
    }

    @Test
    default void testChannel_failsForCallAsFinalAction(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withDefaultActionCall()
                .withAnExceptionCatchingHandler())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectADeliveryExceptionOfType(CallNotAllowedAsFinalChannelAction.class));
    }

    @Test
    default void testChannel_failsForUnknownAction(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAnUnknownAction()
                .withAnExceptionCatchingHandler())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectAExceptionOfType(NoHandlerForUnknownActionException.class));
    }

    //actions: subscription
    @Test
    default void testChannel_subscriptionActionSendsToAllSubscriber(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withSubscriptionAsAction())
                .when(ChannelActionBuilder.severalSubscriberAreAdded()
                        .andThen(ChannelActionBuilder.aMessageIsSend()))
                .then(ChannelValidationBuilder.expectTheMessageToBeReceivedByAllSubscriber());
    }

    @Test
    default void testChannel_subscriptionCanUnsubscribeSubscriber(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withSubscriptionAsAction())
                .when(ChannelActionBuilder.severalSubscriberAreAdded()
                        .andThen(ChannelActionBuilder.oneSubscriberIsRemoved())
                        .andThen(ChannelActionBuilder.aMessageIsSend()))
                .then(ChannelValidationBuilder.expectRemainingSubscriber()
                        .and(ChannelValidationBuilder.expectTheMessageToBeReceivedByAllRemainingSubscriber()));
    }

    @Test
    default void testChannel_subscriptionCanStopDeliveryEarly(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withOnPreemptiveSubscriberAndOneSubscriberThatShouldNeverBeCalled())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectOnlyTheFirstSubscriberToBeCalled());
    }

    @Test
    default void testChannel_subscriptionActionGetsAccessToProcessingContextObject(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withSubscriptionAsAction())
                .when(ChannelActionBuilder.severalSubscriberWithAccessToProcessingContextAreAdded()
                        .andThen(ChannelActionBuilder.aProcessingContextObjectIsSend()))
                .then(ChannelValidationBuilder.expectTheProcessingContextObjectToBeReceivedByAllSubscriber());
    }

    //send
    @Test
    default void testChannel_canSendNull(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withDefaultActionConsume())
                .when(ChannelActionBuilder.aMessageWithoutPayloadIsSend())
                .then(ChannelValidationBuilder.expectTheMessageToBeConsumed());
    }

    @Test
    default void testChannel_canSendBothNormalAndErrorPayload(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withDefaultActionConsume())
                .when(ChannelActionBuilder.aMessageWithoutPayloadAndErrorPayloadIsSend())
                .then(ChannelValidationBuilder.expectTheMessageToBeConsumed());
    }

    //filter
    @Test
    default void testChannel_allowsFilterToChangeAction_forPreFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAPreFilterThatChangesTheAction())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheChangedActionToBeExecuted());
    }

    @Test
    default void testChannel_allowsFilterToBlockMessage_forPreFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAPreFilterThatBlocksMessages())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectNoMessageToBeDelivered());
    }

    @Test
    default void testChannel_dropsMessageWhenMessageIsForgotten_forPreFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAPreFilterThatForgetsMessages())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectNoMessageToBeDelivered());
    }

    @Test
    default void testChannel_allowsAddingFilterWithPosition_forPreFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig))
                .when(ChannelActionBuilder.severalPreFilterOnDifferentPositionAreAdded())
                .then(ChannelValidationBuilder.expectAllFilterToBeInCorrectOrderInChannel());
    }

    @Test
    default void testChannel_canQueryFilter_forPreFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withSeveralPreFilter())
                .when(ChannelActionBuilder.theFilterAreQueried())
                .then(ChannelValidationBuilder.expectTheFilterInOrderAsAdded());
    }

    @Test
    default void testChannel_canRemoveAFilter_forPreFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withSeveralPreFilter())
                .when(ChannelActionBuilder.oneFilterIsRemoved())
                .then(ChannelValidationBuilder.expectTheAllRemainingFilter());
    }

    @Test
    default void testChannel_throwsExceptionForPositionBelowZero_forPreFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAPreFilterAtAnInvalidPosition(-1))
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testChannel_throwsExceptionForPositionGreaterThanAllowed_forPreFilter(final ChannelTestConfig config) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(config)
                .withAPreFilterAtAnInvalidPosition(100))
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testChannel_allowsFilterToChangeAction_forProcessFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAProcessFilterThatChangesTheAction())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheChangedActionToBeExecuted());
    }

    @Test
    default void testChannel_allowsFilterToBlockMessage_forProcessFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAProcessFilterThatBlocksMessages())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectNoMessageToBeDelivered());
    }

    @Test
    default void testChannel_dropsMessageWhenMessageIsForgotten_forProcessFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAProcessFilterThatForgetsMessages())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectNoMessageToBeDelivered());
    }

    @Test
    default void testChannel_allowsAddingFilterWithPosition_forProcessFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig))
                .when(ChannelActionBuilder.severalProcessFilterOnDifferentPositionAreAdded())
                .then(ChannelValidationBuilder.expectAllFilterToBeInCorrectOrderInChannel());
    }

    @Test
    default void testChannel_canQueryFilter_forProcessFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withSeveralProcessFilter())
                .when(ChannelActionBuilder.theFilterAreQueried())
                .then(ChannelValidationBuilder.expectTheFilterInOrderAsAdded());
    }

    @Test
    default void testChannel_canRemoveAFilter_forProcessFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withSeveralProcessFilter())
                .when(ChannelActionBuilder.oneFilterIsRemoved())
                .then(ChannelValidationBuilder.expectTheAllRemainingFilter());
    }

    @Test
    default void testChannel_throwsExceptionForPositionBelowZero_forProcessFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAProcessFilterAtAnInvalidPosition(-1))
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testChannel_throwsExceptionForPositionGreaterThanAllowed_forProcessFilter(final ChannelTestConfig config) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(config)
                .withAProcessFilterAtAnInvalidPosition(100))
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testChannel_allowsFilterToChangeAction_forPostFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAPostFilterThatChangesTheAction())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheChangedActionToBeExecuted());
    }

    @Test
    default void testChannel_allowsFilterToBlockMessage_forPostFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAPostFilterThatBlocksMessages())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectNoMessageToBeDelivered());
    }

    @Test
    default void testChannel_dropsMessageWhenMessageIsForgotten_forPostFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAPostFilterThatForgetsMessages())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectNoMessageToBeDelivered());
    }

    @Test
    default void testChannel_allowsAddingFilterWithPosition_forPostFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig))
                .when(ChannelActionBuilder.severalPostFilterOnDifferentPositionAreAdded())
                .then(ChannelValidationBuilder.expectAllFilterToBeInCorrectOrderInChannel());
    }

    @Test
    default void testChannel_canQueryFilter_forPostFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withSeveralPostFilter())
                .when(ChannelActionBuilder.theFilterAreQueried())
                .then(ChannelValidationBuilder.expectTheFilterInOrderAsAdded());
    }

    @Test
    default void testChannel_canRemoveAFilter_forPostFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withSeveralPostFilter())
                .when(ChannelActionBuilder.oneFilterIsRemoved())
                .then(ChannelValidationBuilder.expectTheAllRemainingFilter());
    }

    @Test
    default void testChannel_throwsExceptionForPositionBelowZero_forPostFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAPostFilterAtAnInvalidPosition(-1))
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testChannel_throwsExceptionForPositionGreaterThanAllowed_forPostFilter(final ChannelTestConfig config) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(config)
                .withAPostFilterAtAnInvalidPosition(100))
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheException(IndexOutOfBoundsException.class));
    }

    //correlationId
    @Test
    default void testChannel_sendMessageHasConstantMessageIdAndCanGenerateMatchingCorrelationId(final ChannelTestConfig config) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(config)
                .withDefaultActionConsume())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId());
    }

    @Test
    default void testChannel_canSetCorrelationIdWhenSend(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withDefaultActionConsume())
                .when(ChannelActionBuilder.aMessageWithCorrelationIdIsSend())
                .then(ChannelValidationBuilder.expectTheCorrelationIdToBeSetWhenReceived());
    }

    //metadata
    @Test
    default void testChannel_filterCanModifyMetaData(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withDefaultActionConsume())
                .when(ChannelActionBuilder.whenTheMetaDataIsModified())
                .then(ChannelValidationBuilder.expectTheMetaDataChangePersist());
    }

    //statistics
    @Test
    default void testChannel_canQueryAcceptedMessages(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withNoopConsumeAsDefaultAction())
                .when(ChannelActionBuilder.severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(ChannelActionBuilder.theNumberOfAcceptedMessagesIsQueried()))
                .then(ChannelValidationBuilder.expectTheResult(numberOfSendMessages));
    }

    // queued statistics config dependent

    @Test
    default void testChannel_canQueryBlockedMessages_whenDroppedInPreFilter(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAPreFilterThatBlocksMessages())
                .when(ChannelActionBuilder.severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(ChannelActionBuilder.theNumberOfBlockedMessagesIsQueried()))
                .then(ChannelValidationBuilder.expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQueryBlockedMessages_whenDroppedInProcessFilter(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAProcessFilterThatBlocksMessages())
                .when(ChannelActionBuilder.severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(ChannelActionBuilder.theNumberOfBlockedMessagesIsQueried()))
                .then(ChannelValidationBuilder.expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQueryBlockedMessages_whenDroppedInPostFilter(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAPostFilterThatBlocksMessages())
                .when(ChannelActionBuilder.severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(ChannelActionBuilder.theNumberOfBlockedMessagesIsQueried()))
                .then(ChannelValidationBuilder.expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQueryForgottenMessages_whenForgottenInPreFilter(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAPreFilterThatForgetsMessages())
                .when(ChannelActionBuilder.severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(ChannelActionBuilder.theNumberOfForgottenMessagesIsQueried()))
                .then(ChannelValidationBuilder.expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQueryForgottenMessages_whenForgottenInProcessFilter(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAProcessFilterThatForgetsMessages())
                .when(ChannelActionBuilder.severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(ChannelActionBuilder.theNumberOfForgottenMessagesIsQueried()))
                .then(ChannelValidationBuilder.expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQueryForgottenMessages_whenForgottenInPostFilter(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAPostFilterThatForgetsMessages())
                .when(ChannelActionBuilder.severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(ChannelActionBuilder.theNumberOfForgottenMessagesIsQueried()))
                .then(ChannelValidationBuilder.expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQuerySuccessfulDeliveredMessages(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withNoopConsumeAsDefaultAction())
                .when(ChannelActionBuilder.severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(ChannelActionBuilder.theNumberOfSuccessfulDeliveredMessagesIsQueried()))
                .then(ChannelValidationBuilder.expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQueryFailedDeliveredMessages_forErrorInSubscriber(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAnExceptionInFinalAction()
                .withAnExceptionHandlerIgnoringExceptions())
                .when(ChannelActionBuilder.severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(ChannelActionBuilder.theNumberOfFailedDeliveredMessagesIsQueried()))
                .then(ChannelValidationBuilder.expectTheResult(numberOfSendMessages));
    }

    @Test
    default void testChannel_canQueryFailedDeliveredMessages_forErrorInFilter(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAnExceptionHandlerIgnoringExceptions()
                .withAnErrorThrowingFilter())
                .when(ChannelActionBuilder.severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(ChannelActionBuilder.theNumberOfFailedDeliveredMessagesIsQueried()))
                .then(ChannelValidationBuilder.expectTheResult(numberOfSendMessages));
    }

    //errors
    @Test
    default void testChannel_callsErrorHandler_forErrorInSubscriber(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAnExceptionInFinalAction()
                .withACustomErrorHandler())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheDeliveryExceptionCatched(TestException.class));
    }

    @Test
    default void testChannel_callsErrorHandler_forErrorInFilter(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withACustomErrorHandler()
                .withAnErrorThrowingFilter())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectTheFilterExceptionCatched(TestException.class));
    }

    @Test
    default void testChannel_errorHandlerCanDeclareExceptionAsIgnoredDuringDelivery(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAnExceptionInFinalAction()
                .withAnErrorHandlerDeclaringErrorsInDeliveryAsNotDeliveryAborting())
                .when(ChannelActionBuilder.severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(ChannelActionBuilder.theNumberOfSuccessfulDeliveredMessagesIsQueried()))
                .then(ChannelValidationBuilder.expectTheResult(numberOfSendMessages));
    }

    //shutdown
    @Test
    default void testChannel_shutdownCallIsIdempotent(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig))
                .when(ChannelActionBuilder.theChannelIsClosedSeveralTimes())
                .then(ChannelValidationBuilder.expectTheChannelToBeShutdown());
    }

    // close without finishRemainingTasks config dependent

    //await
    @Test
    default void testChannel_awaitsIsSuccessfulWhenAllTasksAreFinished(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig))
                .when(ChannelActionBuilder.theChannelIsClosedAndTheShutdownIsAwaited())
                .then(ChannelValidationBuilder.expectTheShutdownToBeSucceededInTime());
    }

    //provoking await returning false config dependent
}
