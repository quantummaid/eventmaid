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

package de.quantummaid.eventmaid.useCases.primitiveReturnType;

import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.useCases.givenWhenThen.DeAndSerializationDefinition;
import de.quantummaid.eventmaid.useCases.shared.RequestExpectedResultTuple;
import de.quantummaid.eventmaid.useCases.shared.UseCaseInvocationConfiguration;
import de.quantummaid.eventmaid.useCases.building.*;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Random;

import static de.quantummaid.eventmaid.useCases.shared.RequestExpectedResultTuple.requestExpectedResultTuple;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class PrimitiveReturnTypeInvocationConfiguration implements UseCaseInvocationConfiguration {
    private static final String PARAMETER_MAP_PROPERTY_NAME = "value";
    private static final String RETURN_MAP_PROPERTY_NAME = "returnValue";
    private static final Random REQUEST_INTEGER_GENERATOR = new Random();

    @Override
    public Class<?> getUseCaseClass() {
        return PrimitiveReturnTypeUseCase.class;
    }

    @Override
    public EventType getEventTypeUseCaseIsRegisteredFor() {
        return EventType.eventTypeFromString("PrimitiveReturnTypeUseCase");
    }

    @Override
    public DeAndSerializationDefinition<RequestSerializationStep1Builder> getRequestSerializationDefinitions() {
        return requestSerializationStep1Builder -> {
            requestSerializationStep1Builder.serializingUseCaseRequestOntoTheBusOfType(PrimitiveReturnTypeRequest.class)
                    .using(object -> {
                        final PrimitiveReturnTypeRequest primitiveReturnTypeRequest = (PrimitiveReturnTypeRequest) object;
                        return Map.of(PARAMETER_MAP_PROPERTY_NAME, primitiveReturnTypeRequest.getValue() + "");
                    });
        };
    }

    @Override
    public DeAndSerializationDefinition<RequestDeserializationStep1Builder> getRequestDeserializationDefinitions() {
        return requestDeserializationStep1Builder -> {
            requestDeserializationStep1Builder.deserializingRequestsToUseCaseParametersOfType(PrimitiveReturnTypeRequest.class)
                    .using((targetType, map) -> {
                        final String stringValue = map.get(PARAMETER_MAP_PROPERTY_NAME) + "";
                        return PrimitiveReturnTypeRequest.primitiveReturnTypeRequest(Integer.parseInt(stringValue));
                    });
        };
    }

    @Override
    public DeAndSerializationDefinition<ResponseSerializationStep1Builder> getResponseSerializationDefinitions() {
        return responseSerializationStep1Builder -> {
            responseSerializationStep1Builder.serializingUseCaseResponseBackOntoTheBusOfType(Integer.class)
                    .using(object -> Map.of(RETURN_MAP_PROPERTY_NAME, object.toString()));
        };
    }

    @Override
    public DeAndSerializationDefinition<ExceptionSerializationStep1Builder> getExceptionsSerializationDefinitions() {
        return exceptionSerializationStep1Builder -> {
            //no exceptions thrown
        };
    }

    @Override
    public DeAndSerializationDefinition<ResponseDeserializationStep1Builder> getResponseDeserializationDefinitions() {
        return responseDeserializationStep1Builder -> {
            responseDeserializationStep1Builder.deserializingUseCaseResponsesOfType(Integer.class)
                    .using((targetType, map) -> Integer.parseInt(map.get(RETURN_MAP_PROPERTY_NAME) + ""));
        };
    }

    @Override
    public RequestExpectedResultTuple createRequestExpectedResultTuple() {
        final int value = REQUEST_INTEGER_GENERATOR.nextInt();
        final PrimitiveReturnTypeRequest requestObject = PrimitiveReturnTypeRequest.primitiveReturnTypeRequest(value);
        return requestExpectedResultTuple(requestObject, value);
    }

    @Override
    public RequestExpectedResultTuple createSerializedRequestExpectedResultTuple() {
        final int value = REQUEST_INTEGER_GENERATOR.nextInt();
        final String stringValue = value + "";
        final Map<String, String> requestObject = Map.of(PARAMETER_MAP_PROPERTY_NAME, stringValue);
        final Map<String, String> expectedResult = Map.of(RETURN_MAP_PROPERTY_NAME, stringValue);
        return requestExpectedResultTuple(requestObject, expectedResult);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InstantiationBuilder applyCustomUseCaseMethodCallingConfiguration(final Step3Builder<?> step3Builder) {
        return step3Builder.callingBy((useCase, event, callingContext) -> {
            final Map<String, String> requestMap = (Map<String, String>) event;
            final int value = Integer.parseInt(requestMap.get(PARAMETER_MAP_PROPERTY_NAME));
            final PrimitiveReturnTypeRequest request = PrimitiveReturnTypeRequest.primitiveReturnTypeRequest(value);

            final PrimitiveReturnTypeUseCase primitiveReturnTypeUseCase = (PrimitiveReturnTypeUseCase) useCase;
            final int response = primitiveReturnTypeUseCase.useCaseMethod(request);
            return Map.of(RETURN_MAP_PROPERTY_NAME, String.valueOf(response));
        });
    }

    @Override
    public RequestSerializationStep1Builder applyCustomUseCaseInstantiationConfiguration(
            final InstantiationBuilder instantiationBuilder) {
        return instantiationBuilder.obtainingUseCaseInstancesUsing(new UseCaseInstantiator() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T instantiate(final Class<T> type) {
                return (T) new PrimitiveReturnTypeUseCase();
            }
        });
    }
}
