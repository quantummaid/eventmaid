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

package de.quantummaid.messagemaid.internal.collections.predicatemap;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Predicate;

import static de.quantummaid.messagemaid.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PredicateMap<P, T> {
    private final Map<Predicate<P>, T> entries;
    private final T defaultValue;

    static <P, T> PredicateMap<P, T> predicateMap(final Map<Predicate<P>, T> entries,
                                                  final T defaultValue) {
        ensureNotNull(entries, "entries");
        ensureNotNull(defaultValue, "defaultValue");
        return new PredicateMap<>(entries, defaultValue);
    }

    public T get(final P condition) {
        return entries.entrySet().stream()
                .filter(e -> e.getKey().test(condition))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(defaultValue);
    }
}
