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

package de.quantummaid.eventmaid.usecases.usecasebus;

import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.serializedmessagebus.SerializedMessageBus;
import de.quantummaid.eventmaid.usecases.payloadanderrorpayload.PayloadAndErrorPayload;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class UseCaseBusImpl implements UseCaseBus {
    private final SerializedMessageBus serializedMessageBus;

    @Override
    public <P, E> PayloadAndErrorPayload<P, E> invokeAndWait(
            final EventType eventType,
            final Object data,
            final Class<P> payloadClass,
            final Class<E> errorPayloadClass) throws InterruptedException, ExecutionException {
        return serializedMessageBus.invokeAndWaitDeserialized(eventType, data, payloadClass, errorPayloadClass);
    }

    @Override
    public <P, E> PayloadAndErrorPayload<P, E> invokeAndWait(
            final EventType eventType,
            final Object data,
            final Class<P> payloadClass,
            final Class<E> errorPayloadClass,
            final long timeout,
            final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return serializedMessageBus.invokeAndWaitDeserialized(eventType, data, payloadClass, errorPayloadClass, timeout, unit);
    }

    @Override
    public PayloadAndErrorPayload<Object, Object> invokeAndWaitNotDeserialized(
            final EventType eventType,
            final Object data) throws InterruptedException, ExecutionException {
        return serializedMessageBus.invokeAndWaitSerializedOnly(eventType, data);
    }

    @Override
    public PayloadAndErrorPayload<Object, Object> invokeAndWaitNotDeserialized(
            final EventType eventType,
            final Object data,
            final long timeout,
            final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return serializedMessageBus.invokeAndWaitSerializedOnly(eventType, data, timeout, unit);
    }
}