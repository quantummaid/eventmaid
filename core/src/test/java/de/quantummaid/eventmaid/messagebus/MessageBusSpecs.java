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

package de.quantummaid.eventmaid.messagebus;

import de.quantummaid.eventmaid.exceptions.AlreadyClosedException;
import de.quantummaid.eventmaid.internal.enforcing.MustNotBeNullException;
import de.quantummaid.eventmaid.messagebus.config.MessageBusTestConfig;
import de.quantummaid.eventmaid.messagebus.givenwhenthen.Given;
import de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusActionBuilder;
import de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusSetupBuilder;
import de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusValidationBuilder;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.shared.exceptions.TestException;
import de.quantummaid.eventmaid.shared.testmessages.TestMessageOfInterest;
import org.junit.jupiter.api.Test;

import static de.quantummaid.eventmaid.shared.eventtype.TestEventType.testEventType;
import static de.quantummaid.eventmaid.shared.testmessages.TestMessageOfInterest.messageOfInterest;

public interface MessageBusSpecs {

    //Send and subscribe
    @Test
    default void testMessageBus_canSendAndReceiveASingleMessage(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASingleSubscriber())
                .when(MessageBusActionBuilder.aSingleMessageIsSend())
                .then(MessageBusValidationBuilder.expectTheMessageToBeReceived());
    }

    @Test
    default void testMessageBus_canSendAndReceiveSeveralMessagesWithSeveralSubscriber(
            final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withSeveralSubscriber(5))
                .when(MessageBusActionBuilder.severalMessagesAreSend(10))
                .then(MessageBusValidationBuilder.expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testMessageBus_canSendMessageTwice(final MessageBusTestConfig config) {
        final TestMessageOfInterest message = messageOfInterest();
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASingleSubscriber())
                .when(MessageBusActionBuilder.theMessageIsSend(message)
                        .andThen(MessageBusActionBuilder.theMessageIsSend(message)))
                .then(MessageBusValidationBuilder.expectTheMessagesToBeReceivedByAllSubscriber(message, message));
    }

    @Test
    default void testMessageBus_canSendAndReceiveMessagesAsynchronously(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withSeveralSubscriber(5))
                .when(MessageBusActionBuilder.severalMessagesAreSendAsynchronously(5, 10))
                .then(MessageBusValidationBuilder.expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testMessageBus_canSendAMessageWithoutPayload(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASingleRawSubscriber())
                .when(MessageBusActionBuilder.aMessageWithoutPayloadIsSend())
                .then(MessageBusValidationBuilder.expectTheMessageToBeReceived());
    }

    @Test
    default void testMessageBus_throwsExceptionWhenEventTypeIsNotSet(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config))
                .when(MessageBusActionBuilder.aMessageWithoutEventType())
                .then(MessageBusValidationBuilder.expectTheException(MustNotBeNullException.class));
    }

    //errorPayload
    @Test
    default void testMessageBus_canSendAndReceiveErrorPayload(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASingleRawSubscriber())
                .when(MessageBusActionBuilder.aSingleMessageWithErrorPayloadIsSend())
                .then(MessageBusValidationBuilder.expectTheErrorPayloadToBeReceived());
    }

    //unsubscribe
    @Test
    default void testMessageBus_canUnsubscribe(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withSeveralSubscriber(5))
                .when(MessageBusActionBuilder.oneSubscriberUnsubscribes())
                .then(MessageBusValidationBuilder.expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testMessageBus_canUnsubscribeTwoSubscribers(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withSeveralSubscriber(5))
                .when(MessageBusActionBuilder.oneSubscriberUnsubscribes()
                        .andThen(MessageBusActionBuilder.oneSubscriberUnsubscribes()))
                .then(MessageBusValidationBuilder.expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testMessageBus_canUnsubscribeTheSameSubscriberSeveralTimes(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withSeveralSubscriber(5))
                .when(MessageBusActionBuilder.oneSubscriberUnsubscribesSeveralTimes(2))
                .then(MessageBusValidationBuilder.expectAllRemainingSubscribersToStillBeSubscribed());
    }

    //MessageId and CorrelationId
    @Test
    default void testMessageBus_sendMessageHasConstantMessageIdAndCanGenerateMatchingCorrelationId(
            final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASingleRawSubscriber())
                .when(MessageBusActionBuilder.aSingleMessageIsSend())
                .then(MessageBusValidationBuilder.expectTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId());
    }

    @Test
    default void testMessageBus_canSetCorrelationIdWhenSend(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASingleRawSubscriber())
                .when(MessageBusActionBuilder.aMessageWithCorrelationIdIsSend())
                .then(MessageBusValidationBuilder.expectTheCorrelationIdToBeSetWhenReceived());
    }

    @Test
    default void testMessageBus_canSendProcessingContextWithAMessageId(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASingleRawSubscriber())
                .when(MessageBusActionBuilder.aMessageWithCorrelationIdIsSend())
                .then(MessageBusValidationBuilder.expectTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId());
    }

    @Test
    default void testMessageBus_canSubscriberForSpecificCorrelationIds(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASubscriberForACorrelationId())
                .when(MessageBusActionBuilder.aMessageWithCorrelationIdIsSend())
                .then(MessageBusValidationBuilder.expectTheMessageWrappedInProcessingContextWithCorrectCorrelationIdToBeReceived());
    }

    @Test
    default void testMessageBus_canUnsubscribeForCorrelationId(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASubscriberForACorrelationId())
                .when(MessageBusActionBuilder.theSubscriberForTheCorrelationIdUnsubscribes()
                        .andThen(MessageBusActionBuilder.aMessageWithCorrelationIdIsSend()))
                .then(MessageBusValidationBuilder.expectNoMessagesToBeDelivered());
    }

    //filter
    @Test
    default void testMessageBus_allowsFiltersToChangeMessages(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASingleSubscriber()
                .withAFilterThatChangesTheContentOfEveryMessage())
                .when(MessageBusActionBuilder.severalMessagesAreSend(5))
                .then(MessageBusValidationBuilder.expectAllMessagesToHaveTheContentChanged());
    }

    @Test
    default void testMessageBus_allowsFiltersToDropMessages(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withSeveralSubscriber(3)
                .withAFilterThatDropsMessages())
                .when(MessageBusActionBuilder.halfValidAndInvalidMessagesAreSendAsynchronously(3, 10))
                .then(MessageBusValidationBuilder.expectNoMessagesToBeDelivered());
    }

    @Test
    default void testMessageBus_whenAFilterDoesNotUseAMethod_messageIsDropped(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withSeveralSubscriber(3)
                .withAnInvalidFilterThatDoesNotUseAnyFilterMethods())
                .when(MessageBusActionBuilder.severalInvalidMessagesAreSendAsynchronously(3, 10))
                .then(MessageBusValidationBuilder.expectNoMessagesToBeDelivered());
    }

    @Test
    default void testMessageBus_throwsExceptionForPositionBelowZero(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAFilterAtAnInvalidPosition(-1))
                .when(MessageBusActionBuilder.aSingleMessageIsSend())
                .then(MessageBusValidationBuilder.expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testMessageBus_throwsExceptionForPositionGreaterThanAllowed(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAFilterAtAnInvalidPosition(100))
                .when(MessageBusActionBuilder.aSingleMessageIsSend())
                .then(MessageBusValidationBuilder.expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testMessageBus_canQueryListOfFilter(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withTwoFilterOnSpecificPositions())
                .when(MessageBusActionBuilder.theListOfFiltersIsQueried())
                .then(MessageBusValidationBuilder.expectAListWithAllFilters());
    }

    @Test
    default void testMessageBus_canRemoveFilter(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withTwoFilterOnSpecificPositions())
                .when(MessageBusActionBuilder.aFilterIsRemoved())
                .then(MessageBusValidationBuilder.expectTheRemainingFilter());
    }

    @Test
    default void testMessageBus_allowsRawFiltersToCompleteProcessingContext(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASingleRawSubscriber()
                .withARawFilterThatChangesCompleteProcessingContext())
                .when(MessageBusActionBuilder.severalMessagesAreSend(5))
                .then(MessageBusValidationBuilder.expectAllProcessingContextsToBeReplaced());
    }

    //messageStatistics
    @Test
    default void testMessageBus_returnsCorrectNumberOfAcceptedMessages(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASingleSubscriber())
                .when(MessageBusActionBuilder.severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(MessageBusActionBuilder.theNumberOfAcceptedMessagesIsQueried()))
                .then(MessageBusValidationBuilder.expectResultToBe(15));
    }

    // queued statistics config dependent

    @Test
    default void testMessageBus_whenAFilterDoesNotUseAMethod_theMessageIsMarkedAsForgotten(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withSeveralSubscriber(3)
                .withAnInvalidFilterThatDoesNotUseAnyFilterMethods())
                .when(MessageBusActionBuilder.aSingleMessageIsSend()
                        .andThen(MessageBusActionBuilder.theNumberOfForgottenMessagesIsQueried()))
                .then(MessageBusValidationBuilder.expectResultToBe(1));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfDroppedMessages(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASingleSubscriber()
                .withAFilterThatDropsMessages())
                .when(MessageBusActionBuilder.severalInvalidMessagesAreSendAsynchronously(3, 5)
                        .andThen(MessageBusActionBuilder.theNumberOfBlockedMessagesIsQueried()))
                .then(MessageBusValidationBuilder.expectResultToBe(15));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfSuccessfulMessages(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASingleSubscriber())
                .when(MessageBusActionBuilder.severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(MessageBusActionBuilder.theNumberOfSuccessfulMessagesIsQueried()))
                .then(MessageBusValidationBuilder.expectResultToBe(15));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfDeliveryFailedMessages(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAnExceptionThrowingSubscriber()
                .withACustomExceptionHandlerMarkingExceptionAsIgnored())
                .when(MessageBusActionBuilder.severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(MessageBusActionBuilder.theNumberOfFailedMessagesIsQueried()))
                .then(MessageBusValidationBuilder.expectResultToBe(0));
        //Is 0, because errors in event-type specific channels do not count for MB itself
    }

    @Test
    default void testMessageBus_returnsAValidTimestampForStatistics(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withoutASubscriber())
                .when(MessageBusActionBuilder.theTimestampOfTheStatisticsIsQueried())
                .then(MessageBusValidationBuilder.expectTimestampToBeInTheLastXSeconds(1));
    }

    //subscribers
    @Test
    default void testMessageBus_returnsCorrectSubscribersPerType(final MessageBusTestConfig config) {
        final EventType eventTypeA = EventType.eventTypeFromString("A");
        final EventType eventTypeB = EventType.eventTypeFromString("B");
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASubscriberForTyp(eventTypeA)
                .withASubscriberForTyp(eventTypeB)
                .withARawSubscriberForType(eventTypeA)
                .withASubscriberForTyp(eventTypeA))
                .when(MessageBusActionBuilder.theSubscriberAreQueriedPerType())
                .then(MessageBusValidationBuilder.expectSubscriberOfType(3, eventTypeA)
                        .and(MessageBusValidationBuilder.expectSubscriberOfType(1, eventTypeB)));
    }

    @Test
    default void testMessageBus_returnsCorrectSubscribersInList(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASubscriberForTyp(EventType.eventTypeFromString("type1"))
                .withASubscriberForTyp(EventType.eventTypeFromString("type2")))
                .when(MessageBusActionBuilder.allSubscribersAreQueriedAsList())
                .then(MessageBusValidationBuilder.expectAListOfSize(2));
    }

    //channel
    @Test
    default void testMessageBus_returnsCorrectChannel(final MessageBusTestConfig config) {
        final EventType eventType = testEventType();
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withACustomChannelFactory()
                .withASubscriberForTyp(eventType))
                .when(MessageBusActionBuilder.theChannelForTheTypeIsQueried(eventType))
                .then(MessageBusValidationBuilder.expectTheCorrectChannel());
    }

    //shutdown
    @Test
    default void testMessageBus_canShutdown_evenIfIsBlocked(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config))
                .when(MessageBusActionBuilder.severalMessagesAreSendAsynchronouslyBeforeTheMessageBusIsShutdown(3)
                        .andThen(MessageBusActionBuilder.theMessageBusShutdownIsExpectedForTimeoutInSeconds(1)))
                .then(MessageBusValidationBuilder.expectTheMessageBusToBeShutdownInTime());
    }

    @Test
    default void testMessageBus_shutdownCallIsIdempotent(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withASubscriberThatBlocksWhenAccepting())
                .when(MessageBusActionBuilder.theMessageBusIsShutdownAsynchronouslyXTimes(6)
                        .andThen(MessageBusActionBuilder.theMessageBusIsShutdown()))
                .then(MessageBusValidationBuilder.expectTheMessageBusToBeShutdown());
    }

    //error cases
    @Test
    default void testMessageBus_throwsExceptionWhenSendOnAClosedMessageBusIsCalled(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAnExceptionAcceptingSubscriber())
                .when(MessageBusActionBuilder.theMessageBusIsShutdown()
                        .andThen(MessageBusActionBuilder.aSingleMessageIsSend()))
                .then(MessageBusValidationBuilder.expectTheException(AlreadyClosedException.class));
    }

    @Test
    default void testMessageBus_customExceptionHandlerCanAccessExceptionsInsideFilterOfAcceptingPipe(
            final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAnExceptionThrowingFilter()
                .withACustomExceptionHandler())
                .when(MessageBusActionBuilder.aSingleMessageIsSend())
                .then(MessageBusValidationBuilder.expectTheExceptionHandledAsFilterException(TestException.class));
    }

    @Test
    default void testMessageBus_customExceptionHandlerCanAccessExceptionsInsideFilterOfDeliveringPipes(
            final MessageBusTestConfig config) {
        final EventType eventType = testEventType();
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withACustomExceptionHandler())
                .when(MessageBusActionBuilder.aSubscriberIsAdded(eventType)
                        .andThen(MessageBusActionBuilder.anExceptionThrowingFilterIsAddedInChannelOf(eventType))
                        .andThen(MessageBusActionBuilder.aSingleMessageIsSend()))
                .then(MessageBusValidationBuilder.expectTheExceptionHandledAsFilterException(TestException.class));
    }

    @Test
    default void testMessageBus_customExceptionHandlerCanAccessExceptionsDuringDelivery(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withACustomExceptionHandler())
                .when(MessageBusActionBuilder.anExceptionThrowingSubscriberIsAdded()
                        .andThen(MessageBusActionBuilder.aSingleMessageIsSend()))
                .then(MessageBusValidationBuilder.expectTheExceptionHandledAsDeliverException(TestException.class));
    }

    @Test
    default void testMessageBus_customExceptionHandlerCanMarkExceptionAsNotDeliveryAborting(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withACustomExceptionHandlerMarkingExceptionAsIgnored())
                .when(MessageBusActionBuilder.anExceptionThrowingSubscriberIsAdded()
                        .andThen(MessageBusActionBuilder.aSingleMessageIsSend()
                                .andThen(MessageBusActionBuilder.theNumberOfSuccessfulMessagesIsQueried())))
                .then(MessageBusValidationBuilder.expectResultToBe(1)
                        .and(MessageBusValidationBuilder.expectNoException()));
    }

    //dynamic exception listener
    @Test
    default void testMessageBus_dynamicExceptionListenerCanBeAdded_forEventType_forExceptionInFilter(
            final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAnExceptionThrowingFilter()
                .withADynamicExceptionListenerForEventType())
                .when(MessageBusActionBuilder.aSingleMessageIsSend())
                .then(MessageBusValidationBuilder.expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_dynamicExceptionListenerCanBeAdded_forEventType_forExceptionInSubscriber(
            final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAnExceptionThrowingSubscriber()
                .withADynamicExceptionListenerForEventType())
                .when(MessageBusActionBuilder.aSingleMessageIsSend())
                .then(MessageBusValidationBuilder.expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_dynamicErrorListenerCanBeRemoved_forEventType(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAnExceptionThrowingSubscriber()
                .withTwoDynamicExceptionListenerForEventType())
                .when(MessageBusActionBuilder.theDynamicExceptionHandlerToBeRemoved()
                        .andThen(MessageBusActionBuilder.aSingleMessageIsSend()))
                .then(MessageBusValidationBuilder.expectNumberOfErrorListener(1)
                        .and(MessageBusValidationBuilder.expectTheExceptionHandled(TestException.class)));
    }

    @Test
    default void testMessageBus_dynamicExceptionListenerCanBeAdded_forCorrelationId_forExceptionInFilter(
            final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAnExceptionThrowingFilter()
                .withADynamicCorrelationIdBasedExceptionListener())
                .when(MessageBusActionBuilder.aMessageWithCorrelationIdIsSend())
                .then(MessageBusValidationBuilder.expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_dynamicExceptionListenerCanBeAdded_forCorrelationId_forExceptionInSubscriber(
            final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAnExceptionThrowingSubscriber()
                .withADynamicCorrelationIdBasedExceptionListener())
                .when(MessageBusActionBuilder.aMessageWithCorrelationIdIsSend())
                .then(MessageBusValidationBuilder.expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_dynamicCorrelationIdBasedErrorListenerCanBeRemoved(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAnExceptionThrowingSubscriber()
                .withTwoDynamicCorrelationBasedExceptionListener())
                .when(MessageBusActionBuilder.theDynamicExceptionHandlerToBeRemoved()
                        .andThen(MessageBusActionBuilder.aMessageWithCorrelationIdIsSend()))
                .then(MessageBusValidationBuilder.expectNumberOfErrorListener(1)
                        .and(MessageBusValidationBuilder.expectTheExceptionHandledOnlyByTheRemainingHandlers(TestException.class)));
    }

    @Test
    default void testMessageBus_dynamicExceptionListenerGetsCorrectMessageTheErrorOccurredOn(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAnExceptionThrowingFilter()
                .withADynamicExceptionListenerForEventType())
                .when(MessageBusActionBuilder.aSingleMessageIsSend())
                .then(MessageBusValidationBuilder.expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_canQueryForAllExceptionListener(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withADynamicExceptionListenerForEventType()
                .withADynamicCorrelationIdBasedExceptionListener())
                .when(MessageBusActionBuilder.allDynamicExceptionListenerAreQueried())
                .then(MessageBusValidationBuilder.expectAListOfSize(2));
    }

    //await
    @Test
    default void testMessageBus_awaitWithoutCloseReturnsAlwaysTrue(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAnExceptionAcceptingSubscriber())
                .when(MessageBusActionBuilder.theMessageBusShutdownIsAwaitedWithoutCloseCall())
                .then(MessageBusValidationBuilder.expectResultToBe(true));
    }
}
