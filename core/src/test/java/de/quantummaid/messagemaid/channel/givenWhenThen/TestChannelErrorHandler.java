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

package de.quantummaid.messagemaid.channel.givenWhenThen;

import de.quantummaid.messagemaid.shared.exceptions.TestException;
import de.quantummaid.messagemaid.channel.exception.ChannelExceptionHandler;
import de.quantummaid.messagemaid.processingContext.ProcessingContext;
import de.quantummaid.messagemaid.shared.environment.TestEnvironment;
import de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty;
import de.quantummaid.messagemaid.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.EXCEPTION;
import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.RESULT;
import static de.quantummaid.messagemaid.shared.properties.SharedTestProperties.EXCEPTION_OCCURRED_DURING_DELIVERY;
import static de.quantummaid.messagemaid.shared.properties.SharedTestProperties.EXCEPTION_OCCURRED_INSIDE_FILTER;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestChannelErrorHandler {

    private static final String TEST_PROPERTY_TO_ENSURE_HANDLER_CALLED_ONCE = "TEST_PROPERTY_TO_ENSURE_HANDLER_CALLED_ONCE";

    public static ChannelExceptionHandler<TestMessage> ignoringChannelExceptionHandler() {
        return new ChannelExceptionHandler<>() {
            @Override
            public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(final ProcessingContext<TestMessage> message,
                                                                            final Exception e) {
                return true;
            }

            @Override
            public void handleSubscriberException(final ProcessingContext<TestMessage> message, final Exception e) {
            }

            @Override
            public void handleFilterException(final ProcessingContext<TestMessage> message, final Exception e) {
            }
        };
    }

    public static ChannelExceptionHandler<TestMessage> exceptionInResultStoringChannelExceptionHandler(
            final TestEnvironment testEnvironment) {
        return storingExceptionHandler(testEnvironment, RESULT);
    }

    public static ChannelExceptionHandler<TestMessage> catchingChannelExceptionHandler(final TestEnvironment testEnvironment) {
        return storingExceptionHandler(testEnvironment, EXCEPTION);
    }

    public static ChannelExceptionHandler<TestMessage> testExceptionIgnoringChannelExceptionHandler(
            final TestEnvironment testEnvironment) {
        return storingExceptionHandler(testEnvironment, EXCEPTION, TestException.class);
    }

    private static ChannelExceptionHandler<TestMessage> storingExceptionHandler(final TestEnvironment testEnvironment,
                                                                                final TestEnvironmentProperty property,
                                                                                final Class<?>... ignoredClasses) {
        return new ChannelExceptionHandler<>() {
            @Override
            public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(final ProcessingContext<TestMessage> message,
                                                                            final Exception e) {
                for (final Class<?> ignoredClass : ignoredClasses) {
                    if (e.getClass().equals(ignoredClass)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void handleSubscriberException(final ProcessingContext<TestMessage> message, final Exception e) {
                testEnvironment.setPropertyIfNotSet(TEST_PROPERTY_TO_ENSURE_HANDLER_CALLED_ONCE, true);
                testEnvironment.setPropertyIfNotSet(property, e);
                testEnvironment.setPropertyIfNotSet(EXCEPTION_OCCURRED_DURING_DELIVERY, true);
            }

            @Override
            public void handleFilterException(final ProcessingContext<TestMessage> message, final Exception e) {
                testEnvironment.setPropertyIfNotSet(TEST_PROPERTY_TO_ENSURE_HANDLER_CALLED_ONCE, true);
                testEnvironment.setPropertyIfNotSet(property, e);
                testEnvironment.setPropertyIfNotSet(EXCEPTION_OCCURRED_INSIDE_FILTER, true);
            }
        };
    }

    public static ChannelExceptionHandler<TestMessage> errorRethrowingExceptionHandler(final TestEnvironment testEnvironment) {
        return new ChannelExceptionHandler<>() {
            @Override
            public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(final ProcessingContext<TestMessage> message,
                                                                            final Exception e) {
                return true;
            }

            @Override
            public void handleSubscriberException(final ProcessingContext<TestMessage> message, final Exception e) {
                testEnvironment.setPropertyIfNotSet(TEST_PROPERTY_TO_ENSURE_HANDLER_CALLED_ONCE, true);
                testEnvironment.setPropertyIfNotSet(EXCEPTION_OCCURRED_DURING_DELIVERY, true);
                throw (RuntimeException) e;
            }

            @Override
            public void handleFilterException(final ProcessingContext<TestMessage> message, final Exception e) {
                testEnvironment.setPropertyIfNotSet(TEST_PROPERTY_TO_ENSURE_HANDLER_CALLED_ONCE, true);
                testEnvironment.setPropertyIfNotSet(EXCEPTION_OCCURRED_INSIDE_FILTER, true);
                throw (RuntimeException) e;
            }
        };
    }
}
