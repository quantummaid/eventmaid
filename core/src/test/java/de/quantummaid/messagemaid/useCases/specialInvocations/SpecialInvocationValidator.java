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

import de.quantummaid.messagemaid.shared.environment.TestEnvironment;
import de.quantummaid.messagemaid.shared.givenWhenThen.TestValidation;
import de.quantummaid.messagemaid.shared.validations.SharedTestValidations;
import de.quantummaid.messagemaid.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.RESULT;
import static de.quantummaid.messagemaid.shared.validations.SharedTestValidations.assertNoExceptionThrown;
import static de.quantummaid.messagemaid.useCases.noParameter.NoParameterUseCase.NO_PARAMETER_USE_CASE_RETURN_VALUE;
import static de.quantummaid.messagemaid.useCases.singleEventParameter.SingleParameterResponse.singleParameterResponse;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SpecialInvocationValidator {
    private final TestValidation testValidation;

    private static SpecialInvocationValidator asValidation(final TestValidation testValidation) {
        return new SpecialInvocationValidator(testValidation);
    }

    public static SpecialInvocationValidator expectExecutionExceptionContaining(final RuntimeException expectedException) {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertResultOfClass(testEnvironment, ExecutionException.class);
            final ExecutionException exception = testEnvironment.getPropertyAsType(RESULT, ExecutionException.class);
            SharedTestValidations.assertEquals(exception.getCause(), expectedException);
        });
    }

    public static SpecialInvocationValidator expectExecutionExceptionContainingExceptionClass(
            final Class<?> expectedExceptionClass) {
        return asValidation(testEnvironment -> {
            SharedTestValidations.assertResultOfClass(testEnvironment, ExecutionException.class);
            final ExecutionException exception = testEnvironment.getPropertyAsType(RESULT, ExecutionException.class);
            final Throwable cause = exception.getCause();
            SharedTestValidations.assertEquals(cause.getClass(), expectedExceptionClass);
        });
    }

    public static SpecialInvocationValidator expectedBothUseCaseToBeInvoked() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);

            final List<PayloadAndErrorPayload<?, ?>> result = getPayloadResults(testEnvironment);
            SharedTestValidations.assertListOfSize(result, 2);
            final PayloadAndErrorPayload<?, ?> noParameterUseCaseResult = result.get(0);
            final Object stringResponse = noParameterUseCaseResult.getPayload();
            SharedTestValidations.assertEquals(stringResponse, NO_PARAMETER_USE_CASE_RETURN_VALUE);

            final PayloadAndErrorPayload<?, ?> singleParameterUseCaseResult = result.get(1);
            final Object singleParameterResponse = singleParameterUseCaseResult.getPayload();
            SharedTestValidations.assertEquals(singleParameterResponse, singleParameterResponse("Test"));
        });
    }

    @SuppressWarnings("unchecked")
    private static List<PayloadAndErrorPayload<?, ?>> getPayloadResults(final TestEnvironment testEnvironment) {
        return (List<PayloadAndErrorPayload<?, ?>>) testEnvironment.getProperty(RESULT);
    }

    public TestValidation build() {
        return testValidation;
    }
}
