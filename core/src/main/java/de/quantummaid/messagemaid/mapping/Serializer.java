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

package de.quantummaid.messagemaid.mapping;

import de.quantummaid.messagemaid.internal.collections.predicatemap.PredicateMap;
import de.quantummaid.messagemaid.internal.enforcing.NotNullEnforcer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Serializer {

    private final PredicateMap<Object, Mapifier<Object>> mapifiers;

    public static Serializer serializer(final PredicateMap<Object, Mapifier<Object>> mapifierPredicateMap) {
        NotNullEnforcer.ensureNotNull(mapifierPredicateMap, "mapifiers");
        return new Serializer(mapifierPredicateMap);
    }

    public Map<String, Object> serialize(final Object value) {
        final Mapifier<Object> mapifier = mapifiers.get(value);
        return mapifier.map(value);
    }
}
