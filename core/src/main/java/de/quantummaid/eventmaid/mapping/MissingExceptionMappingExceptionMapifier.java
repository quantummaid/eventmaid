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

package de.quantummaid.eventmaid.mapping;

import de.quantummaid.eventmaid.useCases.building.MissingExceptionSerializationException;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * A {@code Mapifier} exclusively for exceptions.
 *
 * @see <a href="https://github.com/quantummaid/eventmaid#channel">EventMaid Documentation</a>
 */

public class MissingExceptionMappingExceptionMapifier implements Mapifier<Exception> {
    /**
     * All {@link Exception} objects are stored under this key in the {@link Map}
     */
    public static final String DEFAULT_EXCEPTION_MAPIFIER_KEY = "Exception";

    /**
     * Factory method to create a new {@link ExceptionMapifier}
     *
     * @return the newly created {@link ExceptionMapifier}
     */
    public static MissingExceptionMappingExceptionMapifier missingExceptionSerializationWrappingExceptionMapifier() {
        return new MissingExceptionMappingExceptionMapifier();
    }

    @Override
    public Map<String, Object> map(final Exception cause) {
        final Map<String, Object> map = new HashMap<>();
        final Class<? extends Exception> causeClass = cause.getClass();
        final String message = format("No response mapper found for exception of class %s.", causeClass);
        final MissingExceptionSerializationException exception = MissingExceptionSerializationException.missingExceptionSerializationException(message);
        final String exceptionMessage = exception.getMessage();
        map.put(DEFAULT_EXCEPTION_MAPIFIER_KEY, exceptionMessage);
        return map;
    }
}
