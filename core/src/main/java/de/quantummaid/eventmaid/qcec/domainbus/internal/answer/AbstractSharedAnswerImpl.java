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

package de.quantummaid.eventmaid.qcec.domainbus.internal.answer;

import de.quantummaid.eventmaid.qcec.constraintenforcing.ConstraintEnforcer;
import de.quantummaid.eventmaid.qcec.eventbus.EventBus;
import de.quantummaid.eventmaid.qcec.queryresolving.QueryResolver;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static de.quantummaid.eventmaid.subscribing.SubscriptionId.newUniqueId;
import static lombok.AccessLevel.PROTECTED;

@RequiredArgsConstructor(access = PROTECTED)
public abstract class AbstractSharedAnswerImpl<T> implements Answer {
    protected final SubscriptionIdStorage subscriptionIdStorage = SubscriptionIdStorage.subscriptionIdStorage();
    protected final Class<T> tClass;
    protected final Predicate<T> responseCondition;
    protected final Consumer<T> responseConsumer;
    protected final List<TerminationCondition<?>> terminationConditions;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public SubscriptionId register(final QueryResolver queryResolver,
                                   final ConstraintEnforcer constraintEnforcer,
                                   final EventBus eventBus) {
        executeAnswerSpecificSubscription(tClass, queryResolver, constraintEnforcer, eventBus);

        for (final TerminationCondition<?> terminationCondition : terminationConditions) {
            final Class<T> eventClass = (Class<T>) terminationCondition.getEventClass();
            final Predicate<T> conditionFunction = (Predicate<T>) terminationCondition.getConditionFunction();
            final SubscriptionId eventSubscriptionId = eventBus.reactTo(eventClass, o -> {
                if (conditionFunction.test(o)) {
                    this.unregister(queryResolver, constraintEnforcer, eventBus);
                }
            });
            subscriptionIdStorage.addEventBusSubscriptionId(eventSubscriptionId);
        }
        return newUniqueId();
    }

    protected abstract void executeAnswerSpecificSubscription(Class<T> tClass,
                                                              QueryResolver queryResolver,
                                                              ConstraintEnforcer constraintEnforcer,
                                                              EventBus eventBus);

    @Override
    public void unregister(final QueryResolver queryResolver,
                           final ConstraintEnforcer constraintEnforcer,
                           final EventBus eventBus) {
        final List<SubscriptionId> querySubscriptionIds = subscriptionIdStorage.getQuerySubscriptionIds();
        for (final SubscriptionId querySubscriptionId : querySubscriptionIds) {
            queryResolver.unsubscribe(querySubscriptionId);
        }
        final List<SubscriptionId> constraintSubscriptionIds = subscriptionIdStorage.getConstraintSubscriptionIds();
        for (final SubscriptionId constraintSubscriptionId : constraintSubscriptionIds) {
            constraintEnforcer.unsubscribe(constraintSubscriptionId);
        }
        final List<SubscriptionId> eventSubscriptionIds = subscriptionIdStorage.getEventSubscriptionIds();
        for (final SubscriptionId eventSubscriptionId : eventSubscriptionIds) {
            eventBus.unsubscribe(eventSubscriptionId);
        }
    }
}
