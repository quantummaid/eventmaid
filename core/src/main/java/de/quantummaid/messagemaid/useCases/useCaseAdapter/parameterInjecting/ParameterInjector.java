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

package de.quantummaid.messagemaid.useCases.useCaseAdapter.parameterInjecting;

/**
 * The {@code ParameterInjector} allows for adding additional parameter to the use case methods.
 */
public interface ParameterInjector {

    /**
     * This method is called to check, if the current method parameter should be injected and not deserialized from the request
     * map.
     *
     * @param parameterClass the {@link Class} of the current parameter
     * @return {@code true} if the current parameter should be injected
     */
    boolean hasValueFor(Class<?> parameterClass);

    /**
     * Method to retrieve the injected value for the parameter.
     *
     * @param parameterClass the {@link Class} of the current parameter
     * @param injectionInformation the {@link ParameterInjectionInformation} holding the current use case class, method and more
     * @param <T> the type of the parameter
     * @return the actual value to inject
     */
    <T> T getParameterFor(Class<T> parameterClass, ParameterInjectionInformation injectionInformation);
}
