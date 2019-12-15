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

package de.quantummaid.messagemaid.internal.pipe.transport;

import de.quantummaid.messagemaid.internal.pipe.events.PipeEventListener;
import de.quantummaid.messagemaid.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static lombok.AccessLevel.PUBLIC;

@RequiredArgsConstructor(access = PUBLIC)
public final class SynchronousTransportMechanism<T> implements TransportMechanism<T> {
    private final PipeEventListener<T> eventListener;
    private final SynchronousDelivery<T> synchronousDelivery;
    private final List<Subscriber<T>> subscribers;

    @Override
    public void transport(final T message) {
        eventListener.messageAccepted(message);
        synchronousDelivery.deliver(message, subscribers);
    }

    @Override
    public void close(final boolean finishRemainingTasks) {

    }

    @Override
    public boolean isShutdown() {
        return true;
    }

    @Override
    public boolean awaitTermination(final int timeout, final TimeUnit timeUnit) {
        return true;
    }
}
