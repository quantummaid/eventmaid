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

package de.quantummaid.eventmaid.useCases.severalParameter;

import de.quantummaid.eventmaid.useCases.givenWhenThen.DeAndSerializationDefinition;
import de.quantummaid.eventmaid.useCases.shared.RequestExpectedResultTuple;
import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.useCases.shared.UseCaseInvocationConfiguration;
import de.quantummaid.eventmaid.useCases.building.*;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

import static java.lang.Boolean.valueOf;
import static java.lang.Integer.parseInt;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class SeveralParameterInvocationConfiguration implements UseCaseInvocationConfiguration {
    private static final String PARAMETER_MAP_PROPERTY_NAME_INT = "int";
    private static final String PARAMETER_MAP_PROPERTY_NAME_STRING = "string";
    private static final String PARAMETER_MAP_PROPERTY_NAME_BOOLEAN = "boolean";
    private static final String PARAMETER_MAP_PROPERTY_NAME_OBJECT = "object";

    @Override
    public Class<?> getUseCaseClass() {
        return SeveralParameterUseCase.class;
    }

    @Override
    public EventType getEventTypeUseCaseIsRegisteredFor() {
        return EventType.eventTypeFromString("SeveralParameterRequest");
    }

    @Override
    public DeAndSerializationDefinition<RequestSerializationStep1Builder> getRequestSerializationDefinitions() {
        return requestSerializationStep1Builder -> {
            requestSerializationStep1Builder
                    .serializingUseCaseRequestOntoTheBusOfType(SeveralParameterUseCaseCombinedRequest.class)
                    .using(object -> {
                        final SeveralParameterUseCaseCombinedRequest combinedRequest =
                                (SeveralParameterUseCaseCombinedRequest) object;
                        return Map.of(PARAMETER_MAP_PROPERTY_NAME_INT, combinedRequest.getIntParameter() + "",
                                PARAMETER_MAP_PROPERTY_NAME_STRING, combinedRequest.getStringParameter(),
                                PARAMETER_MAP_PROPERTY_NAME_BOOLEAN, combinedRequest.getBooleanParameter() + "",
                                PARAMETER_MAP_PROPERTY_NAME_OBJECT, combinedRequest.getObjectParameter());
                    });
        };
    }

    @Override
    public DeAndSerializationDefinition<RequestDeserializationStep1Builder> getRequestDeserializationDefinitions() {
        return requestDeserializationStep1Builder -> {
            requestDeserializationStep1Builder
                    .deserializingRequestsToUseCaseParametersOfType(SeveralParameterUseCaseRequest1.class)
                    .using((targetType, map) -> {
                        final int intParameter = parseInt(map.get(PARAMETER_MAP_PROPERTY_NAME_INT) + "");
                        final boolean booleanParameter = valueOf(map.get(PARAMETER_MAP_PROPERTY_NAME_BOOLEAN) + "");
                        return SeveralParameterUseCaseRequest1.severalParameterUseCaseRequest1(intParameter, booleanParameter);
                    })
                    .deserializingRequestsToUseCaseParametersOfType(SeveralParameterUseCaseRequest2.class)
                    .using((targetType, map) -> {
                        final String stringParameter = String.valueOf(map.get(PARAMETER_MAP_PROPERTY_NAME_STRING));
                        final Object objectParameter = map.get(PARAMETER_MAP_PROPERTY_NAME_OBJECT);
                        return SeveralParameterUseCaseRequest2.severalParameterUseCaseRequest2(stringParameter, objectParameter);
                    });
        };
    }

    @Override
    public DeAndSerializationDefinition<ResponseSerializationStep1Builder> getResponseSerializationDefinitions() {
        return responseSerializationStep1Builder -> {
            responseSerializationStep1Builder
                    .serializingUseCaseResponseBackOntoTheBusOfType(SeveralParameterUseCaseResponse.class)
                    .using(response ->
                            Map.of(PARAMETER_MAP_PROPERTY_NAME_INT, response.getIntParameter() + "",
                                    PARAMETER_MAP_PROPERTY_NAME_STRING, response.getStringParameter(),
                                    PARAMETER_MAP_PROPERTY_NAME_BOOLEAN, response.getBooleanParameter() + "",
                                    PARAMETER_MAP_PROPERTY_NAME_OBJECT, response.getObjectParameter()));
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
            responseDeserializationStep1Builder.deserializingUseCaseResponsesOfType(SeveralParameterUseCaseResponse.class)
                    .using((targetType, map) -> {
                        final int intParameter = parseInt(map.get(PARAMETER_MAP_PROPERTY_NAME_INT) + "");
                        final boolean booleanParameter = valueOf(map.get(PARAMETER_MAP_PROPERTY_NAME_BOOLEAN) + "");
                        final String stringParameter = String.valueOf(map.get(PARAMETER_MAP_PROPERTY_NAME_STRING));
                        final Object objectParameter = map.get(PARAMETER_MAP_PROPERTY_NAME_OBJECT);
                        return SeveralParameterUseCaseResponse.severalParameterUseCaseResponse(intParameter, booleanParameter, objectParameter, stringParameter);
                    });
        };
    }

    @Override
    public RequestExpectedResultTuple createRequestExpectedResultTuple() {
        final int intParam = 5;
        final boolean booleanParam = true;
        final String stringParam = UUID.randomUUID().toString();
        final Object objectParam = stringParam;
        final SeveralParameterUseCaseCombinedRequest requestObject = SeveralParameterUseCaseCombinedRequest.severalParameterUseCaseCombinedRequest(intParam,
                booleanParam, objectParam, stringParam);
        final SeveralParameterUseCaseResponse expectedResult = SeveralParameterUseCaseResponse.severalParameterUseCaseResponse(intParam, booleanParam,
                objectParam, stringParam);
        return RequestExpectedResultTuple.requestExpectedResultTuple(requestObject, expectedResult);
    }

    @Override
    public RequestExpectedResultTuple createSerializedRequestExpectedResultTuple() {
        final int intParam = 5;
        final boolean booleanParam = true;
        final String stringParam = UUID.randomUUID().toString();
        final Object objectParam = stringParam;
        final Map<String, String> requestObject = Map.of(PARAMETER_MAP_PROPERTY_NAME_INT, intParam + "",
                PARAMETER_MAP_PROPERTY_NAME_STRING, stringParam,
                PARAMETER_MAP_PROPERTY_NAME_BOOLEAN, booleanParam + "",
                PARAMETER_MAP_PROPERTY_NAME_OBJECT, objectParam.toString());
        final Map<String, String> expectedResult = Map.of(PARAMETER_MAP_PROPERTY_NAME_INT, intParam + "",
                PARAMETER_MAP_PROPERTY_NAME_STRING, stringParam,
                PARAMETER_MAP_PROPERTY_NAME_BOOLEAN, booleanParam + "",
                PARAMETER_MAP_PROPERTY_NAME_OBJECT, objectParam.toString());
        return RequestExpectedResultTuple.requestExpectedResultTuple(requestObject, expectedResult);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InstantiationBuilder applyCustomUseCaseMethodCallingConfiguration(final Step3Builder<?> step3Builder) {
        return step3Builder.callingBy((useCase, event, callingContext) -> {
            final Map<String, String> requestMap = (Map<String, String>) event;
            final int intParameter = parseInt(requestMap.get(PARAMETER_MAP_PROPERTY_NAME_INT));
            final boolean booleanParameter = valueOf(requestMap.get(PARAMETER_MAP_PROPERTY_NAME_BOOLEAN));
            final SeveralParameterUseCaseRequest1 request1 = SeveralParameterUseCaseRequest1.severalParameterUseCaseRequest1(intParameter, booleanParameter);

            final String stringParameter = requestMap.get(PARAMETER_MAP_PROPERTY_NAME_STRING);
            final Object objectParameter = requestMap.get(PARAMETER_MAP_PROPERTY_NAME_OBJECT);
            final SeveralParameterUseCaseRequest2 request2 = SeveralParameterUseCaseRequest2.severalParameterUseCaseRequest2(stringParameter, objectParameter);

            final SeveralParameterUseCase severalParameterUseCase = (SeveralParameterUseCase) useCase;
            final SeveralParameterUseCaseResponse response = severalParameterUseCase.useCaseMethod(request1, request2);

            return Map.of(PARAMETER_MAP_PROPERTY_NAME_INT, response.getIntParameter() + "",
                    PARAMETER_MAP_PROPERTY_NAME_STRING, response.getStringParameter(),
                    PARAMETER_MAP_PROPERTY_NAME_BOOLEAN, response.getBooleanParameter() + "",
                    PARAMETER_MAP_PROPERTY_NAME_OBJECT, response.getObjectParameter().toString());
        });
    }

    @Override
    public RequestSerializationStep1Builder applyCustomUseCaseInstantiationConfiguration(
            final InstantiationBuilder instantiationBuilder) {
        return instantiationBuilder.obtainingUseCaseInstancesUsing(new UseCaseInstantiator() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T instantiate(final Class<T> type) {
                return (T) new SeveralParameterUseCase();
            }
        });
    }
}
