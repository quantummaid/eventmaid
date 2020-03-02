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

package de.quantummaid.eventmaid.useCases.checkedException;

import de.quantummaid.eventmaid.useCases.givenWhenThen.DeAndSerializationDefinition;
import de.quantummaid.eventmaid.useCases.shared.RequestExpectedResultTuple;
import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.useCases.shared.UseCaseInvocationConfiguration;
import de.quantummaid.eventmaid.useCases.building.*;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

import static de.quantummaid.eventmaid.processingContext.EventType.eventTypeFromString;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class CheckedExceptionInvocationConfiguration implements UseCaseInvocationConfiguration {
    private static final String PARAMETER_MAP_PROPERTY_NAME = "value";
    private static final String RETURN_MAP_PROPERTY_NAME = "returnValue";

    @Override
    public Class<?> getUseCaseClass() {
        return CheckedExceptionThrowingUseCase.class;
    }

    @Override
    public EventType getEventTypeUseCaseIsRegisteredFor() {
        return eventTypeFromString("CheckedExceptionThrowingUseCase");
    }

    @Override
    public DeAndSerializationDefinition<RequestSerializationStep1Builder> getRequestSerializationDefinitions() {
        return requestSerializationStep1Builder -> {
            requestSerializationStep1Builder.serializingUseCaseRequestOntoTheBusOfType(CheckedExceptionThrowingRequest.class)
                    .using(object -> {
                        final CheckedExceptionThrowingRequest request = (CheckedExceptionThrowingRequest) object;
                        final CheckedTestException exception = request.getExceptionToThrow();
                        final String message = exception.getMessage();
                        return Map.of(PARAMETER_MAP_PROPERTY_NAME, message);
                    });
        };
    }

    @Override
    public DeAndSerializationDefinition<RequestDeserializationStep1Builder> getRequestDeserializationDefinitions() {
        return requestDeserializationStep1Builder -> {
            requestDeserializationStep1Builder
                    .deserializingRequestsToUseCaseParametersOfType(CheckedExceptionThrowingRequest.class)
                    .using((targetType, map) -> {
                        final String message = (String) map.get(PARAMETER_MAP_PROPERTY_NAME);
                        final CheckedTestException checkedTestException = CheckedTestException.checkedTestException(message);
                        return CheckedExceptionThrowingRequest.checkedExceptionThrowingRequest(checkedTestException);
                    });
        };
    }

    @Override
    public DeAndSerializationDefinition<ResponseSerializationStep1Builder> getResponseSerializationDefinitions() {
        return responseSerializationStep1Builder -> {
            //use case method never terminates normally as always an exception is thrown
        };
    }

    @Override
    public DeAndSerializationDefinition<ExceptionSerializationStep1Builder> getExceptionsSerializationDefinitions() {
        return exceptionSerializationStep1Builder -> {
            exceptionSerializationStep1Builder.serializingExceptionsOfType(CheckedTestException.class)
                    .using(e -> Map.of(RETURN_MAP_PROPERTY_NAME, e.getMessage()));
        };
    }

    @Override
    public DeAndSerializationDefinition<ResponseDeserializationStep1Builder> getResponseDeserializationDefinitions() {
        return responseDeserializationStep1Builder -> {
            responseDeserializationStep1Builder.deserializingUseCaseResponsesOfType(CheckedTestException.class)
                    .using((targetType, map) -> CheckedTestException.checkedTestException((String) map.get(RETURN_MAP_PROPERTY_NAME)));
        };
    }

    @Override
    public RequestExpectedResultTuple createRequestExpectedResultTuple() {
        final String message = UUID.randomUUID().toString();
        final CheckedTestException exception = CheckedTestException.checkedTestException(message);
        final CheckedExceptionThrowingRequest request = CheckedExceptionThrowingRequest.checkedExceptionThrowingRequest(exception);
        return RequestExpectedResultTuple.requestExpectedErrorResultTuple(request, exception);
    }

    @Override
    public RequestExpectedResultTuple createSerializedRequestExpectedResultTuple() {
        final String message = UUID.randomUUID().toString();
        final Map<String, String> requestObject = Map.of(PARAMETER_MAP_PROPERTY_NAME, message);
        final Map<String, String> expectedResult = Map.of(RETURN_MAP_PROPERTY_NAME, message);
        return RequestExpectedResultTuple.requestExpectedErrorResultTuple(requestObject, expectedResult);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InstantiationBuilder applyCustomUseCaseMethodCallingConfiguration(final Step3Builder<?> step3Builder) {
        return step3Builder.callingBy((useCase, event, callingContext) -> {
            final Map<String, String> requestMap = (Map<String, String>) event;
            final String message = requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
            final CheckedTestException checkedTestException = CheckedTestException.checkedTestException(message);
            final CheckedExceptionThrowingRequest request = CheckedExceptionThrowingRequest.checkedExceptionThrowingRequest(checkedTestException);

            final CheckedExceptionThrowingUseCase checkedExceptionThrowingUseCase = (CheckedExceptionThrowingUseCase) useCase;
            checkedExceptionThrowingUseCase.useCaseMethod(request);
            return Map.of("INVALID_RESPONSE", "");
        });
    }

    @Override
    public RequestSerializationStep1Builder applyCustomUseCaseInstantiationConfiguration(
            final InstantiationBuilder instantiationBuilder) {
        return instantiationBuilder.obtainingUseCaseInstancesUsing(new UseCaseInstantiator() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T instantiate(final Class<T> type) {
                return (T) new CheckedExceptionThrowingUseCase();
            }
        });
    }
}
