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

package de.quantummaid.eventmaid.mapping;

import de.quantummaid.eventmaid.internal.collections.filtermap.FilterMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static de.quantummaid.eventmaid.internal.enforcing.NotNullEnforcer.ensureNotNull;

/**
 * The {@code Deserializer} takes a target {@link Class} and a {@link Map} to create a new object of the given class based on the
 * data contained {@code Map}.
 *
 * @see <a href="https://github.com/quantummaid/eventmaid#channel">EventMaid Documentation</a>
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Deserializer {
    private final FilterMap<Class<?>, Object, Demapifier<?>> demapifierMap;

    /**
     * Creates a new {@code Deserializer} from the given {@link FilterMap}.
     *
     * @param requestMappers the {@code FilterMap} to be used as deserializers
     * @return a new {@code Deserializer}
     */
    public static Deserializer deserializer(
            final FilterMap<Class<?>, Object, Demapifier<?>> requestMappers) {
        ensureNotNull(requestMappers, "demapifierMap");
        return new Deserializer(requestMappers);
    }

    /**
     * Creates a new object of the given {@code Class} from the data contained in the {@code Map}.
     *
     * @param type the type of the object to be created
     * @param map  the {@code Map} containing necessary data
     * @param <T>  the type of the object to be created
     * @return the newly created object
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(final Class<T> type,
                             final Object map) {
        final Demapifier<T> demapifier = (Demapifier<T>) demapifierMap.get(type, map);
        return demapifier.map(type, map);
    }
}
