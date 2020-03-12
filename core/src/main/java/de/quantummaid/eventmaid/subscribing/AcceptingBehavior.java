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

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * When accepting a message, a {@code Subscriber} can decide if it the delivery to subsequent {@code Subscribers} should continue
 * or if the delivery is preempted.
 *
 * @see <a href="https://github.com/quantummaid/eventmaid#subscriber">EventMaid Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class AcceptingBehavior {
    /**
     * The message was accepted and the delivery continues.
     */
    public static final AcceptingBehavior MESSAGE_ACCEPTED = new AcceptingBehavior(true);

    /**
     * The message was accepted and the delivery can stop early.
     */
    public static final AcceptingBehavior MESSAGE_ACCEPTED_AND_STOP_DELIVERY = new AcceptingBehavior(false);

    private final boolean continueDelivery;

    /**
     * Factory method to create a new {@code AcceptingBehavior}.
     *
     * @param continueDelivery {@code true} continues the delivery, {@code false} preempts it
     * @return a new {@code AcceptingBehavior}
     */
    public static AcceptingBehavior acceptingBehavior(final boolean continueDelivery) {
        return new AcceptingBehavior(continueDelivery);
    }

    public boolean continueDelivery() {
        return continueDelivery;
    }
}
