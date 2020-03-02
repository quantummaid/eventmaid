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

package de.quantummaid.eventmaid.qcec.constraining.givenWhenThen;

import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.qcec.shared.TestReceiver;
import de.quantummaid.eventmaid.shared.givenWhenThen.TestValidation;
import de.quantummaid.eventmaid.qcec.shared.testConstraints.TestConstraint;
import de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

@RequiredArgsConstructor(access = PRIVATE)
public final class ConstraintValidationBuilder {
    private final TestValidation testValidation;

    public static ConstraintValidationBuilder expectTheConstraintToBeReceivedByAll() {
        return new ConstraintValidationBuilder(testEnvironment -> {
            ensureNoExceptionOccurred(testEnvironment);
            final List<TestReceiver<TestConstraint>> expectedReceivers = getExpectedReceivers(testEnvironment);
            final TestConstraint sendConstraint = testEnvironment.getPropertyAsType(TestEnvironmentProperty.TEST_OBJECT, TestConstraint.class);
            for (final TestReceiver<TestConstraint> receiver : expectedReceivers) {
                assertThat(receiver.hasReceived(sendConstraint), equalTo(true));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static List<TestReceiver<TestConstraint>> getExpectedReceivers(final TestEnvironment testEnvironment) {
        return (List<TestReceiver<TestConstraint>>) testEnvironment.getProperty(TestEnvironmentProperty.EXPECTED_RECEIVERS);
    }

    public static ConstraintValidationBuilder expectTheConstraintToBeReceivedByAllRemainingSubscribers() {
        return expectTheConstraintToBeReceivedByAll();
    }

    public static ConstraintValidationBuilder expectTheExceptionToBeThrown() {
        return new ConstraintValidationBuilder(testEnvironment -> {
            final Exception exception = testEnvironment.getPropertyAsType(TestEnvironmentProperty.EXCEPTION, Exception.class);
            assertThat(exception.getClass(), equalTo(RuntimeException.class));
            final String expectedExceptionMessage = testEnvironment.getPropertyAsType(TestEnvironmentProperty.EXPECTED_EXCEPTION_MESSAGE, String.class);
            assertThat(exception.getMessage(), equalTo(expectedExceptionMessage));
        });
    }

    private static void ensureNoExceptionOccurred(final TestEnvironment testEnvironment) {
        final boolean exceptionOccurred = testEnvironment.has(TestEnvironmentProperty.EXCEPTION);
        assertFalse(exceptionOccurred);
    }

    public TestValidation build() {
        return testValidation;
    }
}
