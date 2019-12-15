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

package de.quantummaid.messagemaid.qcec.queryresolving;

import de.quantummaid.messagemaid.subscribing.SubscriptionId;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * A {@code QueryResolver} allows sending {@code Query} on an synchronous {@code MessageBus}.
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#queries">MessageMaid Documentation</a>
 */
public interface QueryResolver {
    /**
     * Sends the {@code Query} to all interested {@code Subscribers}. This method does not need to return an result.
     *
     * @param query the {@code Query} to be sent
     * @param <R>   the type of the result
     * @return an {@code Optional} for the {@code Query} result
     */
    <R> Optional<R> query(Query<R> query);

    /**
     * Sends the {@code Query} to all interested {@code Subscribers}. This method expects a valid result.
     *
     * @param query the {@code Query} to be sent
     * @param <R>   the type of the result
     * @return the {@code Query} result
     * @throws UnsupportedOperationException if not result was received
     */
    <R> R queryRequired(Query<R> query);

    /**
     * Adds a new {@code Consumer} as {@code Subscriber} forthe given {@code Query} class.
     *
     * @param queryClass the class of the {@code Query}
     * @param responder  the {@code Consumer} to answer the query
     * @param <T>        the type of the query
     * @return a {@code SubscriptionId} of the {@code Subscriber}
     */
    <T extends Query<?>> SubscriptionId answer(Class<T> queryClass, Consumer<T> responder);

    /**
     * Method to remove all {@code Consumer} matching the {@code SubscriptionId}.
     *
     * @param subscriptionId the {@code SubscriptionId} to remove
     */
    void unsubscribe(SubscriptionId subscriptionId);
}
