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

package de.quantummaid.eventmaid.qcec.eventing.givenWhenThen;

import de.quantummaid.eventmaid.qcec.shared.TestReceiver;
import de.quantummaid.eventmaid.qcec.shared.testEvents.TestEvent;
import de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty;
import de.quantummaid.eventmaid.shared.exceptions.TestException;
import de.quantummaid.eventmaid.shared.givenwhenthen.TestAction;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.eventmaid.qcec.shared.TestReceiver.aTestReceiver;
import static de.quantummaid.eventmaid.qcec.shared.testEvents.TestEvent.testEvent;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class EventBusActionBuilder {
    private final TestAction<TestEventBus> testAction;

    public static EventBusActionBuilder anEventIsPublishedToSeveralReceiver() {
        return new EventBusActionBuilder((testEventBus, testEnvironment) -> {
            final int numberOfReceivers = 5;
            for (int i = 0; i < numberOfReceivers; i++) {
                final TestReceiver<TestEvent> receiver = aTestReceiver();
                testEventBus.reactTo(TestEvent.class, receiver);
                testEnvironment.addToListProperty(TestEnvironmentProperty.EXPECTED_RECEIVERS, receiver);
            }
            final TestEvent testEvent = testEvent();
            testEnvironment.setProperty(TestEnvironmentProperty.TEST_OBJECT, testEvent);
            testEventBus.publish(testEvent);
            return null;
        });
    }

    public static EventBusActionBuilder anEventIsPublishedToNoOne() {
        return new EventBusActionBuilder((testEventBus, testEnvironment) -> {
            final TestEvent testEvent = testEvent();
            testEnvironment.setProperty(TestEnvironmentProperty.TEST_OBJECT, testEvent);
            testEventBus.publish(testEvent);
            return null;
        });
    }

    public static EventBusActionBuilder anReceiverUnsubscribes() {
        return new EventBusActionBuilder((testEventBus, testEnvironment) -> {
            final SubscriptionId subscriptionId = testEventBus.reactTo(TestEvent.class, e -> {
                throw new RuntimeException("This receiver should not be called");
            });
            testEventBus.unsubscribe(subscriptionId);

            final TestReceiver<TestEvent> receiver = aTestReceiver();
            testEventBus.reactTo(TestEvent.class, receiver);
            testEnvironment.addToListProperty(TestEnvironmentProperty.EXPECTED_RECEIVERS, receiver);

            final TestEvent testEvent = testEvent();
            testEnvironment.setProperty(TestEnvironmentProperty.TEST_OBJECT, testEvent);
            testEventBus.publish(testEvent);
            return null;
        });
    }

    public static EventBusActionBuilder anEventIsDeliveredToAnErrorThrowingReceiver() {
        return new EventBusActionBuilder((testEventBus, testEnvironment) -> {
            testEventBus.reactTo(TestEvent.class, e -> {
                throw new TestException();
            });

            final TestReceiver<?> receiver = aTestReceiver();
            testEnvironment.addToListProperty(TestEnvironmentProperty.EXPECTED_RECEIVERS, receiver);

            final TestEvent testEvent = testEvent();
            testEnvironment.setProperty(TestEnvironmentProperty.TEST_OBJECT, testEvent);
            testEventBus.publish(testEvent);
            return null;
        });
    }

    public TestAction<TestEventBus> build() {
        return testAction;
    }
}
