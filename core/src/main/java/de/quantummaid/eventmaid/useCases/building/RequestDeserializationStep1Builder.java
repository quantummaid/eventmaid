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

import de.quantummaid.eventmaid.mapping.Demapifier;

import java.util.Map;
import java.util.function.BiPredicate;

import static de.quantummaid.eventmaid.mapping.DeserializationFilters.areOfType;
import static de.quantummaid.eventmaid.useCases.building.MissingRequestDeserializationException.missingDeserializationException;
import static java.lang.String.format;

public interface RequestDeserializationStep1Builder {

    /**
     * Enters a fluent builder that configures a {@link Demapifier} that will be used to deserialize a {@link Map}
     * to a case parameter if the use case parameter is of the specified type.
     *
     * @param type the class of use case parameters that will be deserialized by the {@link Demapifier}
     * @param <T>  the type of the use case parameter
     * @return the next step in the fluent builder
     */
    default <T> RequestDeserializationStep2Builder<T> deserializingRequestsToUseCaseParametersOfType(final Class<T> type) {
        return deserializeRequestsToUseCaseParametersThat(areOfType(type));
    }

    /**
     * Enters a fluent builder that configures a {@link Demapifier} that will be used to deserialize a {@link Map}
     * to a use case parameter if the object matches the provided {@link BiPredicate filter}.
     *
     * @param filter a {@link BiPredicate} that returns true if the {@link Demapifier} should be used
     *               on the respective http request
     * @param <T>    the type of the use case response
     * @return the next step in the fluent builder
     */
    <T> RequestDeserializationStep2Builder<T> deserializeRequestsToUseCaseParametersThat(
            BiPredicate<Class<?>, Object> filter);

    /**
     * Configures to throw an exception if no {@link Demapifier} configured matches the request.
     *
     * @return the next step in the fluent builder
     */
    default ResponseSerializationStep1Builder throwAnExceptionByDefaultIfNoUseCaseRequestDeserializationCanBeApplied() {
        return deserializeRequestsToUseCaseParametersPerDefault((targetType, metaData) -> {
            throw missingDeserializationException(format("No request mapper found %s", targetType));
        });
    }

    /**
     * Configures the default {@link Demapifier} that will be used to deserialize a
     * to a use case parameter if no {@link Demapifier} configured can be applied.
     *
     * @param mapper a {@link Demapifier}
     * @return the next step in the fluent builder
     */
    ResponseSerializationStep1Builder deserializeRequestsToUseCaseParametersPerDefault(Demapifier<Object> mapper);

}
