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

/**
 * Configures the {@link UseCaseInvocationBuilder} with a new use case invocation definition of the given {@link Class}.
 */
public interface InvokingUseCaseStepBuilder {

    /**
     * Adds a new invocation information for a use case of the given {@code Class}.
     *
     * @param useCaseClass the {@code Class} of the use case
     * @param <U>          the type of the use case
     * @return the next step in the fluent builder interface
     */
    <U> Step2Builder<U> invokingUseCase(Class<U> useCaseClass);
}
