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

package de.quantummaid.messagemaid.serializedMessageBus;

import de.quantummaid.messagemaid.identification.CorrelationId;
import de.quantummaid.messagemaid.identification.MessageId;
import de.quantummaid.messagemaid.mapping.Deserializer;
import de.quantummaid.messagemaid.mapping.Serializer;
import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.messageFunction.MessageFunction;
import de.quantummaid.messagemaid.messageFunction.MessageFunctionBuilder;
import de.quantummaid.messagemaid.messageFunction.ResponseFuture;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.processingContext.ProcessingContext;
import de.quantummaid.messagemaid.subscribing.AcceptingBehavior;
import de.quantummaid.messagemaid.subscribing.Subscriber;
import de.quantummaid.messagemaid.subscribing.SubscriptionId;
import de.quantummaid.messagemaid.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static de.quantummaid.messagemaid.useCases.payloadAndErrorPayload.PayloadAndErrorPayload.payloadAndErrorPayload;
import static lombok.AccessLevel.PRIVATE;

public class SerializedMessageBusImpl implements SerializedMessageBus {
    private final MessageBus messageBus;
    private final Serializer requestSerializer;
    private final Deserializer responseDeserializer;
    private final MessageFunction messageFunction;

    SerializedMessageBusImpl(final MessageBus messageBus,
                             final Serializer requestSerializer,
                             final Deserializer responseDeserializer) {
        this.messageBus = messageBus;
        this.responseDeserializer = responseDeserializer;
        this.requestSerializer = requestSerializer;
        this.messageFunction = MessageFunctionBuilder.aMessageFunction(messageBus);
    }

    @Override
    public MessageId send(final EventType eventType, final Map<String, Object> data) {
        return messageBus.send(eventType, data);
    }

    @Override
    public MessageId send(final EventType eventType, final Map<String, Object> data, final CorrelationId correlationId) {
        return messageBus.send(eventType, data, correlationId);
    }

    @Override
    public MessageId send(final EventType eventType, final Map<String, Object> data, final Map<String, Object> errorData) {
        final ProcessingContext<Object> processingContext = ProcessingContext.processingContextForPayloadAndError(eventType, data, errorData);
        return messageBus.send(processingContext);
    }

    @Override
    public MessageId send(final EventType eventType, final Map<String, Object> data,
                          final Map<String, Object> errorData,
                          final CorrelationId correlationId) {
        final ProcessingContext<Object> processingContext = ProcessingContext.processingContextForPayloadAndError(eventType, correlationId,
                data, errorData);
        return messageBus.send(processingContext);
    }

    @Override
    public MessageId serializeAndSend(final EventType eventType, final Object data) {
        final Map<String, Object> map = requestSerializer.serialize(data);
        return send(eventType, map);
    }

    @Override
    public MessageId serializeAndSend(final EventType eventType, final Object data, final CorrelationId correlationId) {
        final Map<String, Object> map = requestSerializer.serialize(data);
        return send(eventType, map, correlationId);
    }

    @Override
    public MessageId serializeAndSend(final EventType eventType, final Object data, final Object errorData) {
        final Map<String, Object> map = requestSerializer.serialize(data);
        return send(eventType, map, requestSerializer.serialize(errorData));
    }

    @Override
    public MessageId serializeAndSend(final EventType eventType,
                                      final Object data,
                                      final Object errorData,
                                      final CorrelationId correlationId) {
        final Map<String, Object> payloadMap = requestSerializer.serialize(data);
        final Map<String, Object> errorPayloadMap = requestSerializer.serialize(errorData);
        return send(eventType, payloadMap, errorPayloadMap, correlationId);
    }

    @Override
    public PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWait(final EventType eventType,
                                                                                          final Map<String, Object> data)
            throws ExecutionException, InterruptedException {
        final ResponseFuture responseFuture = messageFunction.request(eventType, data);
        try {
            final ProcessingContext<Object> processingContext = responseFuture.getRaw();
            final Map<String, Object> payload = getPayloadAsMap(processingContext);
            final Map<String, Object> errorPayload = getErrorPayloadAsMap(processingContext);
            return payloadAndErrorPayload(payload, errorPayload);
        } finally {
            responseFuture.cancel(true);
        }
    }

    @Override
    public PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWait(final EventType eventType,
                                                                                          final Map<String, Object> data,
                                                                                          final long timeout,
                                                                                          final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        final ResponseFuture responseFuture = messageFunction.request(eventType, data);
        try {
            final ProcessingContext<Object> processingContext = responseFuture.getRaw(timeout, unit);
            final Map<String, Object> payload = getPayloadAsMap(processingContext);
            final Map<String, Object> errorPayload = getErrorPayloadAsMap(processingContext);
            return payloadAndErrorPayload(payload, errorPayload);
        } finally {
            responseFuture.cancel(true);
        }
    }

    @Override
    public PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWaitSerializedOnly(
            final EventType eventType,
            final Object data) throws InterruptedException, ExecutionException {
        final Map<String, Object> map = serializeWithExecutionExceptionWrapper(data);
        return invokeAndWait(eventType, map);
    }

    @Override
    public PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWaitSerializedOnly(
            final EventType eventType,
            final Object data,
            final long timeout,
            final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        final Map<String, Object> map = serializeWithExecutionExceptionWrapper(data);
        return invokeAndWait(eventType, map, timeout, unit);
    }

    @Override
    public <P, E> PayloadAndErrorPayload<P, E> invokeAndWaitDeserialized(final EventType eventType,
                                                                         final Object data,
                                                                         final Class<P> responseClass,
                                                                         final Class<E> errorPayloadClass)
            throws InterruptedException, ExecutionException {
        final Map<String, Object> map = serializeWithExecutionExceptionWrapper(data);
        final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> mapPayloadAndErrorPayload =
                invokeAndWait(eventType, map);
        final PayloadAndErrorPayload<P, E> payloadAndErrorPayload = deserializeWithExecutionExceptionWrapper(responseClass,
                errorPayloadClass, mapPayloadAndErrorPayload);
        return payloadAndErrorPayload;
    }

    @Override
    public <P, E> PayloadAndErrorPayload<P, E> invokeAndWaitDeserialized(final EventType eventType,
                                                                         final Object data,
                                                                         final Class<P> responseClass,
                                                                         final Class<E> errorPayloadClass,
                                                                         final long timeout,
                                                                         final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        final Map<String, Object> map = serializeWithExecutionExceptionWrapper(data);
        final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> mapPayloadAndErrorPayload =
                invokeAndWait(eventType, map, timeout, unit);
        final PayloadAndErrorPayload<P, E> payloadAndErrorPayload = deserializeWithExecutionExceptionWrapper(responseClass,
                errorPayloadClass, mapPayloadAndErrorPayload);
        return payloadAndErrorPayload;
    }

    private Map<String, Object> serializeWithExecutionExceptionWrapper(final Object data) throws ExecutionException {
        try {
            return requestSerializer.serialize(data);
        } catch (final Exception e) {
            throw new ExecutionException(e);
        }
    }

    private <P, E> PayloadAndErrorPayload<P, E> deserializeWithExecutionExceptionWrapper(
            final Class<P> responseClass,
            final Class<E> errorPayloadClass,
            final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> mapPayloadAndErrorPayload)
            throws ExecutionException {
        try {
            return deserialize(mapPayloadAndErrorPayload, responseClass, errorPayloadClass);
        } catch (final Exception e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public SubscriptionId subscribe(
            final EventType eventType,
            final Subscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber) {
        return messageBus.subscribeRaw(eventType, new PayloadAndErrorPayloadSubscriberWrapper(subscriber));
    }

    @Override
    public SubscriptionId subscribe(
            final CorrelationId correlationId,
            final Subscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber) {
        return messageBus.subscribe(correlationId, new PayloadAndErrorPayloadSubscriberWrapper(subscriber));
    }

    @Override
    public <P, E> SubscriptionId subscribeDeserialized(final EventType eventType,
                                                       final Subscriber<PayloadAndErrorPayload<P, E>> subscriber,
                                                       final Class<P> responseClass,
                                                       final Class<E> errorClass) {
        return messageBus.subscribeRaw(eventType, new DeserializingSubscriberWrapper<>(subscriber, responseClass, errorClass));
    }

    @Override
    public <P, E> SubscriptionId subscribeDeserialized(final CorrelationId correlationId,
                                                       final Subscriber<PayloadAndErrorPayload<P, E>> subscriber,
                                                       final Class<P> responseClass,
                                                       final Class<E> errorClass) {
        return messageBus.subscribe(correlationId, new DeserializingSubscriberWrapper<>(subscriber, responseClass, errorClass));
    }

    @Override
    public SubscriptionId subscribeRaw(final EventType eventType,
                                       final Subscriber<ProcessingContext<Map<String, Object>>> subscriber) {
        final Subscriber<ProcessingContext<Object>> castedSubscriber = castSubscriber(subscriber);
        return messageBus.subscribeRaw(eventType, castedSubscriber);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Subscriber<ProcessingContext<Object>> castSubscriber(
            final Subscriber<ProcessingContext<Map<String, Object>>> subscriber) {
        final Subscriber genericErasedSubscriber = subscriber;
        return (Subscriber<ProcessingContext<Object>>) genericErasedSubscriber;
    }

    private <P, E> PayloadAndErrorPayload<P, E> deserialize(
            final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> mapPayloadAndErrorPayload,
            final Class<P> responseClass,
            final Class<E> errorPayloadClass) {
        final Map<String, Object> payloadMap = mapPayloadAndErrorPayload.getPayload();
        final Map<String, Object> errorPayloadMap = mapPayloadAndErrorPayload.getErrorPayload();
        return deserialize(payloadMap, responseClass, errorPayloadMap, errorPayloadClass);
    }

    private <P, E> PayloadAndErrorPayload<P, E> deserialize(final Map<String, Object> payloadMap,
                                                            final Class<P> responseClass,
                                                            final Map<String, Object> errorPayloadMap,
                                                            final Class<E> errorPayloadClass) {
        final P payload;
        if (payloadMap != null) {
            payload = responseDeserializer.deserialize(responseClass, payloadMap);
        } else {
            payload = null;
        }
        final E errorPayload;
        if (errorPayloadMap != null) {
            if (errorPayloadClass != null) {
                errorPayload = responseDeserializer.deserialize(errorPayloadClass, errorPayloadMap);
            } else {
                throw MissingErrorPayloadClassForDeserialization.missingErrorPayloadClassForDeserialization();
            }
        } else {
            errorPayload = null;
        }
        return payloadAndErrorPayload(payload, errorPayload);
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        messageBus.unsubcribe(subscriptionId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getPayloadAsMap(final ProcessingContext<Object> processingContext) {
        return (Map<String, Object>) processingContext.getPayload();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getErrorPayloadAsMap(final ProcessingContext<Object> processingContext) {
        return (Map<String, Object>) processingContext.getErrorPayload();
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class PayloadAndErrorPayloadSubscriberWrapper implements Subscriber<ProcessingContext<Object>> {
        private final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
        private final Subscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber;

        @Override
        public AcceptingBehavior accept(final ProcessingContext<Object> processingContext) {
            final Map<String, Object> payload = getPayloadAsMap(processingContext);
            final Map<String, Object> errorPayload = getErrorPayloadAsMap(processingContext);
            final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> payloadAndErrorPayload =
                    payloadAndErrorPayload(payload, errorPayload);
            return subscriber.accept(payloadAndErrorPayload);
        }

        @Override
        public SubscriptionId getSubscriptionId() {
            return subscriptionId;
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class DeserializingSubscriberWrapper<P, E> implements Subscriber<ProcessingContext<Object>> {
        private final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
        private final Subscriber<PayloadAndErrorPayload<P, E>> subscriber;
        private final Class<P> responseClass;
        private final Class<E> errorClass;

        @Override
        public AcceptingBehavior accept(final ProcessingContext<Object> processingContext) {
            final Map<String, Object> payloadMap = getPayloadAsMap(processingContext);
            final Map<String, Object> errorPayloadMap = getErrorPayloadAsMap(processingContext);
            final PayloadAndErrorPayload<P, E> pePayloadAndErrorPayload = deserialize(payloadMap, responseClass,
                    errorPayloadMap, errorClass);
            return subscriber.accept(pePayloadAndErrorPayload);
        }

        @Override
        public SubscriptionId getSubscriptionId() {
            return subscriptionId;
        }
    }
}
