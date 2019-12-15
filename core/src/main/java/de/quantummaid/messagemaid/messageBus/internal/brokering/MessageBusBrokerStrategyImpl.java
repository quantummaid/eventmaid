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

package de.quantummaid.messagemaid.messageBus.internal.brokering;

import de.quantummaid.messagemaid.channel.Channel;
import de.quantummaid.messagemaid.channel.action.Subscription;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.messageBus.channelCreating.MessageBusChannelFactory;
import de.quantummaid.messagemaid.messageBus.exception.MessageBusExceptionHandler;
import de.quantummaid.messagemaid.processingContext.ProcessingContext;
import de.quantummaid.messagemaid.subscribing.Subscriber;
import de.quantummaid.messagemaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusBrokerStrategyImpl implements MessageBusBrokerStrategy {
    private final Map<EventType, Channel<Object>> channelMap = new ConcurrentHashMap<>();
    private final Map<SubscriptionId, List<EventType>> subscriptionLookupMap = new ConcurrentHashMap<>();
    private final MessageBusChannelFactory channelFactory;
    private final MessageBusExceptionHandler messageBusExceptionHandler;

    public static MessageBusBrokerStrategyImpl messageBusBrokerStrategyImpl2(final MessageBusChannelFactory channelFactory,
                                                                             final MessageBusExceptionHandler exceptionHandler) {
        return new MessageBusBrokerStrategyImpl(channelFactory, exceptionHandler);
    }

    @Override
    public Channel<Object> getDeliveringChannelFor(final EventType eventType) {
        return getOrCreateChannel(eventType);
    }

    private Channel<Object> getOrCreateChannel(final EventType eventType) {
        if (channelMap.containsKey(eventType)) {
            return channelMap.get(eventType);
        } else {
            final Channel<Object> channel = channelFactory.createChannel(eventType, null, messageBusExceptionHandler);
            channelMap.put(eventType, channel);
            return channel;
        }
    }

    @Override
    public void addSubscriber(final EventType eventType, final Subscriber<Object> subscriber) {
        final Channel<Object> channel = getOrCreateChannel(eventType);
        final Subscription<Object> subscription = getChannelSubscription(channel);
        subscription.addSubscriber(subscriber);
        storeSubscriptionForLookup(eventType, subscriber);
    }

    @Override
    public void addRawSubscriber(final EventType eventType, final Subscriber<ProcessingContext<Object>> subscriber) {
        final Channel<Object> channel = getOrCreateChannel(eventType);
        final Subscription<Object> subscription = getChannelSubscription(channel);
        subscription.addRawSubscriber(subscriber);
        storeSubscriptionForLookup(eventType, subscriber);
    }

    private Subscription<Object> getChannelSubscription(final Channel<Object> channel) {
        return (Subscription<Object>) channel.getDefaultAction();
    }

    private void storeSubscriptionForLookup(final EventType eventType, final Subscriber<?> subscriber) {
        final SubscriptionId subscriptionId = subscriber.getSubscriptionId();
        final List<EventType> eventTypes;
        if (subscriptionLookupMap.containsKey(subscriptionId)) {
            eventTypes = subscriptionLookupMap.get(subscriptionId);
        } else {
            eventTypes = new LinkedList<>();
            subscriptionLookupMap.put(subscriptionId, eventTypes);
        }
        eventTypes.add(eventType);
    }

    @Override
    public void removeSubscriber(final SubscriptionId subscriptionId) {
        if (subscriptionLookupMap.containsKey(subscriptionId)) {
            final List<EventType> eventTypes = subscriptionLookupMap.get(subscriptionId);
            eventTypes.stream()
                    .map(eventType -> channelMap.get(eventType))
                    .map(channel -> getChannelSubscription(channel))
                    .forEach(subscription -> subscription.removeSubscriber(subscriptionId));
        }
    }

    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        return channelMap.values().stream()
                .map(channel -> getChannelSubscription(channel))
                .flatMap(subscription -> subscription.getAllSubscribers().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Map<EventType, List<Subscriber<?>>> getSubscribersPerType() {
        final Map<EventType, List<Subscriber<?>>> map = channelMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> getChannelSubscription(e.getValue()).getAllSubscribers()));
        return map;
    }

}
