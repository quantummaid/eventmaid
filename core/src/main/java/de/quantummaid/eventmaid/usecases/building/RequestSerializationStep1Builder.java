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

import de.quantummaid.eventmaid.mapping.Mapifier;
import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.usecases.usecaseadapter.UseCaseAdapter;
import de.quantummaid.eventmaid.usecases.usecasebus.UseCaseBus;

import java.util.Objects;
import java.util.function.Predicate;

import static de.quantummaid.eventmaid.mapping.SerializationFilters.areOfType;
import static java.lang.String.format;

public interface RequestSerializationStep1Builder {

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize a use case request
     * to an object if the use case return value is of the specified type.
     *
     * @param type the type of use case requests that will be serialized by the {@link Mapifier}
     * @param <T>  the type of the use case request
     * @return the next step in the fluent builder
     */
    default <T> RequestSerializationStep2Builder<Object> serializingUseCaseRequestOntoTheBusOfType(final Class<T> type) {
        return mapper ->
                serializingUseCaseRequestOntoTheBusMatching(areOfType(type))
                        .using(mapper);
    }

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize a use case request
     * if the request is {@code null}.
     *
     * @return the next step in the fluent builder
     */
    default RequestSerializationStep2Builder<Object> serializingUseCaseRequestOntoTheBusOfTypeVoid() {
        return mapper ->
                serializingUseCaseRequestOntoTheBusMatching(Objects::isNull)
                        .using(mapper);
    }

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize a use case request
     * onto the {@link MessageBus} if the type matches the provided {@link Predicate filter}.
     *
     * @param filter a {@link Predicate} that returns true if the {@link Mapifier} should be used on the
     *               respective use case request
     * @return the next step in the fluent builder
     */
    RequestSerializationStep2Builder<Object> serializingUseCaseRequestOntoTheBusMatching(Predicate<Object> filter);

    /**
     * Configured the {@link UseCaseAdapter} or {@link UseCaseBus} to throw an exception if no other serialization for use case
     * requests can be applied.
     *
     * @return the next step in the fluent builder
     */
    default RequestDeserializationStep1Builder throwingAnExceptionByDefaultIfNoRequestSerializationCanBeApplied() {
        return serializingUseCaseRequestsByDefaultUsing(object -> {
            final String message = format("No serialization found for object %s.", object);
            throw MissingRequestSerializationException.missingRequestSerializationException(message);
        });
    }

    /**
     * Configures the default {@link Mapifier} that will be used to serialize a use case request if no other serialization can be
     * applied.
     *
     * @param mapper a {@link Mapifier}
     * @return the next step in the fluent builder
     */
    RequestDeserializationStep1Builder serializingUseCaseRequestsByDefaultUsing(Mapifier<Object> mapper);
}
