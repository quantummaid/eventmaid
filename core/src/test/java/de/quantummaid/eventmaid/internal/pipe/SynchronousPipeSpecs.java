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
import de.quantummaid.eventmaid.shared.exceptions.TestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static de.quantummaid.eventmaid.internal.pipe.givenwhenthen.Given.given;
import static de.quantummaid.eventmaid.internal.pipe.givenwhenthen.PipeActionBuilder.*;
import static de.quantummaid.eventmaid.internal.pipe.givenwhenthen.PipeSetupBuilder.aConfiguredPipe;
import static de.quantummaid.eventmaid.internal.pipe.givenwhenthen.PipeValidationBuilder.*;

@ExtendWith(SynchronisedPipeConfigurationResolver.class)
public class SynchronousPipeSpecs implements PipeSpecs {

    //messageStatistics
    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfSuccessfulDeliveredMessagesIsQueried_returnsZero(
            final PipeTestConfig testConfig) {
        final int numberOfMessages = 5;
        given(aConfiguredPipe(testConfig))
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfMessages)
                        .andThen(theNumberOfSuccessfulMessagesIsQueriedWhenSubscriberBlocked()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfAcceptedMessagesIsQueried_returnsNumberOfThreads(
            final PipeTestConfig testConfig) {
        final int numberOfMessages = 3;
        given(aConfiguredPipe(testConfig))
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfMessages)
                        .andThen(theNumberOfAcceptedMessagesIsQueried()))
                .then(expectResultToBe(numberOfMessages));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfQueuedMessagesIsQueried_returnsNumberOfThreads(
            final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig))
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(5)
                        .andThen(theNumberOfQueuedMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    //exception handling
    @Test
    public void testPipe_defaultErrorHandlerThrowsException(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig))
                .when(aMessageResultingInAnErrorIsSend())
                .then(expectTheException(TestException.class));
    }

    //shutdown
    @Test
    public void testPipe_whenShutdown_deliversRemainingMessagesButNoNewAdded(final PipeTestConfig testConfig) {
        final int numberOfParallelSendMessagesBeforeShutdown = 5;
        given(aConfiguredPipe(testConfig))
                .when(thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered(10))
                .then(expectXMessagesToBeDelivered_despiteTheChannelClosed(numberOfParallelSendMessagesBeforeShutdown));
        //because waiting senders wait on synchronised send -> they never entered the Pipe and do not count as remaining
    }

    @Test
    public void testPipe_whenShutdownWithoutFinishingRemainingTasksIsCalled_noTasksAreFinished(final PipeTestConfig testConfig) {
        final int numberOfParallelSendMessagesBeforeShutdown = 5;
        given(aConfiguredPipe(testConfig))
                .when(thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(10))
                .then(expectXMessagesToBeDelivered_despiteTheChannelClosed(numberOfParallelSendMessagesBeforeShutdown));
    }

    //await
    @Test
    public void testPipe_awaitsSucceedsEvenIfTasksCouldNotBeFinished(final PipeTestConfig testConfig) {
        final int numberOfMessagesSend = 5;
        given(aConfiguredPipe(testConfig))
                .when(awaitIsCalledWithoutAllowingRemainingTasksToFinish(numberOfMessagesSend))
                .then(expectTheAwaitToBeTerminatedSuccessful());
        //await returns always true, as it's not feasible to check whether there are still Threads waiting in the pipe
    }

}
