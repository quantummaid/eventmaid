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

package de.quantummaid.eventmaid.qcec.queryresolving;

import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.subscribing.PreemptiveSubscriber;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;

import java.util.Optional;
import java.util.function.Consumer;

import static de.quantummaid.eventmaid.processingcontext.EventType.eventTypeFromClass;
import static de.quantummaid.eventmaid.processingcontext.EventType.eventTypeFromObjectClass;

public class QueryResolverImpl implements QueryResolver {
    private final MessageBus messageBus;

    QueryResolverImpl(final MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    @Override
    public <T extends Query<?>> SubscriptionId answer(final Class<T> queryClass, final Consumer<T> responder) {
        final PreemptiveSubscriber<Object> subscriber = PreemptiveSubscriber.preemptiveSubscriber(t -> {
            @SuppressWarnings("unchecked")
            final T query = (T) t;
            responder.accept(query);
            return !query.finished();
        });
        final EventType eventType = eventTypeFromClass(queryClass);
        messageBus.subscribe(eventType, subscriber);
        return subscriber.getSubscriptionId();
    }

    @Override
    public <R> Optional<R> query(final Query<R> query) {
        final EventType eventType = eventTypeFromObjectClass(query);
        messageBus.send(eventType, query);
        return Optional.ofNullable(query.result());
    }

    @Override
    public <R> R queryRequired(final Query<R> query) {
        final EventType eventType = eventTypeFromObjectClass(query);
        messageBus.send(eventType, query);
        return Optional
                .ofNullable(query.result())
                .orElseThrow(() -> new UnsupportedOperationException("Expected a query result for query " + query));
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        messageBus.unsubcribe(subscriptionId);
    }

}
