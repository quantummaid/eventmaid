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

package de.quantummaid.eventmaid.useCases.useCaseAdapter;

import de.quantummaid.eventmaid.internal.collections.filtermap.FilterMapBuilder;
import de.quantummaid.eventmaid.internal.collections.predicatemap.PredicateMapBuilder;
import de.quantummaid.eventmaid.mapping.Demapifier;
import de.quantummaid.eventmaid.mapping.Mapifier;
import de.quantummaid.eventmaid.messageBus.MessageBus;
import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.serializedMessageBus.SerializedMessageBus;
import de.quantummaid.eventmaid.useCases.building.*;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.parameterInjecting.ParameterInjectionInformation;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.usecaseCalling.Caller;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.usecaseCalling.SinglePublicUseCaseMethodCaller;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import de.quantummaid.eventmaid.useCases.useCaseBus.UseCaseBus;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static de.quantummaid.eventmaid.internal.collections.filtermap.FilterMapBuilder.filterMapBuilder;
import static de.quantummaid.eventmaid.internal.collections.predicatemap.PredicateMapBuilder.predicateMapBuilder;
import static de.quantummaid.eventmaid.useCases.useCaseAdapter.LowLevelUseCaseAdapterBuilder.aLowLevelUseCaseInvocationBuilder;
import static de.quantummaid.eventmaid.useCases.useCaseAdapter.usecaseCalling.SinglePublicUseCaseMethodCaller.singlePublicUseCaseMethodCaller;
import static de.quantummaid.eventmaid.useCases.useCaseBus.UseCaseBus.useCaseBus;
import static lombok.AccessLevel.PRIVATE;

/**
 * Fluent interface builder to configure either a {@link UseCaseAdapter} or a {@link UseCaseBus}.
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseInvocationBuilder implements Step1Builder, InstantiationBuilder,
        RequestDeserializationStep1Builder, RequestSerializationStep1Builder,
        ResponseSerializationStep1Builder, ExceptionSerializationStep1Builder, ResponseDeserializationStep1Builder,
        FinalStepBuilder {
    private final LowLevelUseCaseAdapterBuilder lowLevelUseCaseAdapterBuilder = aLowLevelUseCaseInvocationBuilder();
    private final PredicateMapBuilder<Object, Mapifier<Object>> requestSerializers = predicateMapBuilder();
    private final FilterMapBuilder<Class<?>, Map<String, Object>, Demapifier<?>> requestDeserializers = filterMapBuilder();
    private final FilterMapBuilder<Class<?>, Map<String, Object>, Demapifier<?>> responseDeserializers = filterMapBuilder();
    private final PredicateMapBuilder<Object, Mapifier<Object>> responseSerializers = predicateMapBuilder();
    private final PredicateMapBuilder<Exception, Mapifier<Exception>> exceptionSerializers = predicateMapBuilder();

    /**
     * Starts the configuration of a {@link UseCaseAdapter}.
     *
     * @return the first step in the fluent builder interface
     */
    public static Step1Builder anUseCaseAdapter() {
        return new UseCaseInvocationBuilder();
    }

    /**
     * Starts the configuration of a {@link UseCaseBus}.
     *
     * @return the first step in the fluent builder interface
     */
    public static Step1Builder anUseCaseBus() {
        return new UseCaseInvocationBuilder();
    }

    @Override
    public <U> Step2Builder<U> invokingUseCase(final Class<U> useCaseClass) {
        return new UseCaseCallingBuilder<>(useCaseClass);
    }

    @Override
    public RequestSerializationStep1Builder obtainingUseCaseInstancesUsing(final UseCaseInstantiator useCaseInstantiator) {
        lowLevelUseCaseAdapterBuilder.setUseCaseInstantiator(useCaseInstantiator);
        return this;
    }

    @Override
    public RequestSerializationStep2Builder<Object> serializingUseCaseRequestOntoTheBusMatching(final Predicate<Object> filter) {
        return mapper -> {
            requestSerializers.put(filter, mapper);
            return this;
        };
    }

    @Override
    public RequestDeserializationStep1Builder serializingUseCaseRequestsByDefaultUsing(final Mapifier<Object> mapper) {
        requestSerializers.setDefaultValue(mapper);
        return this;
    }

    @Override
    public <T> RequestDeserializationStep2Builder<T> deserializeRequestsToUseCaseParametersThat(
            final BiPredicate<Class<?>, Map<String, Object>> filter) {
        return requestMapper -> {
            requestDeserializers.put(filter, requestMapper);
            return this;
        };
    }

    @Override
    public ResponseSerializationStep1Builder deserializeRequestsToUseCaseParametersPerDefault(final Demapifier<Object> mapper) {
        requestDeserializers.setDefaultValue(mapper);
        return this;
    }

    @Override
    public ResponseSerializationStep2Builder<Object> serializingUseCaseResponseBackOntoTheBusThat(
            final Predicate<Object> filter) {
        return mapper -> {
            responseSerializers.put(filter, mapper);
            return this;
        };
    }

    @Override
    public ExceptionSerializationStep1Builder serializingUseCaseResponseBackOntoTheBusByDefaultUsing(
            final Mapifier<Object> mapper) {
        responseSerializers.setDefaultValue(mapper);
        return this;
    }

    @Override
    public ExceptionSerializationStep2Builder<Exception> serializingExceptionsThat(final Predicate<Exception> filter) {
        return mapper -> {
            exceptionSerializers.put(filter, mapper);
            return this;
        };
    }

    @Override
    public ResponseDeserializationStep1Builder serializingExceptionsByDefaultUsing(final Mapifier<Exception> mapper) {
        exceptionSerializers.setDefaultValue(mapper);
        return this;
    }

    @Override
    public <T> ResponseDeserializationStep2Builder<T> deserializingUseCaseResponsesOfThat(
            final BiPredicate<Class<?>, Map<String, Object>> filter) {
        return mapper -> {
            responseDeserializers.put(filter, mapper);
            return this;
        };
    }

    @Override
    public FinalStepBuilder deserializeUseCaseResponsesPerDefault(final Demapifier<Object> mapper) {
        responseDeserializers.setDefaultValue(mapper);
        return this;
    }

    @Override
    public <T> FinalStepBuilder injectParameterForClass(final Class<T> parameterClass,
                                                        final Function<ParameterInjectionInformation, T> injectionFunction) {
        lowLevelUseCaseAdapterBuilder.injectForClass(parameterClass, injectionFunction);
        return this;
    }

    @Override
    public UseCaseBus build(final MessageBus messageBus) {
        final UseCaseAdapter useCaseAdapter = buildAsStandaloneAdapter();
        final SerializedMessageBus serializedMessageBus = useCaseAdapter.attachAndEnhance(messageBus);
        return useCaseBus(serializedMessageBus);
    }

    @Override
    public UseCaseAdapter buildAsStandaloneAdapter() {
        lowLevelUseCaseAdapterBuilder.setRequestSerializers(requestSerializers);
        lowLevelUseCaseAdapterBuilder.setRequestDeserializers(requestDeserializers);
        lowLevelUseCaseAdapterBuilder.setReseponseSerializers(responseSerializers);
        lowLevelUseCaseAdapterBuilder.setExceptionSerializers(exceptionSerializers);
        lowLevelUseCaseAdapterBuilder.setResponseDeserializers(responseDeserializers);
        return lowLevelUseCaseAdapterBuilder.build();
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class UseCaseCallingBuilder<U> implements Step2Builder<U> {
        private final Class<U> useCaseClass;

        @Override
        public Step3Builder<U> forType(final EventType eventType) {
            return new Step3Builder<>() {
                @Override
                public Step1Builder callingTheSingleUseCaseMethod() {
                    final SinglePublicUseCaseMethodCaller<U> caller = singlePublicUseCaseMethodCaller(useCaseClass);
                    return callingBy(caller);
                }

                @Override
                public Step1Builder callingBy(final Caller<U> caller) {
                    lowLevelUseCaseAdapterBuilder.addUseCase(useCaseClass, eventType, caller);
                    return UseCaseInvocationBuilder.this;
                }
            };
        }
    }
}
