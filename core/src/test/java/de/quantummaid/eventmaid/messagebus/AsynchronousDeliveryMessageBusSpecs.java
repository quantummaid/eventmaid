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

import de.quantummaid.eventmaid.messagebus.config.AsynchronousDeliveryMessageBusConfigurationResolver;
import de.quantummaid.eventmaid.messagebus.config.MessageBusTestConfig;
import de.quantummaid.eventmaid.messagebus.givenwhenthen.Given;
import de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusActionBuilder;
import de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusSetupBuilder;
import de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusValidationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AsynchronousDeliveryMessageBusConfigurationResolver.class)
public class AsynchronousDeliveryMessageBusSpecs implements MessageBusSpecs {

    @Test
    public void testMessageBus_queryingNumberOfQueuedMessages(final MessageBusTestConfig config) {
        final int expectedQueuedMessages = 5;
        final int messagesSendParallel = MessageBusTestConfig.ASYNCHRONOUS_DELIVERY_POOL_SIZE + expectedQueuedMessages;
        final int expectedNumberOfBlockedThreads = MessageBusTestConfig.ASYNCHRONOUS_DELIVERY_POOL_SIZE;
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config))
                .when(MessageBusActionBuilder.severalMessagesAreSendAsynchronouslyButWillBeBlocked(messagesSendParallel, expectedNumberOfBlockedThreads)
                        .andThen(MessageBusActionBuilder.theNumberOfQueuedMessagesIsQueried()))
                .then(MessageBusValidationBuilder.expectResultToBe(expectedQueuedMessages));
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
        final int numberOfParallelSendMessages = MessageBusTestConfig.ASYNCHRONOUS_DELIVERY_POOL_SIZE + 3;
        final boolean finishRemainingTasks = false;
        Given.given(MessageBusSetupBuilder.aConfiguredMessageBus(config))
                .when(MessageBusActionBuilder.sendSeveralMessagesBeforeTheBusIsShutdown(numberOfParallelSendMessages, finishRemainingTasks))
                .then(MessageBusValidationBuilder.expectXMessagesToBeDelivered(MessageBusTestConfig.ASYNCHRONOUS_DELIVERY_POOL_SIZE));
    }
}
