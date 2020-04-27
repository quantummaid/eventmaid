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

package de.quantummaid.eventmaid.internal.pipe;

import de.quantummaid.eventmaid.exceptions.AlreadyClosedException;
import de.quantummaid.eventmaid.internal.pipe.config.PipeTestConfig;
import org.junit.jupiter.api.Test;

import static de.quantummaid.eventmaid.internal.pipe.givenwhenthen.Given.given;
import static de.quantummaid.eventmaid.internal.pipe.givenwhenthen.PipeActionBuilder.*;
import static de.quantummaid.eventmaid.internal.pipe.givenwhenthen.PipeSetupBuilder.aConfiguredPipe;
import static de.quantummaid.eventmaid.internal.pipe.givenwhenthen.PipeValidationBuilder.*;

public interface PipeSpecs {

    //sending and subscribe
    @Test
    default void testPipe_canSendSingleMessageToOneReceiver(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig)
                .withASingleSubscriber())
                .when(aSingleMessageIsSend())
                .then(expectTheMessageToBeReceived());
    }

    @Test
    default void testPipe_canSendSeveralMessagesToSeveralSubscriber(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(severalMessagesAreSend(10))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testPipe_canSendMessagesAsynchronously(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(8))
                .when(severalMessagesAreSendAsynchronously(10, 10))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testPipe_subscriberCanInterruptDeliveringMessage(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig)
                .withSeveralDeliveryInterruptingSubscriber(5))
                .when(severalMessagesAreSend(10))
                .then(expectEachMessagesToBeReceivedByOnlyOneSubscriber());
    }

    @Test
    default void testPipe_canQueryAllSubscriber(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(8))
                .when(theListOfSubscriberIsQueried())
                .then(expectTheListOfAllSubscriber());
    }

    //unsubscribe
    @Test
    default void testPipe_canUnsubscribe(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes())
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testPipe_canUnsubscribeTwoSubscribers(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes()
                        .andThen(oneSubscriberUnsubscribes()))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testPipe_canUnsubscribeTheSameSubscriberSeveralTimes(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribesSeveralTimes(2))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    //pipeStatistics
    @Test
    default void testPipe_returnsCorrectNumberOfAcceptedMessages(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfAcceptedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testPipe_returnsCorrectNumberOfSuccessfulMessages(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testPipe_returnsCorrectNumberOfDeliveryFailedMessages(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig)
                .causingErrorsWhenDelivering())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfFailedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    // statistic of waiting message config dependent

    // message statistics with blocking subscribers also config dependent

    @Test
    default void testPipe_returnsAValidTimestampForStatistics(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig)
                .withoutASubscriber())
                .when(theTimestampOfTheStatisticsIsQueried())
                .then(expectTimestampToBeInTheLastXSeconds(3));
    }

    //exception handling
    @Test
    default void testPipe_canUseCustomErrorHandler(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig)
                .withACustomErrorHandler())
                .when(aMessageResultingInAnErrorIsSend())
                .then(expectTheExceptionToBeHandled());
    }

    @Test
    default void testPipe_customErrorHandlerCanSuppressExceptionSoThatDeliveryCountsAsSuccessful(final PipeTestConfig config) {
        given(aConfiguredPipe(config)
                .withACustomErrorHandlerThatSuppressesException())
                .when(aMessageResultingInAnErrorIsSend())
                .then(expectTheDeliveryToBeStillSuccessful());
    }

    //shutdown
    @Test
    default void testPipe_canShutdown_evenIfIsBlocked(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig))
                .when(severalMessagesAreSendAsynchronouslyBeforeThePipeIsShutdown()
                        .andThen(thePipeShutdownIsExpectedForTimeoutInSeconds(1)))
                .then(expectThePipeToBeShutdownInTime());
    }

    @Test
    default void testPipe_shutdownCallIsIdempotent(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(thePipeIsShutdownAsynchronouslyXTimes(6)
                        .andThen(thePipeIsShutdown()))
                .then(expectThePipeToBeShutdown());
    }

    @Test
    default void testPipe_awaitReturnsAlwaysFalse_withoutACloseCall(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig))
                .when(awaitWithoutACloseIsCalled())
                .then(expectTheResultToAlwaysBeFalse());
    }

    @Test
    default void testPipe_throwsExceptionWhenSendIsCalledOnAClosedPipe(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig))
                .when(aMessageIsSendAfterTheShutdown())
                .then(expectTheException(AlreadyClosedException.class));
    }

    // behaviour of clean up of messages is config dependent

    //await
    @Test
    default void testPipe_awaitsSucceedsWhenAllTasksCouldBeDone(final PipeTestConfig testConfig) {
        final int numberOfMessagesSend = PipeTestConfig.ASYNCHRONOUS_PIPE_POOL_SIZE;
        given(aConfiguredPipe(testConfig))
                .when(closeAndThenWaitForPendingTasksToFinished(numberOfMessagesSend))
                .then(expectTheAwaitToBeTerminatedSuccessful(numberOfMessagesSend));
    }

    //await with unfinished tasks config dependent

}
