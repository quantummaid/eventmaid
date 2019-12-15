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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static de.quantummaid.messagemaid.internal.enforcing.NotNullEnforcer.ensureNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PredicateMapBuilder<P, T> {
    private final Map<Predicate<P>, T> entries;
    private T defaultValue;

    public static <P, T> PredicateMapBuilder<P, T> predicateMapBuilder() {
        final Map<Predicate<P>, T> entries = new HashMap<>();
        return new PredicateMapBuilder<>(entries);
    }

    public PredicateMapBuilder<P, T> put(final Predicate<P> filter, final T value) {
        entries.put(filter, value);
        return this;
    }

    public void setDefaultValue(final T defaultValue) {
        ensureNotNull(defaultValue, "defaultValue");
        this.defaultValue = defaultValue;
    }

    public PredicateMap<P, T> build() {
        return PredicateMap.predicateMap(entries, defaultValue);
    }
}
