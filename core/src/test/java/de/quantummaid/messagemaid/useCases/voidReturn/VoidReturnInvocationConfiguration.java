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

package de.quantummaid.messagemaid.useCases.voidReturn;

import de.quantummaid.messagemaid.useCases.givenWhenThen.DeAndSerializationDefinition;
import de.quantummaid.messagemaid.useCases.shared.RequestExpectedResultTuple;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.useCases.shared.UseCaseInvocationConfiguration;
import de.quantummaid.messagemaid.useCases.building.*;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static de.quantummaid.messagemaid.useCases.voidReturn.VoidReturnRequest.voidReturnRequest;
import static java.util.Collections.emptyMap;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class VoidReturnInvocationConfiguration implements UseCaseInvocationConfiguration {

    @Override
    public Class<?> getUseCaseClass() {
        return VoidReturnUseCase.class;
    }

    @Override
    public EventType getEventTypeUseCaseIsRegisteredFor() {
        return EventType.eventTypeFromString("VoidReturnUseCase");
    }

    @Override
    public DeAndSerializationDefinition<RequestSerializationStep1Builder> getRequestSerializationDefinitions() {
        return requestSerializationStep1Builder -> {
            requestSerializationStep1Builder.serializingUseCaseRequestOntoTheBusOfType(VoidReturnRequest.class)
                    .using(object -> emptyMap());
        };
    }

    @Override
    public DeAndSerializationDefinition<RequestDeserializationStep1Builder> getRequestDeserializationDefinitions() {
        return requestDeserializationStep1Builder -> {
            requestDeserializationStep1Builder.deserializingRequestsToUseCaseParametersOfType(VoidReturnRequest.class)
                    .using((targetType, map) -> voidReturnRequest());
        };
    }

    @Override
    public DeAndSerializationDefinition<ResponseSerializationStep1Builder> getResponseSerializationDefinitions() {
        return responseSerializationStep1Builder -> {
            responseSerializationStep1Builder.serializingResponseObjectsOfTypeVoid()
                    .using(object -> emptyMap());
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
            responseDeserializationStep1Builder.deserializingUseCaseResponsesOfType(Void.class)
                    .using((targetType, map) -> null);
        };
    }

    @Override
    public RequestExpectedResultTuple createRequestExpectedResultTuple() {
        return RequestExpectedResultTuple.requestExpectedResultTuple(voidReturnRequest(), null);
    }

    @Override
    public RequestExpectedResultTuple createSerializedRequestExpectedResultTuple() {
        final Map<String, String> requestObject = emptyMap();
        final Map<String, String> expectedResult = emptyMap();
        return RequestExpectedResultTuple.requestExpectedResultTuple(requestObject, expectedResult);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InstantiationBuilder applyCustomUseCaseMethodCallingConfiguration(final Step3Builder<?> step3Builder) {
        return step3Builder.callingBy((useCase, event, callingContext) -> {
            final VoidReturnUseCase voidReturnUseCase = (VoidReturnUseCase) useCase;
            final VoidReturnRequest request = voidReturnRequest();
            voidReturnUseCase.useCaseMethod(request);
            return emptyMap();
        });
    }

    @Override
    public RequestSerializationStep1Builder applyCustomUseCaseInstantiationConfiguration(
            final InstantiationBuilder instantiationBuilder) {
        return instantiationBuilder.obtainingUseCaseInstancesUsing(new UseCaseInstantiator() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T instantiate(final Class<T> type) {
                return (T) new VoidReturnUseCase();
            }
        });
    }
}
