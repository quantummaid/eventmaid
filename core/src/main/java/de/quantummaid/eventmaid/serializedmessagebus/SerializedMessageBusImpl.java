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

package de.quantummaid.eventmaid.serializedmessagebus;

import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.identification.MessageId;
import de.quantummaid.eventmaid.mapping.Deserializer;
import de.quantummaid.eventmaid.mapping.Serializer;
import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.messagefunction.MessageFunction;
import de.quantummaid.eventmaid.messagefunction.MessageFunctionBuilder;
import de.quantummaid.eventmaid.messagefunction.ResponseFuture;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.processingcontext.ProcessingContext;
import de.quantummaid.eventmaid.subscribing.AcceptingBehavior;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import de.quantummaid.eventmaid.usecases.payloadanderrorpayload.PayloadAndErrorPayload;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static de.quantummaid.eventmaid.processingcontext.ProcessingContext.processingContextForPayloadAndError;
import static de.quantummaid.eventmaid.usecases.payloadanderrorpayload.PayloadAndErrorPayload.payloadAndErrorPayload;
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
    public MessageId send(final EventType eventType, final Object data) {
        return messageBus.send(eventType, data);
    }

    @Override
    public MessageId send(final EventType eventType, final Object data, final CorrelationId correlationId) {
        return messageBus.send(eventType, data, correlationId);
    }

    @Override
    public MessageId send(final EventType eventType, final Object data, final Object errorData) {
        final ProcessingContext<Object> processingContext = processingContextForPayloadAndError(eventType, data, errorData);
        return messageBus.send(processingContext);
    }

    @Override
    public MessageId send(final EventType eventType,
                          final Object data,
                          final Object errorData,
                          final CorrelationId correlationId) {
        final ProcessingContext<Object> processingContext = processingContextForPayloadAndError(eventType, correlationId,
                data, errorData);
        return messageBus.send(processingContext);
    }

    @Override
    public MessageId serializeAndSend(final EventType eventType, final Object data) {
        final Object map = requestSerializer.serialize(data);
        return send(eventType, map);
    }

    @Override
    public MessageId serializeAndSend(final EventType eventType, final Object data, final CorrelationId correlationId) {
        final Object map = requestSerializer.serialize(data);
        return send(eventType, map, correlationId);
    }

    @Override
    public MessageId serializeAndSend(final EventType eventType, final Object data, final Object errorData) {
        final Object map = requestSerializer.serialize(data);
        return send(eventType, map, requestSerializer.serialize(errorData));
    }

    @Override
    public MessageId serializeAndSend(final EventType eventType,
                                      final Object data,
                                      final Object errorData,
                                      final CorrelationId correlationId) {
        final Object payloadMap = requestSerializer.serialize(data);
        final Object errorPayloadMap = requestSerializer.serialize(errorData);
        return send(eventType, payloadMap, errorPayloadMap, correlationId);
    }

    @Override
    public PayloadAndErrorPayload<Object, Object> invokeAndWait(final EventType eventType,
                                                                final Object data)
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
    public PayloadAndErrorPayload<Object, Object> invokeAndWait(final EventType eventType,
                                                                final Object data,
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
    public PayloadAndErrorPayload<Object, Object> invokeAndWaitSerializedOnly(
            final EventType eventType,
            final Object data) throws InterruptedException, ExecutionException {
        final Object map = serializeWithExecutionExceptionWrapper(data);
        return invokeAndWait(eventType, map);
    }

    @Override
    public PayloadAndErrorPayload<Object, Object> invokeAndWaitSerializedOnly(
            final EventType eventType,
            final Object data,
            final long timeout,
            final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        final Object map = serializeWithExecutionExceptionWrapper(data);
        return invokeAndWait(eventType, map, timeout, unit);
    }

    @Override
    public <P, E> PayloadAndErrorPayload<P, E> invokeAndWaitDeserialized(final EventType eventType,
                                                                         final Object data,
                                                                         final Class<P> responseClass,
                                                                         final Class<E> errorPayloadClass)
            throws InterruptedException, ExecutionException {
        final Object map = serializeWithExecutionExceptionWrapper(data);
        final PayloadAndErrorPayload<Object, Object> mapPayloadAndErrorPayload =
                invokeAndWait(eventType, map);
        return deserializeWithExecutionExceptionWrapper(responseClass, errorPayloadClass, mapPayloadAndErrorPayload);
    }

    @Override
    public <P, E> PayloadAndErrorPayload<P, E> invokeAndWaitDeserialized(final EventType eventType,
                                                                         final Object data,
                                                                         final Class<P> responseClass,
                                                                         final Class<E> errorPayloadClass,
                                                                         final long timeout,
                                                                         final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        final Object map = serializeWithExecutionExceptionWrapper(data);
        final PayloadAndErrorPayload<Object, Object> mapPayloadAndErrorPayload =
                invokeAndWait(eventType, map, timeout, unit);
        return deserializeWithExecutionExceptionWrapper(responseClass, errorPayloadClass, mapPayloadAndErrorPayload);
    }

    private Object serializeWithExecutionExceptionWrapper(final Object data) throws ExecutionException {
        try {
            return requestSerializer.serialize(data);
        } catch (final Exception e) {
            throw new ExecutionException(e);
        }
    }

    private <P, E> PayloadAndErrorPayload<P, E> deserializeWithExecutionExceptionWrapper(
            final Class<P> responseClass,
            final Class<E> errorPayloadClass,
            final PayloadAndErrorPayload<Object, Object> mapPayloadAndErrorPayload)
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
        return (Subscriber<ProcessingContext<Object>>) (Subscriber) subscriber; //NOSONAR
    }

    private <P, E> PayloadAndErrorPayload<P, E> deserialize(
            final PayloadAndErrorPayload<Object, Object> mapPayloadAndErrorPayload,
            final Class<P> responseClass,
            final Class<E> errorPayloadClass) {
        final Object payloadMap = mapPayloadAndErrorPayload.getPayload();
        final Object errorPayloadMap = mapPayloadAndErrorPayload.getErrorPayload();
        return deserialize(payloadMap, responseClass, errorPayloadMap, errorPayloadClass);
    }

    private <P, E> PayloadAndErrorPayload<P, E> deserialize(final Object payloadMap,
                                                            final Class<P> responseClass,
                                                            final Object errorPayloadMap,
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
