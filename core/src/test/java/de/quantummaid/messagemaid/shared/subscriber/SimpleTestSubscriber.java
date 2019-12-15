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

package de.quantummaid.messagemaid.shared.subscriber;

import de.quantummaid.messagemaid.subscribing.AcceptingBehavior;
import de.quantummaid.messagemaid.subscribing.SubscriptionId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static de.quantummaid.messagemaid.subscribing.AcceptingBehavior.acceptingBehavior;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleTestSubscriber<T> implements TestSubscriber<T> {
    private final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
    private final List<T> receivedMessages = new CopyOnWriteArrayList<>();
    private final boolean preemptDelivery;

    public static <T> SimpleTestSubscriber<T> testSubscriber() {
        return new SimpleTestSubscriber<>(false);
    }

    public static <T> SimpleTestSubscriber<T> deliveryPreemptingSubscriber() {
        return new SimpleTestSubscriber<>(true);
    }

    @Override
    public AcceptingBehavior accept(final T message) {
        receivedMessages.add(message);
        final boolean continueDelivery = !preemptDelivery;
        return acceptingBehavior(continueDelivery);
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }

    public List<T> getReceivedMessages() {
        return receivedMessages;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SimpleTestSubscriber<?> that = (SimpleTestSubscriber<?>) o;
        return Objects.equals(subscriptionId, that.subscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId);
    }

    @Override
    public String toString() {
        return "SimpleTestSubscriber{SubscriptionId=" + subscriptionId + "}";
    }
}
