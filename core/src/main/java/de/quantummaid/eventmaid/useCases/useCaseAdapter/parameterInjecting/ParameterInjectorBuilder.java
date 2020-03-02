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

package de.quantummaid.eventmaid.useCases.useCaseAdapter.parameterInjecting;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ParameterInjectorBuilder {
    private final Map<Class<?>, Function<ParameterInjectionInformation, ?>> injectionMap = new HashMap<>();

    public static ParameterInjectorBuilder aParameterInjectorBuilder() {
        return new ParameterInjectorBuilder();
    }

    public <T> ParameterInjectorBuilder withAnInjection(final Class<T> parameterClass,
                                                        final Function<ParameterInjectionInformation, T> valueFunction) {
        injectionMap.put(parameterClass, valueFunction);
        return this;
    }

    public ParameterInjector build() {
        return new ParameterInjectorImpl(injectionMap);
    }
}
