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

package de.quantummaid.eventmaid.subscribing;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Predicate;

/**
 * A {@code Subscriber} that calls the given {@code predicate} for each message. The return value of the {@code predicate}
 * decides, if the delivery should be continued ({@code true}), or preempted ({@code false}).
 *
 * @param <T> the type of messages of the {@code Subscriber} can accept
 * @see <a href="https://github.com/quantummaid/eventmaid#subscriber">EventMaid Documentation</a>
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PreemptiveSubscriber<T> implements Subscriber<T> {
    private final Predicate<T> predicate;
    private final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();

    /**
     * Factory method for a new {@code PreemptiveSubscriber}.
     *
     * @param predicate the {@code predicate} to apply on each message
     * @param <T>       the type of messages the {@code Subscriber} accepts
     * @return a new {@code PreemptiveSubscriber}
     */
    public static <T> PreemptiveSubscriber<T> preemptiveSubscriber(final Predicate<T> predicate) {
        return new PreemptiveSubscriber<>(predicate);
    }

    @Override
    public AcceptingBehavior accept(final T message) {
        final boolean continueDelivery = predicate.test(message);
        return AcceptingBehavior.acceptingBehavior(continueDelivery);
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }
}
