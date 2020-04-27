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

package de.quantummaid.eventmaid.shared.validations;

import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.*;
import static de.quantummaid.eventmaid.shared.polling.PollingUtils.pollUntil;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor(access = PRIVATE)
public final class SharedTestValidations {

    public static void assertResultAndExpectedResultAreEqual(final TestEnvironment testEnvironment) {
        final Object expectedResult = testEnvironment.getProperty(EXPECTED_RESULT);
        assertResultEqualsExpected(testEnvironment, expectedResult);
    }

    public static void assertResultEqualsExpected(final TestEnvironment testEnvironment, final Object expectedResult) {
        pollUntil(() -> testEnvironment.has(RESULT));
        final Object result = testEnvironment.getProperty(RESULT);
        assertEquals(result, expectedResult);
    }

    public static void assertEquals(final Object result, final Object expectedResult) {
        if (expectedResult instanceof Number && result instanceof Number) {
            final double resultAsDouble = ((Number) result).doubleValue();
            final double expectedAsDouble = ((Number) expectedResult).doubleValue();
            assertThat(resultAsDouble, equalTo(expectedAsDouble));
        } else {
            assertThat(result, equalTo(expectedResult));
        }
    }

    public static boolean testEquals(final Object result, final Object expectedResult) {
        if (expectedResult instanceof Number && result instanceof Number) {
            final double resultAsDouble = ((Number) result).doubleValue();
            final double expectedAsDouble = ((Number) expectedResult).doubleValue();
            return resultAsDouble == expectedAsDouble;
        } else {
            return result.equals(expectedResult);
        }
    }

    public static void assertResultOfClass(final TestEnvironment testEnvironment, final Class<?> expectedResultClass) {
        pollUntil(() -> testEnvironment.has(RESULT));
        final Object result = testEnvironment.getProperty(RESULT);
        assertThat(result.getClass(), equalTo(expectedResultClass));
    }

    public static void assertNoResultSet(final TestEnvironment testEnvironment) {
        if (testEnvironment.has(RESULT)) {
            final Object result = testEnvironment.getProperty(RESULT);
            fail("Unexpected result: " + result);
        }
    }

    public static void assertNoExceptionThrown(final TestEnvironment testEnvironment) {
        if (testEnvironment.has(EXCEPTION)) {
            final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            fail("Unexpected exception", exception);
        }
    }

    public static void assertExceptionThrownOfType(final TestEnvironment testEnvironment, final Class<?> expectedExceptionClass) {
        assertExceptionThrownOfType(testEnvironment, expectedExceptionClass, EXCEPTION);
    }

    public static void assertExceptionThrownOfType(final TestEnvironment testEnvironment,
                                                   final Class<?> expectedExceptionClass,
                                                   final TestEnvironmentProperty property) {
        pollUntil(() -> testEnvironment.has(property));
        final Exception exception = testEnvironment.getPropertyAsType(property, Exception.class);
        final Class<? extends Exception> exceptionClass = exception.getClass();
        boolean assertSucceeded = false;
        try {
            assertThat(exceptionClass, equalTo(expectedExceptionClass));
            assertSucceeded = true;
        } finally {
            if (!assertSucceeded) {
                exception.printStackTrace();
            }
        }
    }

    public static void assertExceptionThrown(final TestEnvironment testEnvironment,
                                             final Exception expectedException) {
        pollUntil(() -> testEnvironment.has(EXCEPTION));
        final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
        boolean assertSucceeded = false;
        try {
            assertThat(exception, equalTo(expectedException));
            assertSucceeded = true;
        } finally {
            if (!assertSucceeded) {
                exception.printStackTrace();
            }
        }
    }

    public static void assertExceptionThrownOfTypeWithCause(final TestEnvironment testEnvironment,
                                                            final Class<?> expectedExceptionClass,
                                                            final Class<?> expectedCauseClass) {
        pollUntil(() -> testEnvironment.has(EXCEPTION));
        final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
        final Class<? extends Exception> exceptionClass = exception.getClass();
        assertThat(exceptionClass, equalTo(expectedExceptionClass));
        final Throwable cause = exception.getCause();
        final Class<? extends Throwable> causeClass = cause.getClass();
        assertThat(causeClass, equalTo(expectedCauseClass));
    }

    public static void assertTimestampToBeInTheLastXSeconds(final TestEnvironment testEnvironment,
                                                            final long maximumSecondsDifference) {
        final Date now = new Date();
        final Date timestamp = testEnvironment.getPropertyAsType(RESULT, Date.class);
        final long secondsDifference = (now.getTime() - timestamp.getTime()) / 1000;
        assertThat(secondsDifference, lessThanOrEqualTo(maximumSecondsDifference));
    }

    public static void assertResultIsListOfSize(final TestEnvironment testEnvironment, final int expectedSize) {
        final List<?> list = testEnvironment.getPropertyAsType(RESULT, List.class);
        assertListOfSize(list, expectedSize);
    }

    public static void assertListOfSize(final List<?> list, final int expectedSize) {
        assertThat(list.size(), equalTo(expectedSize));
    }

    public static void assertPropertyTrue(final TestEnvironment testEnvironment, final TestEnvironmentProperty property) {
        final String name = property.name();
        assertPropertyTrue(testEnvironment, name);
    }

    public static void assertPropertyTrue(final TestEnvironment testEnvironment, final String property) {
        final boolean condition = testEnvironment.getPropertyAsType(property, Boolean.class);
        assertTrue(condition, "Expected property " + property + " to be true, but it was false.");
    }

    public static void assertPropertyFalseOrUnset(final TestEnvironment testEnvironment,
                                                  final TestEnvironmentProperty property) {
        final String name = property.name();
        assertPropertyFalseOrUnset(testEnvironment, name);
    }

    public static void assertPropertyFalseOrUnset(final TestEnvironment testEnvironment, final String property) {
        if (testEnvironment.has(property)) {
            final boolean condition = testEnvironment.getPropertyAsType(property, Boolean.class);
            assertFalse(condition, "Expected property " + property + " to be false, but it was true.");
        }
    }

    public static void assertCollectionOfSize(final Collection<?> collection, final int expectedSize) {
        assertThat(collection.size(), equalTo(expectedSize));
    }

}
