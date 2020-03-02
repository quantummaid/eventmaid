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

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.BiPredicate;

import static lombok.AccessLevel.PRIVATE;

/**
 * A class defining reusable filters for the deserialization of objects.
 *
 * @see <a href="https://github.com/quantummaid/eventmaid#channel">EventMaid Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class DeserializationFilters {

    /**
     * Creates a deserialization filter, that checks, if the the object to deserialize is of a given type.
     *
     * @param type the type, the to apply the deserialization on
     * @param <T>  the type, the to apply the deserialization on
     * @return a filter checking if the object of deserialize is of the given type
     */
    public static <T extends Class<?>> BiPredicate<T, Map<String, Object>> areOfType(final T type) {
        return (requestedType, map) -> {
            if (requestedType == null || requestedType.equals(Void.class)) {
                return type == null || type.equals(Void.class);
            } else {
                return type.isAssignableFrom(requestedType);
            }
        };
    }

}
