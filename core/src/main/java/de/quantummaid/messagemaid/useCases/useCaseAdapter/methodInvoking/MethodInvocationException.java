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

package de.quantummaid.messagemaid.useCases.useCaseAdapter.methodInvoking;

import java.lang.reflect.Method;

/**
 * This message is thrown, when {@link UseCaseMethodInvoker} failed to invoke the method.
 */
public final class MethodInvocationException extends RuntimeException {

    private MethodInvocationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Factory method to create a new {@code MethodInvocationException}
     *
     * @param useCaseClass  the class of the use case
     * @param useCase       the use case instance
     * @param useCaseMethod the method to invoke
     * @param event         the current event
     * @param cause         the underlying exception
     * @return the newly created {@code MethodInvocationException}
     */
    public static MethodInvocationException methodInvocationException(final Class<?> useCaseClass,
                                                                      final Object useCase,
                                                                      final Method useCaseMethod,
                                                                      final Object event,
                                                                      final Exception cause) {
        final String message = String.format("Could not call method '%s' of class '%s' with arg '%s' on object '%s'",
                useCaseMethod, useCaseClass, event, useCase);
        return new MethodInvocationException(message, cause);
    }
}
