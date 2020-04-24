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

import de.quantummaid.eventmaid.processingcontext.EventType;

/**
 * This builder step defines, for which {@link EventType} the use case class configured in the previous
 * {@link InvokingUseCaseStepBuilder#invokingUseCase(Class)} method should be invoked.
 *
 * @param <U> the type of the currently configured use case
 */
public interface Step2Builder<U> {

    /**
     * Defines the {@link EventType} as {@code String} to invoke the use case for.
     *
     * @param eventType the {@code EventType} {@code String} to check for
     * @return the next step in the fluent builder interface
     */
    default Step3Builder<U> forType(final String eventType) {
        final EventType eventTypeObject = EventType.eventTypeFromString(eventType);
        return forType(eventTypeObject);
    }

    /**
     * Defines the {@link EventType} to invoke the use case for.
     *
     * @param eventType the {@code EventType} to check for
     * @return the next step in the fluent builder interface
     */
    Step3Builder<U> forType(EventType eventType);
}
