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

import de.quantummaid.eventmaid.mapping.Deserializer;
import de.quantummaid.eventmaid.mapping.ExceptionSerializer;
import de.quantummaid.eventmaid.mapping.Serializer;
import de.quantummaid.eventmaid.messageBus.MessageBus;
import de.quantummaid.eventmaid.serializedMessageBus.SerializedMessageBus;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.parameterInjecting.ParameterInjector;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static de.quantummaid.eventmaid.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static de.quantummaid.eventmaid.serializedMessageBus.SerializedMessageBus.aSerializedMessageBus;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
final class UseCaseAdapterImpl implements UseCaseAdapter {
    private final List<UseCaseCallingInformation<?>> useCaseCallingInformations;
    private final UseCaseInstantiator useCaseInstantiator;
    private final Serializer requestSerializer;
    private final Deserializer requestDeserializer;
    private final Serializer responseSerializer;
    private final ExceptionSerializer exceptionSerializer;
    private final Deserializer responseDeserializer;
    private final ParameterInjector parameterInjector;

    static UseCaseAdapter useCaseAdapterImpl(final List<UseCaseCallingInformation<?>> useCaseCallingInformations,
                                             final UseCaseInstantiator useCaseInstantiator,
                                             final Serializer requestSerializer,
                                             final Deserializer requestDeserializer,
                                             final Serializer responseSerializer,
                                             final ExceptionSerializer exceptionSerializer,
                                             final Deserializer responseDeserializer,
                                             final ParameterInjector parameterInjector) {
        ensureNotNull(useCaseCallingInformations, "useCaseCallingInformations");
        ensureNotNull(useCaseInstantiator, "useCaseInstantiator");
        ensureNotNull(requestSerializer, "requestSerializer");
        ensureNotNull(responseDeserializer, "responseDeserializer");
        return new UseCaseAdapterImpl(useCaseCallingInformations, useCaseInstantiator, requestSerializer,
                requestDeserializer, responseSerializer, exceptionSerializer, responseDeserializer, parameterInjector);
    }

    @Override
    public SerializedMessageBus attachAndEnhance(final MessageBus messageBus) {
        final SerializedMessageBus serializedMessageBus = aSerializedMessageBus(messageBus, requestSerializer,
                responseDeserializer);
        attachTo(serializedMessageBus);
        return serializedMessageBus;
    }

    @Override
    public void attachTo(final SerializedMessageBus serializedMessageBus) {
        useCaseCallingInformations.forEach(callingInformation -> {
            final UseCaseRequestExecutingSubscriber useCaseRequestSubscriber = UseCaseRequestExecutingSubscriber.useCaseRequestExecutingSubscriber(
                    callingInformation, useCaseInstantiator, requestDeserializer, responseSerializer, exceptionSerializer,
                    parameterInjector);
            useCaseRequestSubscriber.attachTo(serializedMessageBus);
        });
    }
}
