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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class ConstraintAnswerImpl<T> extends AbstractSharedAnswerImpl<T> {

    private ConstraintAnswerImpl(final Class<T> tClass,
                                 final Predicate<T> responseCondition,
                                 final Consumer<T> responseConsumer,
                                 final List<TerminationCondition<?>> terminationConditions) {
        super(tClass, responseCondition, responseConsumer, terminationConditions);
    }

    static <T> ConstraintAnswerImpl<T> constraintAnswer(final Class<T> tClass,
                                                        final Predicate<T> responseCondition,
                                                        final Consumer<T> responseConsumer,
                                                        final List<TerminationCondition<?>> terminationConditions) {
        return new ConstraintAnswerImpl<>(tClass, responseCondition, responseConsumer, terminationConditions);
    }

    @Override
    protected void executeAnswerSpecificSubscription(final Class<T> tClass,
                                                     final QueryResolver queryResolver,
                                                     final ConstraintEnforcer constraintEnforcer,
                                                     final EventBus eventBus) {
        final SubscriptionId constraintSubscriptionId = constraintEnforcer.respondTo(tClass, t -> {
            if (responseCondition.test(t)) {
                responseConsumer.accept(t);
            }
        });
        subscriptionIdStorage.addConstraintEnforcerSubscriptionId(constraintSubscriptionId);
    }
}
