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

import de.quantummaid.eventmaid.qcec.constraintenforcing.ConstraintEnforcer;
import de.quantummaid.eventmaid.qcec.domainbus.building.AnswerStep1Builder;
import de.quantummaid.eventmaid.qcec.domainbus.internal.answer.Answer;
import de.quantummaid.eventmaid.qcec.domainbus.internal.answer.AnswerBuilder;
import de.quantummaid.eventmaid.qcec.domainbus.internal.answer.AnswerRegister;
import de.quantummaid.eventmaid.qcec.eventbus.EventBus;
import de.quantummaid.eventmaid.qcec.queryresolving.Query;
import de.quantummaid.eventmaid.qcec.queryresolving.QueryResolver;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class DocumentBusImpl implements DocumentBus, AnswerRegister {
    private final Map<SubscriptionId, Answer> subscriptionIdAnswerMap = new HashMap<>();
    private final QueryResolver queryResolver;
    private final ConstraintEnforcer constraintEnforcer;
    private final EventBus eventBus;

    @Override
    public <T extends Query<?>> AnswerStep1Builder<T> answer(final Class<T> queryClass) {
        return AnswerBuilder.anQueryAnswerForClass(queryClass, this);
    }

    @Override
    public <T> AnswerStep1Builder<T> ensure(final Class<T> constraintClass) {
        return AnswerBuilder.anConstraintAnswerForClass(constraintClass, this);
    }

    @Override
    public <T> AnswerStep1Builder<T> reactTo(final Class<T> eventClass) {
        return AnswerBuilder.anEventAnswerForClass(eventClass, this);
    }

    @Override
    public <T> Optional<T> query(final Query<T> query) {
        return queryResolver.query(query);
    }

    @Override
    public <T> T queryRequired(final Query<T> query) {
        return queryResolver.queryRequired(query);
    }

    @Override
    public void enforce(final Object constraint) {
        constraintEnforcer.enforce(constraint);
    }

    @Override
    public void publish(final Object event) {
        eventBus.publish(event);
    }

    @Override
    public SubscriptionId submit(final Answer answer) {
        final SubscriptionId subscriptionId = answer.register(queryResolver, constraintEnforcer, eventBus);
        subscriptionIdAnswerMap.put(subscriptionId, answer);
        return subscriptionId;
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        if (subscriptionIdAnswerMap.containsKey(subscriptionId)) {
            final Answer answer = subscriptionIdAnswerMap.get(subscriptionId);
            answer.unregister(queryResolver, constraintEnforcer, eventBus);
        }
    }
}
