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

package de.quantummaid.messagemaid.channel.action;

import de.quantummaid.messagemaid.processingContext.ProcessingContext;
import de.quantummaid.messagemaid.subscribing.AcceptingBehavior;
import de.quantummaid.messagemaid.subscribing.ConsumerSubscriber;
import de.quantummaid.messagemaid.subscribing.Subscriber;
import de.quantummaid.messagemaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static de.quantummaid.messagemaid.subscribing.ConsumerSubscriber.consumerSubscriber;
import static lombok.AccessLevel.PRIVATE;

/**
 * A {@code Subscription} object manages a list of {@code Subscribers}. Each message is distributed to each {@code Subscriber},
 * if no exception occurred.
 *
 * @param <T> the type of messages of the {@code Channel}
 * @see <a href="https://github.com/quantummaid/messagemaid#subscription">MessageMaid Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class Subscription<T> implements Action<T> {
    /* Use CopyOnWriteArrayList, because
     - concurrent collection
     - can call unsubscribe inside of subscriber and still maintaining order (DocumentBus.until relies on that property)
     */
    private final List<Subscriber<ProcessingContext<T>>> subscribers = new CopyOnWriteArrayList<>();

    /**
     * Creates a new {@code Subscription} object.
     *
     * @param <T> the type of messages of the {@code Channel}
     * @return a new {@code Subscription} object
     */
    public static <T> Subscription<T> subscription() {
        return new Subscription<>();
    }

    /**
     * Adds a the consumer wrapped in a {@code Subscriber} object.
     *
     * @param consumer the consuming {@code Subscriber} to be added
     * @return the wrapping {@code Subscriber's} {@code SubscriptionId}
     */
    public SubscriptionId addSubscriber(final Consumer<T> consumer) {
        final ConsumerSubscriber<T> subscriber = consumerSubscriber(consumer);
        return addSubscriber(subscriber);
    }

    /**
     * Adds a {@code Subscriber}.
     *
     * @param subscriber the {@code Subscriber} to be added
     * @return the wrapping {@code Subscriber's} {@code SubscriptionId}
     */
    public SubscriptionId addSubscriber(final Subscriber<T> subscriber) {
        return addRawSubscriber(new WrappingRawSubscriber(subscriber));
    }

    /**
     * Adds a the consumer wrapped in a {@code Subscriber} object, that gets access to the underlying {@code ProcessingContext}
     * object.
     *
     * @param consumer the consuming {@code Subscriber} to be added
     * @return the wrapping {@code Subscriber's} {@code SubscriptionId}
     */
    public SubscriptionId addRawSubscriber(final Consumer<ProcessingContext<T>> consumer) {
        final ConsumerSubscriber<ProcessingContext<T>> subscriber = consumerSubscriber(consumer);
        return addRawSubscriber(subscriber);
    }

    /**
     * Adds a {@code Subscriber}, that gets access to the underlying {@code ProcessingContext} object.
     *
     * @param subscriber the {@code Subscriber} to be added
     * @return the wrapping {@code Subscriber's} {@code SubscriptionId}
     */
    public SubscriptionId addRawSubscriber(final Subscriber<ProcessingContext<T>> subscriber) {
        subscribers.add(subscriber);
        return subscriber.getSubscriptionId();
    }

    /**
     * Returns if at least one subscriber exists.
     *
     * @return {@code true} if at least one {@code Subscriber} exists, {@code false} otherwise
     */
    public boolean hasSubscribers() {
        return !subscribers.isEmpty();
    }

    public List<Subscriber<?>> getAllSubscribers() {
        return subscribers.stream()
                .map(s -> {
                    if (s.getClass().equals(WrappingRawSubscriber.class)) {
                        return ((WrappingRawSubscriber) s).subscriber;
                    } else {
                        return s;
                    }
                }).collect(Collectors.toList());
    }

    List<Subscriber<ProcessingContext<T>>> getRealSubscribers() {
        return subscribers;
    }

    /**
     * Removes the given {@code Subscriber}.
     *
     * @param subscriber the {@code Subscriber} to be removed
     */
    public void removeSubscriber(final Subscriber<?> subscriber) {
        final SubscriptionId subscriptionId = subscriber.getSubscriptionId();
        removeSubscriber(subscriptionId);
    }

    /**
     * Removes all {@code Subscribers} that match the given {@code SubscriptionId}.
     *
     * @param subscriptionId the {@code SubscriptionId}, for which all {@code Subscribers} should be removed.
     */
    public void removeSubscriber(final SubscriptionId subscriptionId) {
        subscribers.removeIf(subscriber -> subscriber.getSubscriptionId().equals(subscriptionId));
    }

    private final class WrappingRawSubscriber implements Subscriber<ProcessingContext<T>> {
        private final Subscriber<T> subscriber;

        private WrappingRawSubscriber(final Subscriber<T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public AcceptingBehavior accept(final ProcessingContext<T> message) {
            final T payload = message.getPayload();
            return subscriber.accept(payload);
        }

        @Override
        public SubscriptionId getSubscriptionId() {
            return subscriber.getSubscriptionId();
        }
    }
}
