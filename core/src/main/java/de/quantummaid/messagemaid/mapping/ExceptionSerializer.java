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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static de.quantummaid.messagemaid.internal.enforcing.NotNullEnforcer.ensureNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionSerializer {

    private final PredicateMap<Exception, Mapifier<Exception>> mapifiers;

    public static ExceptionSerializer exceptionSerializer(final PredicateMap<Exception, Mapifier<Exception>> mapifierMap) {
        ensureNotNull(mapifierMap, "mapifiers");
        return new ExceptionSerializer(mapifierMap);
    }

    public Map<String, Object> serializeException(final Exception returnValue) {
        final Mapifier<Exception> mapper = mapifiers.get(returnValue);
        return mapper.map(returnValue);
    }

}
