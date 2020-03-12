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

package de.quantummaid.eventmaid.useCases.building;

import de.quantummaid.eventmaid.mapping.Demapifier;

import java.util.Map;

/**
 * Based on the conditions defined in the previous {@link RequestDeserializationStep1Builder}, this step defines how to
 * map the matching object from a {@link Map} to the given type.
 *
 * @param <T> the type to deserialize the {@link Map} into
 */
public interface RequestDeserializationStep2Builder<T> {

    /**
     * Uses the given {@link Demapifier}, when the previous condition triggers.
     *
     * @param deMapifier the {@link Demapifier} to use
     * @return the next step in the fluent builder interface
     */
    RequestDeserializationStep1Builder using(Demapifier<T> deMapifier);
}
