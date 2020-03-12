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

import de.quantummaid.eventmaid.mapping.Mapifier;
import de.quantummaid.eventmaid.messageBus.MessageBus;

import java.util.Objects;
import java.util.function.Predicate;

import static de.quantummaid.eventmaid.mapping.SerializationFilters.areOfType;
import static java.lang.String.format;

public interface ResponseSerializationStep1Builder {

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize a use case return value
     * onto the {@link MessageBus} if the use case return value is of the specified type.
     *
     * @param type the class of use case response that will be serialized by the {@link Mapifier}
     * @param <T>  the type of the response
     * @return the next step in the fluent builder
     */
    @SuppressWarnings("unchecked")
    default <T> ResponseSerializationStep2Builder<T> serializingUseCaseResponseBackOntoTheBusOfType(final Class<T> type) {
        return mapper ->
                serializingUseCaseResponseBackOntoTheBusThat(areOfType(type))
                        .using((Mapifier<Object>) mapper);
    }

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize a use case return value of type
     * {@code null}.
     *
     * @param <T> the type of the follow up builder
     * @return the next step in the fluent builder
     */
    @SuppressWarnings("unchecked")
    default <T> ResponseSerializationStep2Builder<T> serializingResponseObjectsOfTypeVoid() {
        return mapper ->
                serializingUseCaseResponseBackOntoTheBusThat(Objects::isNull)
                        .using((Mapifier<Object>) mapper);
    }

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize a use case return value
     * onto the {@link MessageBus} if the use case return value matches the provided {@link Predicate filter}.
     *
     * @param filter a {@link Predicate} that returns true if the {@link Mapifier} should be used on the
     *               respective use case response
     * @return the next step in the fluent builder
     */
    ResponseSerializationStep2Builder<Object> serializingUseCaseResponseBackOntoTheBusThat(Predicate<Object> filter);

    /**
     * Configures  to throw an exception if no {@link Mapifier} configured matches the use case return value.
     *
     * @return the next step in the fluent builder
     */
    default ExceptionSerializationStep1Builder throwingAnExceptionByDefaultIfNoResponseSerializationCanBeApplied() {
        return serializingUseCaseResponseBackOntoTheBusByDefaultUsing(object -> {
            final String message = format("No serialization found for object %s.", object);
            throw MissingResponseSerializationException.missingResponseSerializationException(message);
        });
    }

    /**
     * Configures the default {@link Mapifier} that will be used to serialize a use case return value
     * onto the {@link MessageBus} if no {@link Mapifier} configured matches the use case return value.
     *
     * @param mapper a {@link Mapifier}
     * @return the next step in the fluent builder
     */
    ExceptionSerializationStep1Builder serializingUseCaseResponseBackOntoTheBusByDefaultUsing(Mapifier<Object> mapper);
}
