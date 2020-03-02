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

import java.util.Map;

/**
 * A {@code Mapifier} is takes an object and returns a {@link Map} representation from it.
 *
 * @param <T> the type of the object to be mapified
 * @see <a href="https://github.com/quantummaid/eventmaid#channel">EventMaid Documentation</a>
 */
@FunctionalInterface
public interface Mapifier<T> {

    /**
     * Takes an object and returns a {@code Map} containing the object's data
     *
     * @param object the object to create a {@code Map} from
     * @return a {@code Map} containing a representation of the object
     */
    Map<String, Object> map(T object);
}
