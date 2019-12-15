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

package de.quantummaid.messagemaid.useCases.specialInvocations;

import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.shared.givenWhenThen.TestAction;
import de.quantummaid.messagemaid.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import de.quantummaid.messagemaid.useCases.useCaseBus.UseCaseBus;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.*;
import static de.quantummaid.messagemaid.useCases.specialInvocations.UseCaseTestInvocation.USE_CASE_INVOCATIONS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SpecialInvocationUseCaseInvoker {
    private final TestAction<UseCaseBus> testAction;

    public static SpecialInvocationUseCaseInvoker whenTheUSeCaseIsInvoked() {
        return new SpecialInvocationUseCaseInvoker((useCaseBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyAsType(TEST_OBJECT, EventType.class);
            try {
                final PayloadAndErrorPayload<Object, Object> payload =
                        useCaseBus.invokeAndWait(eventType, null, null, null, 1, SECONDS);
                testEnvironment.setPropertyIfNotSet(RESULT, payload);
            } catch (final ExecutionException e) {
                testEnvironment.setPropertyIfNotSet(RESULT, e);
            } catch (final InterruptedException | TimeoutException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    public static SpecialInvocationUseCaseInvoker whenBothUseCasesAreInvoked() {
        return new SpecialInvocationUseCaseInvoker((useCaseBus, testEnvironment) -> {
            final List<UseCaseTestInvocation> useCaseTestInvocations = (List<UseCaseTestInvocation>) testEnvironment.getProperty(USE_CASE_INVOCATIONS);
            try {
                for (final UseCaseTestInvocation useCaseTestInvocation : useCaseTestInvocations) {
                    final PayloadAndErrorPayload<?, ?> payload = useCaseTestInvocation.execute();
                    testEnvironment.addToListProperty(RESULT, payload);
                }
            } catch (final ExecutionException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            } catch (final InterruptedException | TimeoutException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public TestAction<UseCaseBus> build() {
        return testAction;
    }
}
