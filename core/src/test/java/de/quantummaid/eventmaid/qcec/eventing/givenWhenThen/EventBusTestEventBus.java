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

package de.quantummaid.eventmaid.qcec.eventing.givenWhenThen;

import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.qcec.eventbus.EventBus;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;

import java.util.function.Consumer;

import static de.quantummaid.eventmaid.messagebus.MessageBusBuilder.aMessageBus;
import static de.quantummaid.eventmaid.qcec.eventbus.EventBusFactory.aEventBus;

public final class EventBusTestEventBus extends TestEventBus {
    private final EventBus eventBus;

    private EventBusTestEventBus() {
        final MessageBus messageBus = aMessageBus()
                .build();
        this.eventBus = aEventBus(messageBus);
    }

    public static EventBusTestEventBus eventBusTestEventBus() {
        return new EventBusTestEventBus();
    }

    @Override
    public void publish(final Object event) {
        eventBus.publish(event);
    }

    @Override
    public <T> SubscriptionId reactTo(final Class<T> tClass, final Consumer<T> consumer) {
        return eventBus.reactTo(tClass, consumer);
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        eventBus.unsubscribe(subscriptionId);
    }
}
