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

import java.util.HashMap;
import java.util.Map;

/**
 * A {@code Mapifier} exclusively for exceptions.
 *
 * @see <a href="https://github.com/quantummaid/eventmaid#channel">EventMaid Documentation</a>
 */

public class ExceptionMapifier implements Mapifier<Exception> {
    /**
     * All {@link Exception} objects are stored under this key in the {@code Map}
     */
    public static final String DEFAULT_EXCEPTION_MAPIFIER_KEY = "Exception";

    /**
     * Factory method to create a new {@code ExceptionMapifier}
     *
     * @return the newly created {@code ExceptionMapifier}
     */
    public static ExceptionMapifier defaultExceptionMapifier() {
        return new ExceptionMapifier();
    }

    @Override
    public Map<String, Object> map(final Exception exception) {
        final Map<String, Object> map = new HashMap<>();
        map.put(DEFAULT_EXCEPTION_MAPIFIER_KEY, exception);
        return map;
    }
}
