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

package de.quantummaid.messagemaid.internal.pipe.events;

import de.quantummaid.messagemaid.internal.pipe.statistics.PipeStatisticsCollector;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class SimplePipeEventListener<T> implements PipeEventListener<T> {
    private final PipeStatisticsCollector statisticsCollector;

    @Override
    public void messageAccepted(final T message) {
        statisticsCollector.informMessageAccepted();
    }

    @Override
    public void messageQueued(final T message) {
        statisticsCollector.informMessageQueued();
    }

    @Override
    public void messageDequeued(final T message) {
        statisticsCollector.informMessageDequeued();
    }

    @Override
    public void messageDeliverySucceeded(final T message) {
        statisticsCollector.informMessageDeliveredSucceeded();
    }

    @Override
    public void messageDeliveryFailed(final T message, final Exception cause) {
        statisticsCollector.informMessageDeliveryFailed();
    }
}
