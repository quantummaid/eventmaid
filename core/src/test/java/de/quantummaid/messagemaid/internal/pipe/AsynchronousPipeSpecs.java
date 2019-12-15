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

import de.quantummaid.messagemaid.internal.pipe.config.AsynchronousPipeConfigurationProvider;
import de.quantummaid.messagemaid.internal.pipe.config.PipeTestConfig;
import de.quantummaid.messagemaid.internal.pipe.givenWhenThen.Given;
import de.quantummaid.messagemaid.internal.pipe.givenWhenThen.PipeActionBuilder;
import de.quantummaid.messagemaid.internal.pipe.givenWhenThen.PipeSetupBuilder;
import de.quantummaid.messagemaid.internal.pipe.transport.PipeWaitingQueueIsFullException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static de.quantummaid.messagemaid.internal.pipe.config.PipeTestConfig.*;
import static de.quantummaid.messagemaid.internal.pipe.givenWhenThen.PipeValidationBuilder.*;

@ExtendWith(AsynchronousPipeConfigurationProvider.class)
public class AsynchronousPipeSpecs implements PipeSpecs {

    //send
    @Test
    public void testPipe_doesNotFailForFullWaitingQueue() {
        final int completeCapacity = ASYNCHRONOUS_QUEUED_BOUND + ASYNCHRONOUS_PIPE_POOL_SIZE;
        Given.given(PipeSetupBuilder.aConfiguredPipe(anAsynchronousBoundedPipe())
                .withASubscriberThatBlocksWhenAccepting())
                .when(PipeActionBuilder.severalMessagesAreSend(completeCapacity))
                .then(expectNoException());
    }

    @Test
    public void testPipe_failsWhenBoundedQueueOverflows() {
        final int completeCapacity = ASYNCHRONOUS_QUEUED_BOUND + ASYNCHRONOUS_PIPE_POOL_SIZE;
        final int messagesSend = completeCapacity + 1;
        Given.given(PipeSetupBuilder.aConfiguredPipe(anAsynchronousBoundedPipe())
                .withASubscriberThatBlocksWhenAccepting())
                .when(PipeActionBuilder.severalMessagesAreSend(messagesSend))
                .then(expectTheException(PipeWaitingQueueIsFullException.class));
    }

    //statistics
    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfSuccessfulDeliveredMessagesIsQueried_returnsZero(
            final PipeTestConfig testConfig) {
        final int numberOfMessages = ASYNCHRONOUS_PIPE_POOL_SIZE;
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfMessages)
                        .andThen(PipeActionBuilder.theNumberOfSuccessfulMessagesIsQueriedWhenSubscriberBlocked()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfFailedDeliveredMessagesIsQueried_returnsZero(
            final PipeTestConfig testConfig) {
        final int numberOfMessages = ASYNCHRONOUS_PIPE_POOL_SIZE;
        final int expectedResult = 0;
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfMessages)
                        .andThen(PipeActionBuilder.theNumberOfFailedMessagesIsQueried(expectedResult)))
                .then(expectResultToBe(expectedResult));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfAcceptedMessagesIsQueried_returnsAll(final PipeTestConfig testConfig) {
        final int numberOfParallelSender = 3;
        final int numberOfMessagesPerSender = 5;
        final int expectedAcceptedMessages = numberOfParallelSender * numberOfMessagesPerSender;
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfParallelSender, numberOfMessagesPerSender)
                        .andThen(PipeActionBuilder.theNumberOfAcceptedMessagesIsQueried()))
                .then(expectResultToBe(expectedAcceptedMessages));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfQueuedMessagesIsQueried(final PipeTestConfig testConfig) {
        final int numberOfParallelSender = 3;
        final int numberOfMessagesPerSender = 5;
        final int sumOfMessages = numberOfParallelSender * numberOfMessagesPerSender;
        final int expectedQueuedMessages = sumOfMessages - ASYNCHRONOUS_PIPE_POOL_SIZE;
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfParallelSender, numberOfMessagesPerSender)
                        .andThen(PipeActionBuilder.theNumberOfQueuedMessagesIsQueried()))
                .then(expectResultToBe(expectedQueuedMessages));
    }

    //shutdown
    @Test
    public void testPipe_whenShutdown_deliversRemainingMessagesButNoNewAdded(final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered(10))
                .then(expectXMessagesToBeDelivered_despiteTheChannelClosed(5));
    }

    @Test
    public void testPipe_whenShutdownWithoutFinishingRemainingTasksIsCalled_noTasksAreFinished(final PipeTestConfig testConfig) {
        final int numberOfParallelSendMessage = 5;
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(10))
                .then(expectXMessagesToBeDelivered_despiteTheChannelClosed(numberOfParallelSendMessage));
    }

    //await
    @Test
    public void testPipe_awaitsFailsWhenAllTasksCouldBeDone(final PipeTestConfig testConfig) {
        final int numberOfMessagesSend = ASYNCHRONOUS_PIPE_POOL_SIZE + 3;
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.awaitIsCalledWithoutAllowingRemainingTasksToFinish(numberOfMessagesSend))
                .then(expectTheAwaitToBeTerminatedWithFailure());
    }

}
