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
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.shared.exceptions.TestException;
import de.quantummaid.eventmaid.shared.testmessages.TestMessageOfInterest;
import org.junit.jupiter.api.Test;

import static de.quantummaid.eventmaid.messagebus.givenwhenthen.Given.given;
import static de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusActionBuilder.*;
import static de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusSetupBuilder.aConfiguredMessageBus;
import static de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusValidationBuilder.*;
import static de.quantummaid.eventmaid.shared.eventtype.TestEventType.testEventType;
import static de.quantummaid.eventmaid.shared.testmessages.TestMessageOfInterest.messageOfInterest;

public interface MessageBusSpecs {

    //Send and subscribe
    @Test
    default void testMessageBus_canSendAndReceiveASingleMessage(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASingleSubscriber())
                .when(aSingleMessageIsSend())
                .then(expectTheMessageToBeReceived());
    }

    @Test
    default void testMessageBus_canSendAndReceiveSeveralMessagesWithSeveralSubscriber(
            final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withSeveralSubscriber(5))
                .when(severalMessagesAreSend(10))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testMessageBus_canSendMessageTwice(final MessageBusTestConfig config) {
        final TestMessageOfInterest message = messageOfInterest();
        given(aConfiguredMessageBus(config)
                .withASingleSubscriber())
                .when(theMessageIsSend(message)
                        .andThen(theMessageIsSend(message)))
                .then(expectTheMessagesToBeReceivedByAllSubscriber(message, message));
    }

    @Test
    default void testMessageBus_canSendAndReceiveMessagesAsynchronously(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withSeveralSubscriber(5))
                .when(severalMessagesAreSendAsynchronously(5, 10))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testMessageBus_canSendAMessageWithoutPayload(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASingleRawSubscriber())
                .when(aMessageWithoutPayloadIsSend())
                .then(expectTheMessageToBeReceived());
    }

    @Test
    default void testMessageBus_throwsExceptionWhenEventTypeIsNotSet(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config))
                .when(aMessageWithoutEventType())
                .then(expectTheException(MustNotBeNullException.class));
    }

    //errorPayload
    @Test
    default void testMessageBus_canSendAndReceiveErrorPayload(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASingleRawSubscriber())
                .when(aSingleMessageWithErrorPayloadIsSend())
                .then(expectTheErrorPayloadToBeReceived());
    }

    //unsubscribe
    @Test
    default void testMessageBus_canUnsubscribe(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes())
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testMessageBus_canUnsubscribeTwoSubscribers(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes()
                        .andThen(oneSubscriberUnsubscribes()))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testMessageBus_canUnsubscribeTheSameSubscriberSeveralTimes(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribesSeveralTimes(2))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    //MessageId and CorrelationId
    @Test
    default void testMessageBus_sendMessageHasConstantMessageIdAndCanGenerateMatchingCorrelationId(
            final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASingleRawSubscriber())
                .when(aSingleMessageIsSend())
                .then(expectTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId());
    }

    @Test
    default void testMessageBus_canSetCorrelationIdWhenSend(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASingleRawSubscriber())
                .when(aMessageWithCorrelationIdIsSend())
                .then(expectTheCorrelationIdToBeSetWhenReceived());
    }

    @Test
    default void testMessageBus_canSendProcessingContextWithAMessageId(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASingleRawSubscriber())
                .when(aMessageWithCorrelationIdIsSend())
                .then(expectTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId());
    }

    @Test
    default void testMessageBus_canSubscriberForSpecificCorrelationIds(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASubscriberForACorrelationId())
                .when(aMessageWithCorrelationIdIsSend())
                .then(expectTheMessageWrappedInProcessingContextWithCorrectCorrelationIdToBeReceived());
    }

    @Test
    default void testMessageBus_canUnsubscribeForCorrelationId(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASubscriberForACorrelationId())
                .when(theSubscriberForTheCorrelationIdUnsubscribes()
                        .andThen(aMessageWithCorrelationIdIsSend()))
                .then(expectNoMessagesToBeDelivered());
    }

    //filter
    @Test
    default void testMessageBus_allowsFiltersToChangeMessages(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASingleSubscriber()
                .withAFilterThatChangesTheContentOfEveryMessage())
                .when(severalMessagesAreSend(5))
                .then(expectAllMessagesToHaveTheContentChanged());
    }

    @Test
    default void testMessageBus_allowsFiltersToDropMessages(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withSeveralSubscriber(3)
                .withAFilterThatDropsMessages())
                .when(halfValidAndInvalidMessagesAreSendAsynchronously(3, 10))
                .then(expectNoMessagesToBeDelivered());
    }

    @Test
    default void testMessageBus_whenAFilterDoesNotUseAMethod_messageIsDropped(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withSeveralSubscriber(3)
                .withAnInvalidFilterThatDoesNotUseAnyFilterMethods())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 10))
                .then(expectNoMessagesToBeDelivered());
    }

    @Test
    default void testMessageBus_throwsExceptionForPositionBelowZero(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withAFilterAtAnInvalidPosition(-1))
                .when(aSingleMessageIsSend())
                .then(expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testMessageBus_throwsExceptionForPositionGreaterThanAllowed(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withAFilterAtAnInvalidPosition(100))
                .when(aSingleMessageIsSend())
                .then(expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testMessageBus_canQueryListOfFilter(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withTwoFilterOnSpecificPositions())
                .when(theListOfFiltersIsQueried())
                .then(expectAListWithAllFilters());
    }

    @Test
    default void testMessageBus_canRemoveFilter(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withTwoFilterOnSpecificPositions())
                .when(aFilterIsRemoved())
                .then(expectTheRemainingFilter());
    }

    @Test
    default void testMessageBus_allowsRawFiltersToCompleteProcessingContext(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASingleRawSubscriber()
                .withARawFilterThatChangesCompleteProcessingContext())
                .when(severalMessagesAreSend(5))
                .then(expectAllProcessingContextsToBeReplaced());
    }

    //messageStatistics
    @Test
    default void testMessageBus_returnsCorrectNumberOfAcceptedMessages(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfAcceptedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    // queued statistics config dependent

    @Test
    default void testMessageBus_whenAFilterDoesNotUseAMethod_theMessageIsMarkedAsForgotten(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withSeveralSubscriber(3)
                .withAnInvalidFilterThatDoesNotUseAnyFilterMethods())
                .when(aSingleMessageIsSend()
                        .andThen(theNumberOfForgottenMessagesIsQueried()))
                .then(expectResultToBe(1));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfDroppedMessages(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASingleSubscriber()
                .withAFilterThatDropsMessages())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfBlockedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfSuccessfulMessages(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfDeliveryFailedMessages(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withAnExceptionThrowingSubscriber()
                .withACustomExceptionHandlerMarkingExceptionAsIgnored())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfFailedMessagesIsQueried()))
                .then(expectResultToBe(0));
        //Is 0, because errors in event-type specific channels do not count for MB itself
    }

    @Test
    default void testMessageBus_returnsAValidTimestampForStatistics(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withoutASubscriber())
                .when(theTimestampOfTheStatisticsIsQueried())
                .then(expectTimestampToBeInTheLastXSeconds(1));
    }

    //subscribers
    @Test
    default void testMessageBus_returnsCorrectSubscribersPerType(final MessageBusTestConfig config) {
        final EventType eventTypeA = EventType.eventTypeFromString("A");
        final EventType eventTypeB = EventType.eventTypeFromString("B");
        given(aConfiguredMessageBus(config)
                .withASubscriberForTyp(eventTypeA)
                .withASubscriberForTyp(eventTypeB)
                .withARawSubscriberForType(eventTypeA)
                .withASubscriberForTyp(eventTypeA))
                .when(theSubscriberAreQueriedPerType())
                .then(expectSubscriberOfType(3, eventTypeA)
                        .and(expectSubscriberOfType(1, eventTypeB)));
    }

    @Test
    default void testMessageBus_returnsCorrectSubscribersInList(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASubscriberForTyp(EventType.eventTypeFromString("type1"))
                .withASubscriberForTyp(EventType.eventTypeFromString("type2")))
                .when(allSubscribersAreQueriedAsList())
                .then(expectAListOfSize(2));
    }

    //channel
    @Test
    default void testMessageBus_returnsCorrectChannel(final MessageBusTestConfig config) {
        final EventType eventType = testEventType();
        given(aConfiguredMessageBus(config)
                .withACustomChannelFactory()
                .withASubscriberForTyp(eventType))
                .when(theChannelForTheTypeIsQueried(eventType))
                .then(expectTheCorrectChannel());
    }

    //shutdown
    @Test
    default void testMessageBus_canShutdown_evenIfIsBlocked(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config))
                .when(severalMessagesAreSendAsynchronouslyBeforeTheMessageBusIsShutdown(3)
                        .andThen(theMessageBusShutdownIsExpectedForTimeoutInSeconds(1)))
                .then(expectTheMessageBusToBeShutdownInTime());
    }

    @Test
    default void testMessageBus_shutdownCallIsIdempotent(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withASubscriberThatBlocksWhenAccepting())
                .when(theMessageBusIsShutdownAsynchronouslyXTimes(6)
                        .andThen(theMessageBusIsShutdown()))
                .then(expectTheMessageBusToBeShutdown());
    }

    //error handling
    @Test
    default void testMessageBus_throwsExceptionWhenSendOnAClosedMessageBusIsCalled(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withAnExceptionAcceptingSubscriber())
                .when(theMessageBusIsShutdown()
                        .andThen(aSingleMessageIsSend()))
                .then(expectTheException(AlreadyClosedException.class));
    }

    @Test
    default void testMessageBus_customExceptionHandlerCanAccessExceptionsInsideFilterOfAcceptingPipe(
            final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withAnExceptionThrowingFilter()
                .withACustomExceptionHandler())
                .when(aSingleMessageIsSend())
                .then(expectTheExceptionHandledAsFilterException(TestException.class));
    }

    @Test
    default void testMessageBus_customExceptionHandlerCanAccessExceptionsInsideFilterOfDeliveringPipes(
            final MessageBusTestConfig config) {
        final EventType eventType = testEventType();
        given(aConfiguredMessageBus(config)
                .withACustomExceptionHandler())
                .when(aSubscriberIsAdded(eventType)
                        .andThen(anExceptionThrowingFilterIsAddedInChannelOf(eventType))
                        .andThen(aSingleMessageIsSend()))
                .then(expectTheExceptionHandledAsFilterException(TestException.class));
    }

    @Test
    default void testMessageBus_customExceptionHandlerCanAccessExceptionsDuringDelivery(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withACustomExceptionHandler())
                .when(anExceptionThrowingSubscriberIsAdded()
                        .andThen(aSingleMessageIsSend()))
                .then(expectTheExceptionHandledAsDeliverException(TestException.class));
    }

    @Test
    default void testMessageBus_customExceptionHandlerCanMarkExceptionAsNotDeliveryAborting(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withACustomExceptionHandlerMarkingExceptionAsIgnored())
                .when(anExceptionThrowingSubscriberIsAdded()
                        .andThen(aSingleMessageIsSend()
                                .andThen(theNumberOfSuccessfulMessagesIsQueried())))
                .then(expectResultToBe(1)
                        .and(expectNoException()));
    }

    //dynamic exception listener
    @Test
    default void testMessageBus_dynamicExceptionListenerCanBeAdded_forEventType_forExceptionInFilter(
            final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withAnExceptionThrowingFilter()
                .withADynamicExceptionListenerForEventType())
                .when(aSingleMessageIsSend())
                .then(expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_dynamicExceptionListenerCanBeAdded_forEventType_forExceptionInSubscriber(
            final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withAnExceptionThrowingSubscriber()
                .withADynamicExceptionListenerForEventType())
                .when(aSingleMessageIsSend())
                .then(expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_dynamicErrorListenerCanBeRemoved_forEventType(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withAnExceptionThrowingSubscriber()
                .withTwoDynamicExceptionListenerForEventType())
                .when(theDynamicExceptionHandlerToBeRemoved()
                        .andThen(aSingleMessageIsSend()))
                .then(expectNumberOfErrorListener(1)
                        .and(expectTheExceptionHandled(TestException.class)));
    }

    @Test
    default void testMessageBus_dynamicExceptionListenerCanBeAdded_forCorrelationId_forExceptionInFilter(
            final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withAnExceptionThrowingFilter()
                .withADynamicCorrelationIdBasedExceptionListener())
                .when(aMessageWithCorrelationIdIsSend())
                .then(expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_dynamicExceptionListenerCanBeAdded_forCorrelationId_forExceptionInSubscriber(
            final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withAnExceptionThrowingSubscriber()
                .withADynamicCorrelationIdBasedExceptionListener())
                .when(aMessageWithCorrelationIdIsSend())
                .then(expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_dynamicCorrelationIdBasedErrorListenerCanBeRemoved(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withAnExceptionThrowingSubscriber()
                .withTwoDynamicCorrelationBasedExceptionListener())
                .when(theDynamicExceptionHandlerToBeRemoved()
                        .andThen(aMessageWithCorrelationIdIsSend()))
                .then(expectNumberOfErrorListener(1)
                        .and(expectTheExceptionHandledOnlyByTheRemainingHandlers(TestException.class)));
    }

    @Test
    default void testMessageBus_dynamicExceptionListenerGetsCorrectMessageTheErrorOccurredOn(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withAnExceptionThrowingFilter()
                .withADynamicExceptionListenerForEventType())
                .when(aSingleMessageIsSend())
                .then(expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_canQueryForAllExceptionListener(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withADynamicExceptionListenerForEventType()
                .withADynamicCorrelationIdBasedExceptionListener())
                .when(allDynamicExceptionListenerAreQueried())
                .then(expectAListOfSize(2));
    }

    //await
    @Test
    default void testMessageBus_awaitWithoutCloseReturnsAlwaysTrue(final MessageBusTestConfig config) {
        given(aConfiguredMessageBus(config)
                .withAnExceptionAcceptingSubscriber())
                .when(theMessageBusShutdownIsAwaitedWithoutCloseCall())
                .then(expectResultToBe(true));
    }
}
