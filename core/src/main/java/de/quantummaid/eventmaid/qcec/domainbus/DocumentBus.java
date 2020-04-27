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

package de.quantummaid.eventmaid.qcec.domainbus;

import de.quantummaid.eventmaid.qcec.domainbus.building.AnswerStep1Builder;
import de.quantummaid.eventmaid.qcec.queryresolving.Query;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;

import java.util.Optional;

/**
 * The {@code DocumentBus} class combines the functionality of the {@code QueryResolver}, the {@code ConstraintEnforcer},
 * and the {@code EventBus}. All of their methods have been extended with conditions and automatic unsubscribing.
 *
 * @see <a href="https://github.com/quantummaid/eventmaid#documentbus">EventMaid Documentation</a>
 */
public interface DocumentBus {

    /**
     * Entry point of the defining process, how and when to answer a {@code Query}.
     *
     * @param queryClass the class of the {@code Query} to answer
     * @param <T>        the type of the {@code Query}
     * @return a builder to define further aspects of the answer
     */
    <T extends Query<?>> AnswerStep1Builder<T> answer(Class<T> queryClass);

    /**
     * Entry point of the defining process, how and when to enforce a constraint.
     *
     * @param constraintClass the class of the constraint to enforce
     * @param <T>             the type of the constraint
     * @return a builder to define further aspects of the enforcement
     */
    <T> AnswerStep1Builder<T> ensure(Class<T> constraintClass);

    /**
     * Entry point of the defining process, how an when to react to an event.
     *
     * @param event the class of the event to react to
     * @param <T>   the type of the event
     * @return a builder to define further aspects of the reactin
     */
    <T> AnswerStep1Builder<T> reactTo(Class<T> event);

    /**
     * Executes the {@code Query}.
     *
     * @param query the {@code Query} to execute
     * @param <T>   the type of the {@code Query}
     * @return an {@code Optional} containing the result
     */
    <T> Optional<T> query(Query<T> query);

    /**
     * Executes the {@code Query} and expects a result.
     *
     * @param query the {@code Query} to execute
     * @param <T>   the type of the {@code Query}
     * @return the result of the query
     * @throws UnsupportedOperationException if no result could be obtained
     */
    <T> T queryRequired(Query<T> query);

    /**
     * Enforces to given constraint.
     *
     * @param constraint the constraint to enforce
     */
    void enforce(Object constraint);

    /**
     * Publishes the given event.
     *
     * @param event the event to publish
     */
    void publish(Object event);

    /**
     * Removes the subscription for the given {@code SubscriptionId} independent of whether it was for a {@code Query},
     * a constraint or an event.
     *
     * @param subscriptionId the {@code SubscriptionId} to remove
     */
    void unsubscribe(SubscriptionId subscriptionId);
}
