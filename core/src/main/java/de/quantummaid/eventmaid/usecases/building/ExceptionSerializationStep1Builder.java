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

import de.quantummaid.eventmaid.mapping.ExceptionMapifier;
import de.quantummaid.eventmaid.mapping.Mapifier;

import java.util.Map;
import java.util.function.Predicate;

import static de.quantummaid.eventmaid.mapping.ExceptionMapifier.defaultExceptionMapifier;
import static de.quantummaid.eventmaid.mapping.MissingExceptionMappingExceptionMapifier.missingExceptionSerializationWrappingExceptionMapifier;
import static de.quantummaid.eventmaid.mapping.SerializationFilters.areOfType;

public interface ExceptionSerializationStep1Builder {

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize an exception thrown by a use
     * case to a {@link Map} if the exception matches the provided {@link Predicate filter}.
     *
     * @param filter a {@link Predicate} that returns true if the {@link Mapifier} should be used on the
     *               respective exception
     * @return the next step in the fluent builder
     */
    ExceptionSerializationStep2Builder<Exception> serializingExceptionsThat(Predicate<Exception> filter);

    /**
     * Enters a fluent builder that configures a {@link Mapifier} that will be used to serialize an exception to a {@link Map}
     * if the exception is of the specified type.
     *
     * @param type the class of exception that will be serialized by the {@link Mapifier}
     * @param <T>  the type of exception
     * @return the next step in the fluent builder
     */
    @SuppressWarnings("unchecked")
    default <T> ExceptionSerializationStep2Builder<T> serializingExceptionsOfType(final Class<T> type) {
        return mapper ->
                serializingExceptionsThat(areOfType(type))
                        .using((Mapifier<Exception>) mapper);
    }

    /**
     * Configures to throw an exception if no {@link Mapifier} configured under
     * {@link ExceptionSerializationStep1Builder#serializingExceptionsThat(Predicate)},
     * {@link ExceptionSerializationStep1Builder#serializingExceptionsOfType(Class)}, etc. matches exception.
     *
     * @return the next step in the fluent builder
     */
    default ResponseDeserializationStep1Builder respondingWithAWrappingMissingExceptionSerializationExceptionByDefault() {
        return serializingExceptionsByDefaultUsing(missingExceptionSerializationWrappingExceptionMapifier());
    }

    /**
     * Configures the default {@link Mapifier} to take all exceptions, that have not been matched by a previous rule and
     * serialize them into a {@link Map} by taking the {@link Exception} object and storing it under
     * {@value ExceptionMapifier#DEFAULT_EXCEPTION_MAPIFIER_KEY} key.
     *
     * @return the next step in the fluent builder interface
     */
    default ResponseDeserializationStep1Builder puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault() {
        return serializingExceptionsByDefaultUsing(defaultExceptionMapifier());
    }

    /**
     * Configures the default {@link Mapifier} that will be used to serialize an exception to a {@link Map} if no
     * {@link Mapifier} configured under {@link ExceptionSerializationStep1Builder#serializingExceptionsThat(Predicate)},
     * {@link ExceptionSerializationStep1Builder#serializingExceptionsOfType(Class)}, etc. matches the exception.
     *
     * @param mapper a {@link Mapifier}
     * @return the next step in the fluent builder
     */
    ResponseDeserializationStep1Builder serializingExceptionsByDefaultUsing(Mapifier<Exception> mapper);
}
