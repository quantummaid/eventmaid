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

package de.quantummaid.eventmaid.messageBus.internal.correlationIds;

import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class CorrelationBasedSubscriptionsImpl implements CorrelationBasedSubscriptions {
    private final Map<CorrelationId, List<Subscriber<ProcessingContext<Object>>>> correlationBasedSubscriber =
            new ConcurrentHashMap<>();
    private final Map<SubscriptionId, List<CorrelationId>> reverseLookupMap = new ConcurrentHashMap<>();

    public static CorrelationBasedSubscriptionsImpl correlationBasedSubscriptions() {
        return new CorrelationBasedSubscriptionsImpl();
    }

    @Override
    public synchronized SubscriptionId addCorrelationBasedSubscriber(final CorrelationId correlationId,
                                                                     final Subscriber<ProcessingContext<Object>> subscriber) {
        final SubscriptionId subscriptionId = subscriber.getSubscriptionId();
        if (correlationBasedSubscriber.containsKey(correlationId)) {
            final List<Subscriber<ProcessingContext<Object>>> subscribers = correlationBasedSubscriber.get(correlationId);
            subscribers.add(subscriber);
        } else {
            final CopyOnWriteArrayList<Subscriber<ProcessingContext<Object>>> subscribers = new CopyOnWriteArrayList<>();
            subscribers.add(subscriber);
            correlationBasedSubscriber.put(correlationId, subscribers);
        }
        if (reverseLookupMap.containsKey(subscriptionId)) {
            final List<CorrelationId> correlationIds = reverseLookupMap.get(subscriptionId);
            correlationIds.add(correlationId);
        } else {
            final LinkedList<CorrelationId> correlationIds = new LinkedList<>();
            correlationIds.add(correlationId);
            reverseLookupMap.putIfAbsent(subscriptionId, correlationIds);
        }
        return subscriptionId;
    }

    @Override
    public synchronized void unsubscribe(final SubscriptionId subscriptionId) {
        if (reverseLookupMap.containsKey(subscriptionId)) {
            final List<CorrelationId> correlationIds = reverseLookupMap.get(subscriptionId);
            for (final CorrelationId correlationId : correlationIds) {
                final List<Subscriber<ProcessingContext<Object>>> subscribers = correlationBasedSubscriber.get(correlationId);
                subscribers.removeIf(s -> s.getSubscriptionId().equals(subscriptionId));
            }
            if (correlationIds.size() == 1) {
                reverseLookupMap.remove(subscriptionId);
            }
        }
    }

    @Override
    public List<Subscriber<ProcessingContext<Object>>> getSubscribersFor(final CorrelationId correlationId) {
        if (correlationId == null) {
            return emptyList();
        } else {
            return correlationBasedSubscriber.getOrDefault(correlationId, emptyList());
        }
    }
}
