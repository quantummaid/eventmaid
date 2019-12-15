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

package de.quantummaid.messagemaid.qcec.querying.config;

import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.qcec.queryresolving.Query;
import de.quantummaid.messagemaid.qcec.queryresolving.QueryResolver;
import de.quantummaid.messagemaid.qcec.queryresolving.QueryResolverFactory;
import de.quantummaid.messagemaid.subscribing.SubscriptionId;

import java.util.Optional;
import java.util.function.Consumer;

import static de.quantummaid.messagemaid.messageBus.MessageBusBuilder.aMessageBus;
import static de.quantummaid.messagemaid.messageBus.MessageBusType.SYNCHRONOUS;

public final class QueryResolverTestQueryResolver extends TestQueryResolver {
    private final QueryResolver queryResolver;

    private QueryResolverTestQueryResolver() {
        final MessageBus messageBus = aMessageBus()
                .forType(SYNCHRONOUS)
                .build();
        queryResolver = QueryResolverFactory.aQueryResolver(messageBus);
    }

    public static QueryResolverTestQueryResolver queryResolverTestQueryResolver() {
        return new QueryResolverTestQueryResolver();
    }

    @Override
    public <R> Optional<R> executeQuery(final Query<R> query) {
        return queryResolver.query(query);
    }

    @Override
    public <R> R executeRequiredQuery(final Query<R> query) {
        return queryResolver.queryRequired(query);
    }

    @Override
    public <T extends Query<?>> SubscriptionId subscribing(final Class<T> queryClass, final Consumer<T> consumer) {
        final SubscriptionId subscriptionId = queryResolver.answer(queryClass, consumer);
        return subscriptionId;
    }

    @Override
    public <T extends Query<?>> TestQueryResolver withASubscriber(final Class<T> queryClass, final Consumer<T> consumer) {
        subscribing(queryClass, consumer);
        return this;
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        queryResolver.unsubscribe(subscriptionId);
    }
}
