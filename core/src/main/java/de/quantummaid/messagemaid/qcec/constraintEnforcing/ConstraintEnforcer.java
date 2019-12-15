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

package de.quantummaid.messagemaid.qcec.constraintEnforcing;

import de.quantummaid.messagemaid.subscribing.SubscriptionId;

import java.util.function.Consumer;

/**
 * The {@code ConstraintEnforcer} class is used to distribute constraints to all subscribers.
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#constraints">MessageMaid Documentation</a>
 */
public interface ConstraintEnforcer {

    /**
     * Sends the given constraint to all interested subscribers.
     *
     * @param constraint the constraint to be enforced
     */
    void enforce(Object constraint);

    /**
     * Adds the given {@code Consumer} as {@code Subscriber} for the given class.
     *
     * @param aClass    the class of constraint
     * @param responder the {@code Consumer} to be called for a matching constraint
     * @param <T>       the type of the constraint
     * @return a {@code SubscriptionId} identifying the {@code Consumer}
     */
    <T> SubscriptionId respondTo(Class<T> aClass, Consumer<T> responder);

    /**
     * Removes all {@code Subscribers} matching the given {@code SubscriptionId}.
     *
     * @param subscriptionId the {@code SubscriptionId} to remove
     */
    void unsubscribe(SubscriptionId subscriptionId);
}
