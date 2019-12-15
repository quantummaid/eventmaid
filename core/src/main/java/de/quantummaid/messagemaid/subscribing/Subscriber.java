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

/**
 * All delivering of messages to dynamically added and removed objects is done on the basis of {@code Subscribers}. A
 * {@code Subscriber} can accept messages and is uniquely identified by a {@code SubscriptionId}.
 *
 * @param <T> the type of messages of the {@code Subscriber} can accept
 * @see <a href="https://github.com/quantummaid/messagemaid#subscriber">MessageMaid Documentation</a>
 */
public interface Subscriber<T> {

    /**
     * Delivers the message to the {@code Subscriber}. The {@code Subscriber} can decide, if the delivery continues or
     * is preempted by returning the respective {@code AcceptingBehavior}.
     *
     * @param message the message
     * @return {@code AcceptingBehavior} to continue or preempt delivery
     */
    AcceptingBehavior accept(T message);

    /**
     * The unique and constant {@code SubscriptionId} of the {@code Subscriber}.
     *
     * @return the {@code Subscriber's} {@code SubscriptionId}
     */
    SubscriptionId getSubscriptionId();
}
