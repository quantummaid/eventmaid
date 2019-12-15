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

import java.util.Map;

/**
 * A {@code Demapifier} is responsible to create a new object of the given {@link Class} from the date contained in the
 * {@link Map}.
 *
 * @param <T> the type of the object to be created
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#channel">MessageMaid Documentation</a>
 */
@FunctionalInterface
public interface Demapifier<T> {

    /**
     * Creates a new object of the given type using the data from the {@code Map}.
     *
     * @param targetType the type to be created
     * @param map        a map containing necessary data
     * @return the newly created object
     */
    T map(Class<T> targetType, Map<String, Object> map);
}
