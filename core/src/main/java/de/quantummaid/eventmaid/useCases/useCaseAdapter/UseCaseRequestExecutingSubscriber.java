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

import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.mapping.Deserializer;
import de.quantummaid.eventmaid.mapping.ExceptionSerializer;
import de.quantummaid.eventmaid.mapping.Serializer;
import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import de.quantummaid.eventmaid.serializedMessageBus.SerializedMessageBus;
import de.quantummaid.eventmaid.subscribing.AcceptingBehavior;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.parameterInjecting.ParameterInjector;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.usecaseCalling.Caller;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.usecaseCalling.CallingContext;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static de.quantummaid.eventmaid.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static de.quantummaid.eventmaid.subscribing.AcceptingBehavior.MESSAGE_ACCEPTED;
import static de.quantummaid.eventmaid.subscribing.SubscriptionId.newUniqueId;
import static de.quantummaid.eventmaid.useCases.useCaseAdapter.UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
final class UseCaseRequestExecutingSubscriber implements Subscriber<ProcessingContext<Map<String, Object>>> {
    private final UseCaseCallingInformation<?> useCaseCallingInformation;
    private final UseCaseInstantiator useCaseInstantiator;
    private final CallingContext callingContext;
    private final ExceptionSerializer exceptionSerializer;
    private final SubscriptionId subscriptionId = newUniqueId();
    private SerializedMessageBus serializedMessageBus;

    static UseCaseRequestExecutingSubscriber useCaseRequestExecutingSubscriber(
            final UseCaseCallingInformation<?> useCaseCallingInformation,
            final UseCaseInstantiator useCaseInstantiator,
            final Deserializer requestDeserializer,
            final Serializer responseSerializer,
            final ExceptionSerializer exceptionSerializer,
            final ParameterInjector parameterInjector) {
        ensureNotNull(useCaseCallingInformation, "useCaseCallingInformation");
        ensureNotNull(useCaseInstantiator, "useCaseInstantiator");
        ensureNotNull(requestDeserializer, "requestDeserializer");
        ensureNotNull(responseSerializer, "responseSerializer");
        ensureNotNull(exceptionSerializer, "exceptionSerializer");
        ensureNotNull(parameterInjector, "parameterInjector");
        final CallingContext callingContext = CallingContext.callingContext(requestDeserializer, responseSerializer, parameterInjector);
        return new UseCaseRequestExecutingSubscriber(useCaseCallingInformation, useCaseInstantiator,
                callingContext, exceptionSerializer);
    }

    @Override
    public AcceptingBehavior accept(final ProcessingContext<Map<String, Object>> processingContext) {
        @SuppressWarnings("unchecked")
        final Caller<Object> caller = (Caller<Object>) useCaseCallingInformation.getCaller();
        final Class<?> useCaseClass = useCaseCallingInformation.getUseCaseClass();
        final Object useCase = useCaseInstantiator.instantiate(useCaseClass);
        final Map<String, Object> payload = processingContext.getPayload();
        Map<String, Object> serializedReturnValue = null;
        Map<String, Object> serializedException = null;
        try {
            serializedReturnValue = caller.call(useCase, payload, callingContext);
        } catch (final Exception e) {
            serializedException = exceptionSerializer.serializeException(e);
        }
        final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
        serializedMessageBus.send(USE_CASE_RESPONSE_EVENT_TYPE, serializedReturnValue, serializedException, correlationId);
        return MESSAGE_ACCEPTED;
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }

    public void attachTo(final SerializedMessageBus serializedMessageBus) {
        this.serializedMessageBus = serializedMessageBus;
        final EventType eventType = useCaseCallingInformation.getEventType();
        serializedMessageBus.subscribeRaw(eventType, this);
    }
}
