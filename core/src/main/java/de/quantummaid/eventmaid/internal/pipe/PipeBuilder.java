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

package de.quantummaid.eventmaid.internal.pipe;

import de.quantummaid.eventmaid.configuration.AsynchronousConfiguration;
import de.quantummaid.eventmaid.internal.pipe.error.ErrorThrowingPipeErrorHandler;
import de.quantummaid.eventmaid.internal.pipe.error.PipeErrorHandler;
import de.quantummaid.eventmaid.internal.pipe.events.PipeEventListener;
import de.quantummaid.eventmaid.internal.pipe.events.SimplePipeEventListener;
import de.quantummaid.eventmaid.internal.pipe.statistics.PipeStatisticsCollector;
import de.quantummaid.eventmaid.internal.pipe.transport.TransportMechanism;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import de.quantummaid.eventmaid.internal.pipe.statistics.AtomicPipeStatisticsCollector;
import de.quantummaid.eventmaid.internal.pipe.transport.TransportMechanismFactory;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CopyOnWriteArrayList;

import static de.quantummaid.eventmaid.internal.pipe.PipeType.ASYNCHRONOUS;
import static de.quantummaid.eventmaid.internal.pipe.PipeType.SYNCHRONOUS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeBuilder<T> {
    private PipeType pipeType = SYNCHRONOUS;
    private PipeStatisticsCollector statisticsCollector = AtomicPipeStatisticsCollector.atomicPipeStatisticsCollector();
    private PipeErrorHandler<T> errorHandler = new ErrorThrowingPipeErrorHandler<>();
    private PipeEventListener<T> eventListener;
    private AsynchronousConfiguration asynchronousConfiguration;

    public static <T> PipeBuilder<T> aPipe() {
        return new PipeBuilder<>();
    }

    public static <T> PipeBuilder<T> aPipeForClass(final Class<T> tClass) {
        return new PipeBuilder<>();
    }

    public PipeBuilder<T> ofType(final PipeType pipeType) {
        this.pipeType = pipeType;
        return this;
    }

    public PipeBuilder<T> withAsynchronousConfiguration(final AsynchronousConfiguration configuration) {
        this.asynchronousConfiguration = configuration;
        return this;
    }

    public PipeBuilder<T> withStatisticsCollector(final PipeStatisticsCollector statisticsCollector) {
        this.statisticsCollector = statisticsCollector;
        return this;
    }

    public PipeBuilder<T> withEventListener(final PipeEventListener<T> eventListener) {
        this.eventListener = eventListener;
        return this;
    }

    public PipeBuilder<T> withErrorHandler(final PipeErrorHandler<T> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public Pipe<T> build() {
        final PipeEventListener<T> eventListener = createEventListener();
        final CopyOnWriteArrayList<Subscriber<T>> subscribers = new CopyOnWriteArrayList<>();
        if (pipeType.equals(ASYNCHRONOUS) && asynchronousConfiguration == null) {
            throw new IllegalArgumentException("Asynchronous configuration required.");
        }
        final TransportMechanism<T> tTransportMechanism = TransportMechanismFactory.transportMechanism(pipeType, eventListener, errorHandler,
                subscribers, asynchronousConfiguration);
        return new PipeImpl<>(tTransportMechanism, statisticsCollector, subscribers);
    }

    private PipeEventListener<T> createEventListener() {
        if (eventListener != null) {
            return eventListener;
        } else {
            return new SimplePipeEventListener<>(statisticsCollector);
        }
    }

}
