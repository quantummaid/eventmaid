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
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty;
import de.quantummaid.eventmaid.shared.givenWhenThen.TestValidation;
import de.quantummaid.eventmaid.shared.validations.SharedTestValidations;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor(access = PRIVATE)
public final class EventBusValidationBuilder {
    private final TestValidation validation;

    public static EventBusValidationBuilder expectItToReceivedByAll() {
        return new EventBusValidationBuilder(testEnvironment -> {
            ensureNoExceptionOccurred(testEnvironment);
            final TestEvent testEvent = testEnvironment.getPropertyAsType(TestEnvironmentProperty.TEST_OBJECT, TestEvent.class);
            final List<TestReceiver<TestEvent>> receivers = getExpectedTestEventReceivers(testEnvironment);
            for (final TestReceiver<TestEvent> receiver : receivers) {
                assertTrue(receiver.hasReceived(testEvent));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static List<TestReceiver<TestEvent>> getExpectedTestEventReceivers(final TestEnvironment testEnvironment) {
        return (List<TestReceiver<TestEvent>>) testEnvironment.getProperty(TestEnvironmentProperty.EXPECTED_RECEIVERS);
    }

    public static EventBusValidationBuilder expectTheEventToBeReceivedByAllRemainingSubscribers() {
        return expectItToReceivedByAll();
    }

    public static EventBusValidationBuilder expectNoException() {
        return new EventBusValidationBuilder(EventBusValidationBuilder::ensureNoExceptionOccurred);
    }

    public static EventBusValidationBuilder expectTheException(final Class<?> expectedExceptionClass) {
        return new EventBusValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertExceptionThrownOfType(testEnvironment, expectedExceptionClass);
        });
    }

    private static void ensureNoExceptionOccurred(final TestEnvironment testEnvironment) {
        final boolean exceptionOccurred = testEnvironment.has(TestEnvironmentProperty.EXCEPTION);
        assertFalse(exceptionOccurred);
    }

    public TestValidation build() {
        return validation;
    }
}
