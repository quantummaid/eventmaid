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

package de.quantummaid.eventmaid.internal.pipe.transport;

import de.quantummaid.eventmaid.configuration.AsynchronousConfiguration;
import de.quantummaid.eventmaid.internal.pipe.PipeType;
import de.quantummaid.eventmaid.internal.pipe.events.PipeEventListener;
import de.quantummaid.eventmaid.internal.pipe.exceptions.PipeErrorHandler;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TransportMechanismFactory {

    public static <T> TransportMechanism<T> transportMechanism(final PipeType pipeType,
                                                               final PipeEventListener<T> eventListener,
                                                               final PipeErrorHandler<T> errorHandler,
                                                               final List<Subscriber<T>> subscribers,
                                                               final AsynchronousConfiguration asynchronousConfiguration) {
        final SynchronousDelivery<T> synchronousDelivery = new SynchronousDelivery<>(eventListener, errorHandler);
        switch (pipeType) {
            case SYNCHRONOUS:
                return new SynchronousTransportMechanism<>(eventListener, synchronousDelivery, subscribers);
            case ASYNCHRONOUS:
                final ThreadPoolExecutor threadPoolExecutor = createThreadPoolExecutor(asynchronousConfiguration);
                return new AsynchronousTransportMechanism<>(eventListener, synchronousDelivery, subscribers, threadPoolExecutor);
            default:
                throw new IllegalArgumentException("Unknown pipe type " + pipeType);
        }
    }

    private static ThreadPoolExecutor createThreadPoolExecutor(final AsynchronousConfiguration config) {
        final int corePoolSize = config.getCorePoolSize();
        final int maximumPoolSize = config.getMaximumPoolSize();
        final int maximumTimeout = config.getMaximumTimeout();
        final TimeUnit timeoutTimeUnit = config.getTimeoutTimeUnit();
        final BlockingQueue<Runnable> queue = config.getThreadPoolWorkingQueue();
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, maximumTimeout, timeoutTimeUnit, queue);
    }

}
