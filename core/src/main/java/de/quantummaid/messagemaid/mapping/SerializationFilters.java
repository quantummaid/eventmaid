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

import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

import static lombok.AccessLevel.PRIVATE;

/**
 * A class defining reusable filters for the serialization of objects.
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#channel">MessageMaid Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class SerializationFilters {

    /**
     * Returns a {@link Predicate} to check if an object is of the given {@link Class}.
     *
     * @param type the type to check for
     * @param <T>  the type to check for
     * @return a {@code Predicate} checking for the given type
     */
    public static <T> Predicate<T> areOfType(final Class<?> type) {
        return obj -> {
            if (obj == null || obj.equals(Void.class)) {
                return type == null || type.equals(Void.class);
            } else {
                return type.isInstance(obj);
            }
        };
    }

}
