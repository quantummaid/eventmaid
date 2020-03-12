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

import de.quantummaid.eventmaid.channel.config.ChannelTestConfig;
import de.quantummaid.eventmaid.channel.config.SynchronousChannelConfigResolver;
import de.quantummaid.eventmaid.shared.exceptions.TestException;
import de.quantummaid.eventmaid.channel.givenWhenThen.ChannelActionBuilder;
import de.quantummaid.eventmaid.channel.givenWhenThen.ChannelSetupBuilder;
import de.quantummaid.eventmaid.channel.givenWhenThen.ChannelValidationBuilder;
import de.quantummaid.eventmaid.channel.givenWhenThen.Given;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SynchronousChannelConfigResolver.class)
public class SynchronousChannelSpecs implements ChannelSpecs {

    //statistics
    @Test
    public void testChannel_synchronousConfigDoesNotQueueMessages(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 7;
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withSubscriptionAsAction())
                .when(ChannelActionBuilder.severalMessagesAreSendAsynchronouslyThatWillBeBlocked(numberOfSendMessages)
                        .andThen(ChannelActionBuilder.theNumberOfQueuedMessagesIsQueried()))
                .then(ChannelValidationBuilder.expectTheResult(0));
    }

    //shutdown
    @Test
    public void testChannel_closeWithoutFinishingRemainingTasks_hasNoEffectForSynchronousConfig(final ChannelTestConfig config) {
        final int numberOfMessages = 7;
        Given.given(ChannelSetupBuilder.aConfiguredChannel(config)
                .withSubscriptionAsAction())
                .when(ChannelActionBuilder.severalMessagesAreSendAsynchronouslyBeforeTheChannelIsClosedWithoutFinishingRemainingTasks(numberOfMessages)
                        .andThen(ChannelActionBuilder.theNumberOfMessagesIsQueriedThatAreStillDeliveredSuccessfully()))
                .then(ChannelValidationBuilder.expectTheResult(numberOfMessages)
                        .and(ChannelValidationBuilder.expectTheChannelToBeShutdown()));
    }

    //errors
    @Test
    public void testMessageBus_exceptionHandlerIsCalledOnceEvenIfExceptionIsRethrown(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAnExceptionInFinalAction()
                .withAnExceptionHandlerRethrowingExceptions())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectADeliveryExceptionOfType(TestException.class));
    }

    @Test
    public void testMessageBus_exceptionIsAlsoThrownBySendMethod(final ChannelTestConfig channelTestConfig) {
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withAnExceptionInFinalAction()
                .withAnExceptionHandlerRethrowingExceptions())
                .when(ChannelActionBuilder.aMessageIsSend())
                .then(ChannelValidationBuilder.expectADeliveryExceptionOfType(TestException.class));
    }

    //await
    @Test
    public void testChannel_awaitsWithoutFinishingTasks_succeedsDespiteNotFinished(final ChannelTestConfig channelTestConfig) {
        final int numberOfMessages = 7;
        Given.given(ChannelSetupBuilder.aConfiguredChannel(channelTestConfig)
                .withSubscriptionAsAction())
                .when(ChannelActionBuilder.severalMessagesAreSendAsynchronouslyBeforeTheChannelIsClosedWithoutFinishingRemainingTasks(numberOfMessages)
                        .andThen(ChannelActionBuilder.theShutdownIsAwaited()))
                .then(ChannelValidationBuilder.expectTheShutdownToBeSucceededInTime());
    }

}
