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

package de.quantummaid.eventmaid.messagebus.internal;

import de.quantummaid.eventmaid.channel.Channel;
import de.quantummaid.eventmaid.messagebus.MessageBusStatusInformation;
import de.quantummaid.eventmaid.messagebus.exception.MessageBusExceptionListener;
import de.quantummaid.eventmaid.messagebus.internal.brokering.MessageBusBrokerStrategy;
import de.quantummaid.eventmaid.messagebus.internal.exception.ExceptionListenerHandler;
import de.quantummaid.eventmaid.messagebus.internal.statistics.MessageBusStatisticsCollector;
import de.quantummaid.eventmaid.messagebus.statistics.MessageBusStatistics;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusStatusInformationAdapter implements MessageBusStatusInformation {
    private final MessageBusStatisticsCollector statisticsCollector;
    private final MessageBusBrokerStrategy brokerStrategy;
    private final ExceptionListenerHandler exceptionListenerHandler;

    public static MessageBusStatusInformationAdapter statusInformationAdapter(
            @NonNull final MessageBusStatisticsCollector statisticsCollector,
            @NonNull final MessageBusBrokerStrategy brokerStrategy,
            @NonNull final ExceptionListenerHandler exceptionListenerHandler) {
        return new MessageBusStatusInformationAdapter(statisticsCollector, brokerStrategy, exceptionListenerHandler);
    }

    @Override
    public MessageBusStatistics getCurrentMessageStatistics() {
        return statisticsCollector.getStatistics();
    }

    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        return brokerStrategy.getAllSubscribers();
    }

    @Override
    public Channel<Object> getChannelFor(final EventType eventType) {
        return brokerStrategy.getDeliveringChannelFor(eventType);
    }

    @Override
    public Map<EventType, List<Subscriber<?>>> getSubscribersPerType() {
        return brokerStrategy.getSubscribersPerType();
    }

    @Override
    public List<MessageBusExceptionListener> getAllExceptionListener() {
        return exceptionListenerHandler.allListener();
    }
}
