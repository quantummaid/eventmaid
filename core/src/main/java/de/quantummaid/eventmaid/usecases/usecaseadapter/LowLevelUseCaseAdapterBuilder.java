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

package de.quantummaid.eventmaid.usecases.usecaseadapter;

import de.quantummaid.eventmaid.internal.collections.filtermap.FilterMapBuilder;
import de.quantummaid.eventmaid.internal.collections.predicatemap.PredicateMapBuilder;
import de.quantummaid.eventmaid.mapping.*;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.usecases.usecaseadapter.parameterinjecting.ParameterInjectionInformation;
import de.quantummaid.eventmaid.usecases.usecaseadapter.parameterinjecting.ParameterInjector;
import de.quantummaid.eventmaid.usecases.usecaseadapter.parameterinjecting.ParameterInjectorBuilder;
import de.quantummaid.eventmaid.usecases.usecaseadapter.usecasecalling.Caller;
import de.quantummaid.eventmaid.usecases.usecaseadapter.usecaseinstantiating.UseCaseInstantiator;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static de.quantummaid.eventmaid.mapping.Deserializer.deserializer;
import static de.quantummaid.eventmaid.mapping.ExceptionSerializer.exceptionSerializer;
import static de.quantummaid.eventmaid.mapping.Serializer.serializer;
import static de.quantummaid.eventmaid.usecases.usecaseadapter.UseCaseAdapterImpl.useCaseAdapterImpl;
import static de.quantummaid.eventmaid.usecases.usecaseadapter.UseCaseCallingInformation.useCaseInvocationInformation;
import static de.quantummaid.eventmaid.usecases.usecaseadapter.parameterinjecting.ParameterInjectorBuilder.aParameterInjectorBuilder;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class LowLevelUseCaseAdapterBuilder {
    private final List<UseCaseCallingInformation<?>> useCaseCallingInformationList = new LinkedList<>();
    private final ParameterInjectorBuilder parameterInjectorBuilder = aParameterInjectorBuilder();
    private PredicateMapBuilder<Object, Mapifier<Object>> requestSerializers;
    private FilterMapBuilder<Class<?>, Object, Demapifier<?>> requestDeserializers;
    private PredicateMapBuilder<Object, Mapifier<Object>> responseSerializers;
    private PredicateMapBuilder<Exception, Mapifier<Exception>> exceptionSerializers;
    private FilterMapBuilder<Class<?>, Object, Demapifier<?>> responseDeserializers;
    private UseCaseInstantiator useCaseInstantiator;

    public static LowLevelUseCaseAdapterBuilder aLowLevelUseCaseInvocationBuilder() {
        return new LowLevelUseCaseAdapterBuilder();
    }

    public <U> void addUseCase(final Class<U> useCaseClass, final EventType eventType, final Caller<U> caller) {
        final UseCaseCallingInformation<U> invocationInformation = useCaseInvocationInformation(useCaseClass, eventType, caller);
        useCaseCallingInformationList.add(invocationInformation);
    }

    public void setUseCaseInstantiator(final UseCaseInstantiator useCaseInstantiator) {
        this.useCaseInstantiator = useCaseInstantiator;
    }

    public void setRequestSerializers(final PredicateMapBuilder<Object, Mapifier<Object>> requestSerializers) {
        this.requestSerializers = requestSerializers;
    }

    public void setRequestDeserializers(
            final FilterMapBuilder<Class<?>, Object, Demapifier<?>> requestDeserializers) {
        this.requestDeserializers = requestDeserializers;
    }

    public void setReseponseSerializers(final PredicateMapBuilder<Object, Mapifier<Object>> responseSerializers) {
        this.responseSerializers = responseSerializers;
    }

    public void setExceptionSerializers(final PredicateMapBuilder<Exception, Mapifier<Exception>> exceptionSerializers) {
        this.exceptionSerializers = exceptionSerializers;
    }

    public void setResponseDeserializers(
            final FilterMapBuilder<Class<?>, Object, Demapifier<?>> responseDeserializers) {
        this.responseDeserializers = responseDeserializers;
    }

    public <T> void injectForClass(final Class<T> parameterClass,
                                   final Function<ParameterInjectionInformation, T> injectionFunction) {
        parameterInjectorBuilder.withAnInjection(parameterClass, injectionFunction);
    }

    public UseCaseAdapter build() {
        final Serializer requestSerializer = serializer(requestSerializers.build());
        final Deserializer requestDeserializer = deserializer(requestDeserializers.build());
        final Serializer responseSerializer = serializer(responseSerializers.build());
        final ExceptionSerializer exceptionSerializer = exceptionSerializer(exceptionSerializers.build());
        final Deserializer responseDeserializer = deserializer(responseDeserializers.build());
        final ParameterInjector parameterInjector = parameterInjectorBuilder.build();
        return useCaseAdapterImpl(useCaseCallingInformationList, useCaseInstantiator,
                requestSerializer, requestDeserializer, responseSerializer, exceptionSerializer, responseDeserializer,
                parameterInjector);
    }
}
