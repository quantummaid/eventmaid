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

package de.quantummaid.eventmaid.internal.collections.filtermap;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;

import static de.quantummaid.eventmaid.internal.enforcing.NotNullEnforcer.ensureNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilterMapBuilder<F, G, T> {
    private final List<FilterMapEntry<F, G, T>> entries;
    private T defaultValue;

    public static <F, G, T> FilterMapBuilder<F, G, T> filterMapBuilder() {
        return new FilterMapBuilder<>(new CopyOnWriteArrayList<>());
    }

    public FilterMapBuilder<F, G, T> put(final BiPredicate<F, G> filter, final T value) {
        final FilterMapEntry<F, G, T> entry = FilterMapEntry.filterMapEntry(filter, value);
        entries.add(entry);
        return this;
    }

    public void setDefaultValue(final T defaultValue) {
        ensureNotNull(defaultValue, "defaultValue");
        this.defaultValue = defaultValue;
    }

    public FilterMap<F, G, T> build() {
        return FilterMap.filterMap(entries, defaultValue);
    }
}
