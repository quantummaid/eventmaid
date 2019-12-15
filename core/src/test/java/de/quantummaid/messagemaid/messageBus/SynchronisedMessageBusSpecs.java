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

package de.quantummaid.messagemaid.messageBus;

import de.quantummaid.messagemaid.messageBus.config.MessageBusTestConfig;
import de.quantummaid.messagemaid.messageBus.config.SynchronisedMessageBusConfigurationResolver;
import de.quantummaid.messagemaid.shared.exceptions.TestException;
import de.quantummaid.messagemaid.messageBus.givenWhenThen.Given;
import de.quantummaid.messagemaid.messageBus.givenWhenThen.MessageBusActionBuilder;
import de.quantummaid.messagemaid.messageBus.givenWhenThen.MessageBusSetupBuilder;
import de.quantummaid.messagemaid.messageBus.givenWhenThen.MessageBusValidationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SynchronisedMessageBusConfigurationResolver.class)
public class SynchronisedMessageBusSpecs implements MessageBusSpecs {

    //messageStatistics
    @Test
    public void testMessageBus_queryingNumberOfQueuedMessages_alwaysReturnsZero(
            final MessageBusTestConfig config) {
        final int messagesSend = 3;
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config))
                .when(MessageBusActionBuilder.severalMessagesAreSendAsynchronouslyButWillBeBlocked(messagesSend)
                        .andThen(MessageBusActionBuilder.theNumberOfQueuedMessagesIsQueried()))
                .then(MessageBusValidationBuilder.expectResultToBe(0));
    }

    //shutdown
    @Test
    public void testMessageBus_whenShutdownAllRemainingTasksAreFinished(final MessageBusTestConfig config) {
        final int numberOfParallelSendMessages = 10;
        final boolean finishRemainingTasks = true;
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config))
                .when(MessageBusActionBuilder.sendSeveralMessagesBeforeTheBusIsShutdown(numberOfParallelSendMessages, finishRemainingTasks))
                .then(MessageBusValidationBuilder.expectXMessagesToBeDelivered(10));
    }

    @Test
    public void testMessageBus_whenShutdownWithoutFinishingRemainingTasks_allTasksAreStillFinished(
            final MessageBusTestConfig config) {
        final int numberOfParallelSendMessages = 10;
        final boolean finishRemainingTasks = false;
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config))
                .when(MessageBusActionBuilder.sendSeveralMessagesBeforeTheBusIsShutdown(numberOfParallelSendMessages, finishRemainingTasks))
                .then(MessageBusValidationBuilder.expectXMessagesToBeDelivered(10));
    }

    //errors
    @Test
    public void testMessageBus_dynamicErrorHandlerIsCalledOnceIfMessageBusExceptionHandlerRethrowsException(
            final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAnExceptionThrowingSubscriber()
                .withADynamicErrorListenerAndAnErrorThrowingExceptionHandler())
                .when(MessageBusActionBuilder.aSingleMessageIsSend())
                .then(MessageBusValidationBuilder.expectTheExceptionHandledAndTheErrorToBeThrown(TestException.class));
    }

    @Test
    public void testMessageBus_exceptionIsAlsoThrownBySendMethod(final MessageBusTestConfig config) {
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config)
                .withAnExceptionThrowingSubscriber()
                .withAnErrorThrowingExceptionHandler())
                .when(MessageBusActionBuilder.aSingleMessageIsSend())
                .then(MessageBusValidationBuilder.expectTheException(TestException.class));
    }
}
