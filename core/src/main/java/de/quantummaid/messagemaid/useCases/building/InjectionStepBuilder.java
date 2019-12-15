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

package de.quantummaid.messagemaid.useCases.building;

import de.quantummaid.messagemaid.useCases.useCaseAdapter.UseCaseInvocationBuilder;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.parameterInjecting.ParameterInjectionInformation;

import java.util.function.Function;

/**
 * The {@link UseCaseInvocationBuilder} step for configuring injected parameters.
 */
public interface InjectionStepBuilder extends BuilderStepBuilder {

    /**
     * Adds a new parameter injection.
     *
     * <p>
     * Calling this method again for the same class overwrites previous calls.
     * </p>
     *
     * @param parameterClass the class of the paramter to inject
     * @param injector       a function to inject the value based on the {@link ParameterInjectionInformation}
     * @param <T>            the type of the class
     * @return               the next step in the fluent builder interface
     */
    <T> FinalStepBuilder injectParameterForClass(Class<T> parameterClass, Function<ParameterInjectionInformation, T> injector);
}
