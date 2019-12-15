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

package de.quantummaid.messagemaid.qcec.eventing.givenWhenThen;

import de.quantummaid.messagemaid.qcec.domainBus.DocumentBus;
import de.quantummaid.messagemaid.qcec.domainBus.DocumentBusBuilder;
import de.quantummaid.messagemaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class DocumentBusTestEventBus extends TestEventBus {
    private final DocumentBus documentBus = DocumentBusBuilder.aDefaultDocumentBus();

    public static DocumentBusTestEventBus documentTestEventBus() {
        return new DocumentBusTestEventBus();
    }

    @Override
    public void publish(final Object event) {
        documentBus.publish(event);
    }

    @Override
    public <T> SubscriptionId reactTo(final Class<T> tClass, final Consumer<T> consumer) {
        return documentBus.reactTo(tClass)
                .using(consumer);
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        documentBus.unsubscribe(subscriptionId);
    }
}
