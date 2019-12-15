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

package de.quantummaid.messagemaid.subscribing;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Consumer;

/**
 * A {@code Subscriber} that calls the given {@code consumer} for each message.
 *
 * @param <T> the type of messages of the {@code Subscriber} can accept
 * @see <a href="https://github.com/quantummaid/messagemaid#subscriber">MessageMaid Documentation</a>
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConsumerSubscriber<T> implements Subscriber<T> {
    private final Consumer<T> consumer;
    private final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();

    /**
     * Factory method to create a new {@code ConsumerSubscriber}.
     *
     * @param consumer the {@code consumer} to call for each message
     * @param <T>      the type of the message the {@code Subscriber} accepts
     * @return a new {@code ConsumerSubscriber}
     */
    public static <T> ConsumerSubscriber<T> consumerSubscriber(final Consumer<T> consumer) {
        return new ConsumerSubscriber<>(consumer);
    }

    @Override
    public AcceptingBehavior accept(final T message) {
        consumer.accept(message);
        return AcceptingBehavior.MESSAGE_ACCEPTED;
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }
}
