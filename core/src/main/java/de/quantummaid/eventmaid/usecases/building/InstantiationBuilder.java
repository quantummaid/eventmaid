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

package de.quantummaid.eventmaid.usecases.building;

import de.quantummaid.eventmaid.usecases.usecaseadapter.UseCaseInvocationBuilder;
import de.quantummaid.eventmaid.usecases.usecaseadapter.usecaseinstantiating.UseCaseInstantiator;
import de.quantummaid.eventmaid.usecases.usecaseadapter.usecaseinstantiating.ZeroArgumentsConstructorUseCaseInstantiator;

import static de.quantummaid.eventmaid.usecases.usecaseadapter.usecaseinstantiating.ZeroArgumentsConstructorUseCaseInstantiator.zeroArgumentsConstructorUseCaseInstantiator;

/**
 * Defines how a instance for a use case should be instantiated, whenever a request was received.
 */
public interface InstantiationBuilder {

    /**
     * Configures the {@link UseCaseInvocationBuilder} to create a new use case instance by invoking the
     * {@link ZeroArgumentsConstructorUseCaseInstantiator}.
     *
     * @return the next step in the fluent builder interface
     */
    default RequestSerializationStep1Builder obtainingUseCaseInstancesUsingTheZeroArgumentConstructor() {
        return obtainingUseCaseInstancesUsing(zeroArgumentsConstructorUseCaseInstantiator());
    }

    /**
     * Configures the {@link UseCaseInvocationBuilder} to use the given {@code UseCaseInstantiator} for each request.
     *
     * @param useCaseInstantiator the {@code UseCaseInstantiator} to invoke
     * @return the next step in the fluent builder interface
     */
    RequestSerializationStep1Builder obtainingUseCaseInstancesUsing(UseCaseInstantiator useCaseInstantiator);

}
