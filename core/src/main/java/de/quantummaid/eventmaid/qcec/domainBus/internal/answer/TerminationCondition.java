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

package de.quantummaid.eventmaid.qcec.domainBus.internal.answer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TerminationCondition<R> {

    @Getter
    private final Class<R> eventClass;

    @Getter
    private final Predicate<R> conditionFunction;

    static <R> TerminationCondition<R> terminationCondition(final Class<R> eventClass, final Predicate<R> conditionFunction) {
        return new TerminationCondition<>(eventClass, conditionFunction);
    }
}
