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

package de.quantummaid.eventmaid.qcec.querying.config;

import de.quantummaid.eventmaid.qcec.domainBus.DocumentBus;
import de.quantummaid.eventmaid.qcec.domainBus.DocumentBusBuilder;
import de.quantummaid.eventmaid.qcec.queryresolving.Query;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;

import java.util.Optional;
import java.util.function.Consumer;

public final class DocumentBusTestQueryResolver extends TestQueryResolver {
    private final DocumentBus documentBus;

    private DocumentBusTestQueryResolver() {
        documentBus = DocumentBusBuilder.aDefaultDocumentBus();
    }

    public static DocumentBusTestQueryResolver documentBusTestQueryResolver() {
        return new DocumentBusTestQueryResolver();
    }

    @Override
    public <R> Optional<R> executeQuery(final Query<R> query) {
        return documentBus.query(query);
    }

    @Override
    public <R> R executeRequiredQuery(final Query<R> query) {
        return documentBus.queryRequired(query);
    }

    @Override
    public <T extends Query<?>> SubscriptionId subscribing(final Class<T> queryClass, final Consumer<T> consumer) {
        return documentBus.answer(queryClass)
                .using(consumer);
    }

    @Override
    public <T extends Query<?>> TestQueryResolver withASubscriber(final Class<T> queryClass, final Consumer<T> consumer) {
        subscribing(queryClass, consumer);
        return this;
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        documentBus.unsubscribe(subscriptionId);
    }
}
