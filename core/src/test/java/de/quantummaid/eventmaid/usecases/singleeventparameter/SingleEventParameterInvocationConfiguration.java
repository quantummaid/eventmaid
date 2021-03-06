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

package de.quantummaid.eventmaid.usecases.singleeventparameter;

import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.usecases.building.*;
import de.quantummaid.eventmaid.usecases.givenwhenthen.DeAndSerializationDefinition;
import de.quantummaid.eventmaid.usecases.shared.RequestExpectedResultTuple;
import de.quantummaid.eventmaid.usecases.shared.UseCaseInvocationConfiguration;
import de.quantummaid.eventmaid.usecases.usecaseadapter.usecaseinstantiating.UseCaseInstantiator;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

import static de.quantummaid.eventmaid.usecases.shared.RequestExpectedResultTuple.requestExpectedResultTuple;
import static de.quantummaid.eventmaid.usecases.singleeventparameter.SingleParameterResponse.singleParameterResponse;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class SingleEventParameterInvocationConfiguration implements UseCaseInvocationConfiguration {
    private static final String PARAMETER_MAP_PROPERTY_NAME = "value";
    private static final String RETURN_MAP_PROPERTY_NAME = "returnValue";

    @Override
    public Class<?> getUseCaseClass() {
        return SingleEventParameterUseCase.class;
    }

    @Override
    public EventType getEventTypeUseCaseIsRegisteredFor() {
        return EventType.eventTypeFromString("SingleParameterEvent");
    }

    @Override
    public DeAndSerializationDefinition<RequestSerializationStep1Builder> getRequestSerializationDefinitions() {
        return requestSerializationStep1Builder -> {
            requestSerializationStep1Builder.serializingUseCaseRequestOntoTheBusOfType(SingleParameterEvent.class)
                    .using(object -> {
                        final SingleParameterEvent singleParameterEvent = (SingleParameterEvent) object;
                        return Map.of(PARAMETER_MAP_PROPERTY_NAME, singleParameterEvent.getMessage());
                    });
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public DeAndSerializationDefinition<RequestDeserializationStep1Builder> getRequestDeserializationDefinitions() {
        return requestDeserializationStep1Builder -> {
            requestDeserializationStep1Builder.deserializingRequestsToUseCaseParametersOfType(SingleParameterEvent.class)
                    .using((targetType, map) -> SingleParameterEvent.singleParameterEvent((String) ((Map<Object, Object>) map).get(PARAMETER_MAP_PROPERTY_NAME)));
        };
    }

    @Override
    public DeAndSerializationDefinition<ResponseSerializationStep1Builder> getResponseSerializationDefinitions() {
        return responseSerializationStep1Builder -> {
            responseSerializationStep1Builder.serializingUseCaseResponseBackOntoTheBusOfType(SingleParameterResponse.class)
                    .using(object -> Map.of(RETURN_MAP_PROPERTY_NAME, object.getMessage()));
        };
    }

    @Override
    public DeAndSerializationDefinition<ExceptionSerializationStep1Builder> getExceptionsSerializationDefinitions() {
        return exceptionSerializationStep1Builder -> {
            //no exceptions thrown
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public DeAndSerializationDefinition<ResponseDeserializationStep1Builder> getResponseDeserializationDefinitions() {
        return responseDeserializationStep1Builder -> {
            responseDeserializationStep1Builder.deserializingUseCaseResponsesOfType(SingleParameterResponse.class)
                    .using((targetType, map) -> singleParameterResponse((String) ((Map<Object, Object>) map).get(RETURN_MAP_PROPERTY_NAME)));
        };
    }

    @Override
    public RequestExpectedResultTuple createRequestExpectedResultTuple() {
        final String message = UUID.randomUUID().toString();
        final SingleParameterEvent requestObject = SingleParameterEvent.singleParameterEvent(message);
        final SingleParameterResponse expectedResult = singleParameterResponse(message);
        return requestExpectedResultTuple(requestObject, expectedResult);
    }

    @Override
    public RequestExpectedResultTuple createSerializedRequestExpectedResultTuple() {
        final String message = UUID.randomUUID().toString();
        final Map<String, String> requestObject = Map.of(PARAMETER_MAP_PROPERTY_NAME, message);
        final Map<String, String> expectedResult = Map.of(RETURN_MAP_PROPERTY_NAME, message);
        return requestExpectedResultTuple(requestObject, expectedResult);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InstantiationBuilder applyCustomUseCaseMethodCallingConfiguration(final Step3Builder<?> step3Builder) {
        return step3Builder.callingBy((useCase, event, callingContext) -> {
            final Map<String, String> requestMap = (Map<String, String>) event;
            final String message = requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
            final SingleParameterEvent request = SingleParameterEvent.singleParameterEvent(message);

            final SingleEventParameterUseCase singleEventParameterUseCase = (SingleEventParameterUseCase) useCase;
            final SingleParameterResponse response = singleEventParameterUseCase.useCaseMethod(request);
            final String responseMessage = response.getMessage();
            return Map.of(RETURN_MAP_PROPERTY_NAME, responseMessage);
        });
    }

    @Override
    public RequestSerializationStep1Builder applyCustomUseCaseInstantiationConfiguration(
            final InstantiationBuilder instantiationBuilder) {
        return instantiationBuilder.obtainingUseCaseInstancesUsing(new UseCaseInstantiator() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T instantiate(final Class<T> type) {
                return (T) new SingleEventParameterUseCase();
            }
        });
    }
}
