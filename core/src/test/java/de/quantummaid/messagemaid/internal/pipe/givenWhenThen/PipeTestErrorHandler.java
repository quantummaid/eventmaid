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

package de.quantummaid.messagemaid.internal.pipe.givenWhenThen;

import de.quantummaid.messagemaid.internal.pipe.error.PipeErrorHandler;
import de.quantummaid.messagemaid.shared.environment.TestEnvironment;
import de.quantummaid.messagemaid.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static de.quantummaid.messagemaid.shared.properties.SharedTestProperties.EXPECTED_AND_IGNORED_EXCEPTION;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
final class PipeTestErrorHandler implements PipeErrorHandler<TestMessage> {
    private final Consumer<Exception> exceptionHandlerForNotIgnoredExceptions;
    private final TestEnvironment testEnvironment;
    private final Class<?>[] ignoredExceptionsClasses;

    static PipeTestErrorHandler pipeTestErrorHandler(final Consumer<Exception> exceptionHandlerForNotIgnoredExceptions,
                                                     final TestEnvironment testEnvironment,
                                                     final Class<?>... ignoredExceptionsClasses) {
        return new PipeTestErrorHandler(exceptionHandlerForNotIgnoredExceptions, testEnvironment, ignoredExceptionsClasses);
    }

    @Override
    public boolean shouldErrorBeHandledAndDeliveryAborted(final TestMessage message, final Exception e) {
        for (final Class<?> ignoredExceptionClass : ignoredExceptionsClasses) {
            if (e.getClass().equals(ignoredExceptionClass)) {
                testEnvironment.addToListProperty(EXPECTED_AND_IGNORED_EXCEPTION, e);
                return false;
            }
        }
        return true;
    }

    @Override
    public void handleException(final TestMessage message, final Exception e) {
        exceptionHandlerForNotIgnoredExceptions.accept(e);
    }
}
