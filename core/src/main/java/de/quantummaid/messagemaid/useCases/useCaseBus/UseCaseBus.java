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

package de.quantummaid.messagemaid.useCases.useCaseBus;

import de.quantummaid.messagemaid.mapping.Deserializer;
import de.quantummaid.messagemaid.mapping.Serializer;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.serializedMessageBus.SerializedMessageBus;
import de.quantummaid.messagemaid.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.UseCaseInvocationBuilder;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The use case bus invokes use cases as defined by {@link UseCaseInvocationBuilder}. Requests are serialized using the defined
 * {@link Serializer}. All methods wait until a fitting response was received and can optionally deserialized with the defined
 * {@link Deserializer}.
 */
public interface UseCaseBus {

    static UseCaseBus useCaseBus(final SerializedMessageBus serializedMessageBus) {
        return new UseCaseBusImpl(serializedMessageBus);
    }

    /**
     * Invokes the correct use case based on the {@code EventType} with the send data. The method waits until a response is
     * received or an exception occurred. The response is deserialized based on the given classes.
     *
     * @param eventType         the {@code EventType} to send the request on
     * @param data              the request data
     * @param payloadClass      the class to serialize the response payload to
     * @param errorPayloadClass the class to serialize the error response payload to
     * @param <P>               the type to serialize the response payload to
     * @param <E>               the type to serialize the error response payload to
     * @return the serialized response
     * @throws InterruptedException if the waiting {@link Thread} was interrupted
     * @throws ExecutionException   if an exception occurred
     */
    <P, E> PayloadAndErrorPayload<P, E> invokeAndWait(
            EventType eventType,
            Object data,
            Class<P> payloadClass,
            Class<E> errorPayloadClass) throws InterruptedException, ExecutionException;

    /**
     * Invokes the correct use case based on the {@code EventType} with the send data, which is serialized before sending it. The
     * method waits until a response is received, an exception occurred or the timeout expired. The response is deserialized
     * based on the given classes.
     *
     * @param eventType         the {@code EventType} to send the request on
     * @param data              the request data
     * @param payloadClass      the class to serialize the response payload to
     * @param errorPayloadClass the class to serialize the error response payload to
     * @param timeout           the interval to wait
     * @param unit              the unit to define the interval
     * @param <P>               the type to serialize the response payload to
     * @param <E>               the type to serialize the error response payload to
     * @return the deserialized response
     * @throws InterruptedException if the waiting {@link Thread} was interrupted
     * @throws ExecutionException   if an exception occurred
     * @throws TimeoutException     if the timeout expires
     */
    <P, E> PayloadAndErrorPayload<P, E> invokeAndWait(
            EventType eventType,
            Object data,
            Class<P> payloadClass,
            Class<E> errorPayloadClass,
            long timeout,
            TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Invokes the correct use case based on the {@code EventType} with the send data, which is serialized before sending it. The
     * method waits until a response is received or an exception occurred. The response is not deserialized.
     *
     * @param eventType the {@code EventType} to send the request on
     * @param data      the request data
     * @return the raw response
     * @throws InterruptedException if the waiting {@link Thread} was interrupted
     * @throws ExecutionException   if an exception occurred
     */
    PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWaitNotDeserialized(
            EventType eventType,
            Object data) throws InterruptedException, ExecutionException;

    /**
     * Invokes the correct use case based on the {@code EventType} with the send data, which is serialized before sending it. The
     * method waits until a response is received or an exception occurred. The response is not deserialized.
     *
     * @param eventType the {@code EventType} to send the request on
     * @param data      the request data
     * @param timeout   the interval to wait
     * @param unit      the unit to define the interval
     * @return the raw response
     * @throws InterruptedException if the waiting {@link Thread} was interrupted
     * @throws ExecutionException   if an exception occurred
     * @throws TimeoutException     if the timeout expires
     */
    PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWaitNotDeserialized(
            EventType eventType,
            Object data,
            long timeout,
            TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

}
