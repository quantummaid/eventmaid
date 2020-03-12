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

package de.quantummaid.eventmaid.useCases.injectedParameter;

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
public class InjectedParameterInvocationConfiguration implements UseCaseInvocationConfiguration {
    private static final String PARAMETER_MAP_PROPERTY_NAME = "normalParam";
    private static final String RETURN_MAP_PROPERTY_NAME = "returnValue";
    private static final String INJECTED_STRING_VALUE = "injected";

    @Override
    public Class<?> getUseCaseClass() {
        return InjectedParameterUseCase.class;
    }

    @Override
    public EventType getEventTypeUseCaseIsRegisteredFor() {
        return eventTypeFromString("InjectedParameterUseCase");
    }

    @Override
    public DeAndSerializationDefinition<RequestSerializationStep1Builder> getRequestSerializationDefinitions() {
        return requestSerializationStep1Builder -> {
            requestSerializationStep1Builder.serializingUseCaseRequestOntoTheBusOfType(NormalParameter.class)
                    .using(object -> {
                        final NormalParameter normalParameter = (NormalParameter) object;
                        return Map.of(PARAMETER_MAP_PROPERTY_NAME, normalParameter.getMessage());
                    });
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public DeAndSerializationDefinition<RequestDeserializationStep1Builder> getRequestDeserializationDefinitions() {
        return requestDeserializationStep1Builder -> {
            requestDeserializationStep1Builder.deserializingRequestsToUseCaseParametersOfType(NormalParameter.class)
                    .using((targetType, map) -> NormalParameter.normalParameter((String) ((Map<Object, Object>) map).get(PARAMETER_MAP_PROPERTY_NAME)));
        };
    }

    @Override
    public DeAndSerializationDefinition<ResponseSerializationStep1Builder> getResponseSerializationDefinitions() {
        return responseSerializationStep1Builder -> {
            responseSerializationStep1Builder.serializingUseCaseResponseBackOntoTheBusOfType(String.class)
                    .using(object -> Map.of(RETURN_MAP_PROPERTY_NAME, object));
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
            responseDeserializationStep1Builder.deserializingUseCaseResponsesOfType(String.class)
                    .using((targetType, map) -> (String) ((Map<Object, Object>) map).get(RETURN_MAP_PROPERTY_NAME));
        };
    }

    @Override
    public RequestExpectedResultTuple createRequestExpectedResultTuple() {
        final String message = UUID.randomUUID().toString();
        final NormalParameter requestObject = NormalParameter.normalParameter(message);
        final String expectedResult = message + INJECTED_STRING_VALUE;
        return RequestExpectedResultTuple.requestExpectedResultTuple(requestObject, expectedResult);
    }

    @Override
    public RequestExpectedResultTuple createSerializedRequestExpectedResultTuple() {
        final String message = UUID.randomUUID().toString();
        final Map<String, String> requestObject = Map.of(PARAMETER_MAP_PROPERTY_NAME, message);
        final String expectedPayload = message + INJECTED_STRING_VALUE;
        final Map<String, String> expectedResult = Map.of(RETURN_MAP_PROPERTY_NAME, expectedPayload);
        return RequestExpectedResultTuple.requestExpectedResultTuple(requestObject, expectedResult);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InstantiationBuilder applyCustomUseCaseMethodCallingConfiguration(final Step3Builder<?> step3Builder) {
        return step3Builder.callingBy((useCase, event, callingContext) -> {
            final Map<String, String> requestMap = (Map<String, String>) event;
            final String message = requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
            final NormalParameter normalParameter = NormalParameter.normalParameter(message);
            final InjectedParameter injectedParameter = InjectedParameter.injectedParameter(INJECTED_STRING_VALUE);

            final InjectedParameterUseCase injectedParameterUseCase = (InjectedParameterUseCase) useCase;
            final String response = injectedParameterUseCase.useCaseMethod(normalParameter, injectedParameter);
            return Map.of(RETURN_MAP_PROPERTY_NAME, response);
        });
    }

    @Override
    public RequestSerializationStep1Builder applyCustomUseCaseInstantiationConfiguration(
            final InstantiationBuilder instantiationBuilder) {
        return instantiationBuilder.obtainingUseCaseInstancesUsing(new UseCaseInstantiator() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T instantiate(final Class<T> type) {
                return (T) new InjectedParameterUseCase();
            }
        });
    }

    @Override
    public void applyParameterInjection(final FinalStepBuilder finalStepBuilder) {
        finalStepBuilder.injectParameterForClass(InjectedParameter.class, injectionInformation -> {
            return InjectedParameter.injectedParameter(INJECTED_STRING_VALUE);
        });
    }
}
