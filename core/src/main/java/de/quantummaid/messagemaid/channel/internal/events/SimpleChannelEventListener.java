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

package de.quantummaid.messagemaid.channel.internal.events;

import de.quantummaid.messagemaid.channel.internal.statistics.PartialCollectingChannelStatisticsCollector;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SimpleChannelEventListener<T> implements ChannelEventListener<T> {
    private final PartialCollectingChannelStatisticsCollector statisticsCollector;

    public static <T> SimpleChannelEventListener<T> simpleChannelEventListener(
            final PartialCollectingChannelStatisticsCollector statisticsCollector) {
        return new SimpleChannelEventListener<>(statisticsCollector);
    }

    @Override
    public void messageBlocked(final T message) {
        statisticsCollector.informMessageBlocked();
    }

    @Override
    public void messageForgotten(final T message) {
        statisticsCollector.informMessageForgotten();
    }

    @Override
    public void exceptionInFilter(final T message, final Exception e) {
        statisticsCollector.informExceptionInFilterThrown();
    }
}
