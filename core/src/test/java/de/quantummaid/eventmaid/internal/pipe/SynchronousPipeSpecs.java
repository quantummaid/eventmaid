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

import de.quantummaid.eventmaid.internal.pipe.config.PipeTestConfig;
import de.quantummaid.eventmaid.internal.pipe.config.SynchronisedPipeConfigurationResolver;
import de.quantummaid.eventmaid.internal.pipe.givenwhenthen.Given;
import de.quantummaid.eventmaid.internal.pipe.givenwhenthen.PipeActionBuilder;
import de.quantummaid.eventmaid.internal.pipe.givenwhenthen.PipeSetupBuilder;
import de.quantummaid.eventmaid.internal.pipe.givenwhenthen.PipeValidationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SynchronisedPipeConfigurationResolver.class)
public class SynchronousPipeSpecs implements PipeSpecs {

    //messageStatistics
    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfSuccessfulDeliveredMessagesIsQueried_returnsZero(
            final PipeTestConfig testConfig) {
        final int numberOfMessages = 5;
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfMessages)
                        .andThen(PipeActionBuilder.theNumberOfSuccessfulMessagesIsQueriedWhenSubscriberBlocked()))
                .then(PipeValidationBuilder.expectResultToBe(0));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfAcceptedMessagesIsQueried_returnsNumberOfThreads(
            final PipeTestConfig testConfig) {
        final int numberOfMessages = 3;
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfMessages)
                        .andThen(PipeActionBuilder.theNumberOfAcceptedMessagesIsQueried()))
                .then(PipeValidationBuilder.expectResultToBe(numberOfMessages));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfQueuedMessagesIsQueried_returnsNumberOfThreads(
            final PipeTestConfig testConfig) {
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.severalMessagesAreSendAsynchronouslyButWillBeBlocked(5)
                        .andThen(PipeActionBuilder.theNumberOfQueuedMessagesIsQueried()))
                .then(PipeValidationBuilder.expectResultToBe(0));
    }

    //shutdown
    @Test
    public void testPipe_whenShutdown_deliversRemainingMessagesButNoNewAdded(final PipeTestConfig testConfig) {
        final int numberOfParallelSendMessagesBeforeShutdown = 5;
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered(10))
                .then(PipeValidationBuilder.expectXMessagesToBeDelivered_despiteTheChannelClosed(numberOfParallelSendMessagesBeforeShutdown));
        //because waiting senders wait on synchronised send -> they never entered the Pipe and do not count as remaining
    }

    @Test
    public void testPipe_whenShutdownWithoutFinishingRemainingTasksIsCalled_noTasksAreFinished(final PipeTestConfig testConfig) {
        final int numberOfParallelSendMessagesBeforeShutdown = 5;
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(10))
                .then(PipeValidationBuilder.expectXMessagesToBeDelivered_despiteTheChannelClosed(numberOfParallelSendMessagesBeforeShutdown));
    }

    //await
    @Test
    public void testPipe_awaitsSucceedsEvenIfTasksCouldNotBeFinished(final PipeTestConfig testConfig) {
        final int numberOfMessagesSend = 5;
        Given.given(PipeSetupBuilder.aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.awaitIsCalledWithoutAllowingRemainingTasksToFinish(numberOfMessagesSend))
                .then(PipeValidationBuilder.expectTheAwaitToBeTerminatedSuccessful());
        //await returns always true, as it's not feasible to check whether there are still Threads waiting in the pipe
    }

}
