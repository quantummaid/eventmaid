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

import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.UseCaseAdapter;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.UseCaseInvocationBuilder;
import de.quantummaid.messagemaid.useCases.useCaseBus.UseCaseBus;

/**
 * The {@link UseCaseInvocationBuilder} step, that builds the final {@link UseCaseBus} or {@link UseCaseAdapter}.
 */
public interface BuilderStepBuilder {

    /**
     * Completes the configuration and creates a {@code UseCaseBus} based on the given {@code SerializedMessageBus}.
     *
     * @param messageBus the {@code MessageBus} to use
     * @return the newly created {@code UseCaseBus}
     */
    UseCaseBus build(MessageBus messageBus);

    /**
     * Completes the configuration and creates a {@code UseCaseAdapter}.
     *
     * @return the newly created {@code UseCaseAdapter}
     */
    UseCaseAdapter buildAsStandaloneAdapter();

}
