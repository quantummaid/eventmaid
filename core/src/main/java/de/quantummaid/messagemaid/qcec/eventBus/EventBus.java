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

package de.quantummaid.messagemaid.qcec.eventBus;

import de.quantummaid.messagemaid.subscribing.SubscriptionId;

import java.util.function.Consumer;

/**
 * The {@code EventBus} class is used to distribute events to all subscribers.
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#events">MessageMaid Documentation</a>
 */
public interface EventBus {

    /**
     * Send the event to all interested subscribers.
     *
     * @param event the event to publish
     */
    void publish(Object event);

    /**
     * Adds the {@code Consumer} as {@code Subscriber} for the given event class.
     *
     * @param tClass   the class of the event
     * @param consumer the {@code Consumer} to be called
     * @param <T>      the type of the event
     * @return the {@code Subscriber's} {@code SubscriptionId}
     */
    <T> SubscriptionId reactTo(Class<T> tClass, Consumer<T> consumer);

    /**
     * Removes all {@code Subscribers} matching the given {@code SubscriptionId}.
     *
     * @param subscriptionId the {@code SubscriptionId} to be removed
     */
    void unsubscribe(SubscriptionId subscriptionId);
}
