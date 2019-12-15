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

package de.quantummaid.messagemaid.useCases.useCaseAdapter;

import de.quantummaid.messagemaid.internal.collections.filtermap.FilterMapBuilder;
import de.quantummaid.messagemaid.internal.collections.predicatemap.PredicateMapBuilder;
import de.quantummaid.messagemaid.mapping.*;
import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.serializedMessageBus.SerializedMessageBus;
import de.quantummaid.messagemaid.useCases.building.*;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.parameterInjecting.ParameterInjectionInformation;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.parameterInjecting.ParameterInjector;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.parameterInjecting.ParameterInjectorBuilder;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.usecaseCalling.Caller;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.usecaseCalling.SinglePublicUseCaseMethodCaller;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import de.quantummaid.messagemaid.useCases.useCaseBus.UseCaseBus;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static de.quantummaid.messagemaid.internal.collections.filtermap.FilterMapBuilder.filterMapBuilder;
import static de.quantummaid.messagemaid.internal.collections.predicatemap.PredicateMapBuilder.predicateMapBuilder;
import static de.quantummaid.messagemaid.mapping.Deserializer.deserializer;
import static de.quantummaid.messagemaid.mapping.ExceptionSerializer.exceptionSerializer;
import static de.quantummaid.messagemaid.mapping.Serializer.serializer;
import static lombok.AccessLevel.PRIVATE;

/**
 * Fluent interface builder to configure either a {@link UseCaseAdapter} or a {@link UseCaseBus}.
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseInvocationBuilder implements Step1Builder, InstantiationBuilder,
        RequestDeserializationStep1Builder, RequestSerializationStep1Builder,
        ResponseSerializationStep1Builder, ExceptionSerializationStep1Builder, ResponseDeserializationStep1Builder,
        FinalStepBuilder {
    private final List<UseCaseCallingInformation<?>> useCaseCallingInformationList = new LinkedList<>();
    private final PredicateMapBuilder<Object, Mapifier<Object>> requestSerializers = predicateMapBuilder();
    private final FilterMapBuilder<Class<?>, Map<String, Object>, Demapifier<?>> requestDeserializers = filterMapBuilder();
    private final FilterMapBuilder<Class<?>, Map<String, Object>, Demapifier<?>> responseDeserializers = filterMapBuilder();
    private final PredicateMapBuilder<Object, Mapifier<Object>> responseSerializers = predicateMapBuilder();
    private final PredicateMapBuilder<Exception, Mapifier<Exception>> exceptionSerializers = predicateMapBuilder();
    private final ParameterInjectorBuilder parameterInjectorBuilder = ParameterInjectorBuilder.aParameterInjectorBuilder();

    private UseCaseInstantiator useCaseInstantiator;

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
    public RequestSerializationStep1Builder obtainingUseCaseInstancesUsing(final UseCaseInstantiator useCaseInstantiator) {
        this.useCaseInstantiator = useCaseInstantiator;
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
    public <U> Step2Builder<U> invokingUseCase(final Class<U> useCaseClass) {
        return new UseCaseCallingBuilder<>(useCaseClass);
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
        parameterInjectorBuilder.withAnInjection(parameterClass, injectionFunction);
        return this;
    }

    @Override
    public UseCaseBus build(final MessageBus messageBus) {
        final UseCaseAdapter useCaseAdapter = buildAsStandaloneAdapter();
        final SerializedMessageBus serializedMessageBus = useCaseAdapter.attachAndEnhance(messageBus);
        return UseCaseBus.useCaseBus(serializedMessageBus);
    }

    @Override
    public UseCaseAdapter buildAsStandaloneAdapter() {
        final Serializer requestSerializer = serializer(requestSerializers.build());
        final Deserializer requestDeserializer = deserializer(requestDeserializers.build());
        final Serializer responseSerializer = serializer(responseSerializers.build());
        final ExceptionSerializer exceptionSerializer = exceptionSerializer(exceptionSerializers.build());
        final Deserializer responseDeserializer = deserializer(responseDeserializers.build());
        final ParameterInjector parameterInjector = parameterInjectorBuilder.build();
        return UseCaseAdapterImpl.useCaseAdapterImpl(useCaseCallingInformationList, useCaseInstantiator,
                requestSerializer, requestDeserializer, responseSerializer, exceptionSerializer, responseDeserializer,
                parameterInjector);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class UseCaseCallingBuilder<U> implements Step2Builder<U> {
        private final Class<U> useCaseClass;

        @Override
        public Step3Builder<U> forType(final EventType eventType) {
            return new Step3Builder<>() {
                @Override
                public InstantiationBuilder callingTheSingleUseCaseMethod() {
                    final SinglePublicUseCaseMethodCaller<U> caller = SinglePublicUseCaseMethodCaller.singlePublicUseCaseMethodCaller(useCaseClass);
                    return callingBy(caller);
                }

                @Override
                public InstantiationBuilder callingBy(final Caller<U> caller) {
                    final UseCaseCallingInformation<U> invocationInformation = UseCaseCallingInformation.useCaseInvocationInformation(useCaseClass,
                            eventType, caller);
                    useCaseCallingInformationList.add(invocationInformation);
                    return UseCaseInvocationBuilder.this;
                }
            };
        }
    }
}
