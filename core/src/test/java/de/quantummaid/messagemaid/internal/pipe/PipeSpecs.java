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

package de.quantummaid.messagemaid.internal.pipe;

import de.quantummaid.messagemaid.internal.pipe.config.PipeTestConfig;
import de.quantummaid.messagemaid.internal.pipe.givenWhenThen.Given;
import de.quantummaid.messagemaid.internal.pipe.givenWhenThen.PipeActionBuilder;
import de.quantummaid.messagemaid.internal.pipe.givenWhenThen.PipeSetupBuilder;
import de.quantummaid.messagemaid.internal.pipe.givenWhenThen.PipeValidationBuilder;
import de.quantummaid.messagemaid.exceptions.AlreadyClosedException;
import org.junit.jupiter.api.Test;

public interface PipeSpecs {

    //sending and subscribe
    @Test
    default void testPipe_canSendSingleMessageToOneReceiver(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig)
                .withASingleSubscriber())
                .when(PipeActionBuilder.aSingleMessageIsSend())
                .then(PipeValidationBuilder.expectTheMessageToBeReceived());
    }

    @Test
    default void testPipe_canSendSeveralMessagesToSeveralSubscriber(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(PipeActionBuilder.severalMessagesAreSend(10))
                .then(PipeValidationBuilder.expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testPipe_canSendMessagesAsynchronously(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig)
                .withSeveralSubscriber(8))
                .when(PipeActionBuilder.severalMessagesAreSendAsynchronously(10, 10))
                .then(PipeValidationBuilder.expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testPipe_subscriberCanInterruptDeliveringMessage(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig)
                .withSeveralDeliveryInterruptingSubscriber(5))
                .when(PipeActionBuilder.severalMessagesAreSend(10))
                .then(PipeValidationBuilder.expectEachMessagesToBeReceivedByOnlyOneSubscriber());
    }

    @Test
    default void testPipe_canQueryAllSubscriber(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig)
                .withSeveralSubscriber(8))
                .when(PipeActionBuilder.theListOfSubscriberIsQueried())
                .then(PipeValidationBuilder.expectTheListOfAllSubscriber());
    }

    //unsubscribe
    @Test
    default void testPipe_canUnsubscribe(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(PipeActionBuilder.oneSubscriberUnsubscribes())
                .then(PipeValidationBuilder.expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testPipe_canUnsubscribeTwoSubscribers(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(PipeActionBuilder.oneSubscriberUnsubscribes()
                        .andThen(PipeActionBuilder.oneSubscriberUnsubscribes()))
                .then(PipeValidationBuilder.expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testPipe_canUnsubscribeTheSameSubscriberSeveralTimes(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(PipeActionBuilder.oneSubscriberUnsubscribesSeveralTimes(2))
                .then(PipeValidationBuilder.expectAllRemainingSubscribersToStillBeSubscribed());
    }

    //pipeStatistics
    @Test
    default void testPipe_returnsCorrectNumberOfAcceptedMessages(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig)
                .withASingleSubscriber())
                .when(PipeActionBuilder.severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(PipeActionBuilder.theNumberOfAcceptedMessagesIsQueried()))
                .then(PipeValidationBuilder.expectResultToBe(15));
    }

    @Test
    default void testPipe_returnsCorrectNumberOfSuccessfulMessages(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig)
                .withASingleSubscriber())
                .when(PipeActionBuilder.severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(PipeActionBuilder.theNumberOfSuccessfulMessagesIsQueried()))
                .then(PipeValidationBuilder.expectResultToBe(15));
    }

    @Test
    default void testPipe_returnsCorrectNumberOfDeliveryFailedMessages(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig)
                .causingErrorsWhenDelivering())
                .when(PipeActionBuilder.severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(PipeActionBuilder.theNumberOfFailedMessagesIsQueried()))
                .then(PipeValidationBuilder.expectResultToBe(15));
    }

    // statistic of waiting message config dependent

    // message statistics with blocking subscribers also config dependent

    @Test
    default void testPipe_returnsAValidTimestampForStatistics(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig)
                .withoutASubscriber())
                .when(PipeActionBuilder.theTimestampOfTheStatisticsIsQueried())
                .then(PipeValidationBuilder.expectTimestampToBeInTheLastXSeconds(3));
    }

    //exceptions handling
    @Test
    default void testPipe_canUseCustomErrorHandler(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig)
                .withACustomErrorHandler())
                .when(PipeActionBuilder.aMessageResultingInAnErrorIsSend())
                .then(PipeValidationBuilder.expectTheExceptionToBeHandled());
    }

    @Test
    default void testPipe_customErrorHandlerCanSuppressExceptionSoThatDeliveryCountsAsSuccessful(final PipeTestConfig config) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(config)
                .withACustomErrorHandlerThatSuppressesException())
                .when(PipeActionBuilder.aMessageResultingInAnErrorIsSend())
                .then(PipeValidationBuilder.expectTheDeliveryToBeStillSuccessful());
    }

    //shutdown
    @Test
    default void testPipe_canShutdown_evenIfIsBlocked(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.severalMessagesAreSendAsynchronouslyBeforeThePipeIsShutdown()
                        .andThen(PipeActionBuilder.thePipeShutdownIsExpectedForTimeoutInSeconds(1)))
                .then(PipeValidationBuilder.expectThePipeToBeShutdownInTime());
    }

    @Test
    default void testPipe_shutdownCallIsIdempotent(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(PipeActionBuilder.thePipeIsShutdownAsynchronouslyXTimes(6)
                        .andThen(PipeActionBuilder.thePipeIsShutdown()))
                .then(PipeValidationBuilder.expectThePipeToBeShutdown());
    }

    @Test
    default void testPipe_awaitReturnsAlwaysFalse_withoutACloseCall(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.awaitWithoutACloseIsCalled())
                .then(PipeValidationBuilder.expectTheResultToAlwaysBeFalse());
    }

    @Test
    default void testPipe_throwsExceptionWhenSendIsCalledOnAClosedPipe(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.aMessageIsSendAfterTheShutdown())
                .then(PipeValidationBuilder.expectTheException(AlreadyClosedException.class));
    }

    // behaviour of clean up of messages is config dependent

    //await
    @Test
    default void testPipe_awaitsSucceedsWhenAllTasksCouldBeDone(final PipeTestConfig testConfig) {
        final int numberOfMessagesSend = PipeTestConfig.ASYNCHRONOUS_PIPE_POOL_SIZE;
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.closeAndThenWaitForPendingTasksToFinished(numberOfMessagesSend))
                .then(PipeValidationBuilder.expectTheAwaitToBeTerminatedSuccessful(numberOfMessagesSend));
    }

    //await with unfinished tasks config dependent

}
