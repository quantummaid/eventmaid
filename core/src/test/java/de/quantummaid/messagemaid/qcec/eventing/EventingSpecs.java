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

package de.quantummaid.messagemaid.qcec.eventing;

import de.quantummaid.messagemaid.qcec.eventing.givenWhenThen.TestEventBus;
import de.quantummaid.messagemaid.shared.exceptions.TestException;
import de.quantummaid.messagemaid.qcec.eventing.givenWhenThen.EventBusActionBuilder;
import de.quantummaid.messagemaid.qcec.eventing.givenWhenThen.EventBusValidationBuilder;
import de.quantummaid.messagemaid.qcec.eventing.givenWhenThen.Given;
import org.junit.jupiter.api.Test;

public interface EventingSpecs {

    @Test
    default void testEventBus_eventsAreReceivedByAllReceivers(final TestEventBus anEventBus) {
        Given.given(anEventBus)
                .when(EventBusActionBuilder.anEventIsPublishedToSeveralReceiver())
                .then(EventBusValidationBuilder.expectItToReceivedByAll());
    }

    @Test
    default void testEventBus_eventsDoNotHaveToBeReceived(final TestEventBus anEventBus) {
        Given.given(anEventBus)
                .when(EventBusActionBuilder.anEventIsPublishedToNoOne())
                .then(EventBusValidationBuilder.expectNoException());
    }

    @Test
    default void testEventBus_canUnsubscribe(final TestEventBus anEventBus) {
        Given.given(anEventBus)
                .when(EventBusActionBuilder.anReceiverUnsubscribes())
                .then(EventBusValidationBuilder.expectTheEventToBeReceivedByAllRemainingSubscribers());
    }

    @Test
    default void testEventBus_informsAboutThrownExceptions(final TestEventBus anEventBus) {
        Given.given(anEventBus)
                .when(EventBusActionBuilder.anEventIsDeliveredToAnErrorThrowingReceiver())
                .then(EventBusValidationBuilder.expectTheException(TestException.class));
    }

}
