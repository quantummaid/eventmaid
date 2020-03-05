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

package de.quantummaid.eventmaid.useCases.useCaseAdapter.methodInvoking;

import de.quantummaid.eventmaid.mapping.Deserializer;
import de.quantummaid.eventmaid.mapping.Serializer;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.parameterInjecting.ParameterInjectionInformation;
import de.quantummaid.eventmaid.useCases.useCaseAdapter.parameterInjecting.ParameterInjector;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static de.quantummaid.eventmaid.useCases.useCaseAdapter.methodInvoking.MethodInvocationException.methodInvocationException;
import static java.util.Arrays.stream;
import static lombok.AccessLevel.PRIVATE;

/**
 * A {@link UseCaseMethodInvoker}, that uses the {@link Deserializer} to deserialized the event into parameters of the use case
 * method. The {@link Serializer} is used to serialize the return value back into a {@link Map}.
 */

@RequiredArgsConstructor(access = PRIVATE)
public final class SerializingMethodInvoker implements UseCaseMethodInvoker {
    private final Method useCaseMethod;

    /**
     * Factory method to create a new {@code SerializingMethodInvoker}.
     *
     * @param method the {@code method} to invoke
     * @return the newly created {@code SerializingMethodInvoker} object
     */
    public static SerializingMethodInvoker serializingMethodInvoker(final Method method) {
        return new SerializingMethodInvoker(method);
    }

    @Override
    public Object invoke(final Object useCase,
                         final Object event,
                         final Deserializer requestDeserializer,
                         final Serializer responseSerializer,
                         final ParameterInjector parameterInjector) throws Exception {
        try {
            final Class<?>[] parameterTypes = useCaseMethod.getParameterTypes();

            @SuppressWarnings("unchecked") final Map<String, Object> map = (Map<String, Object>) event;
            final Object[] parameters = stream(parameterTypes)
                    .map(parameterType -> {
                        if (parameterInjector.hasValueFor(parameterType)) {
                            final ParameterInjectionInformation injectionInformation = getInjectionInformation(useCase, map);
                            return parameterInjector.getParameterFor(parameterType, injectionInformation);
                        } else {
                            return requestDeserializer.deserialize(parameterType, map);
                        }
                    })
                    .toArray();
            final Object returnValue = useCaseMethod.invoke(useCase, parameters);
            return responseSerializer.serialize(returnValue);
        } catch (final IllegalAccessException e) {
            final Class<?> useCaseClass = useCase.getClass();
            throw methodInvocationException(useCaseClass, useCase, useCaseMethod, event, e);
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (isDeclaredByMethod(cause, useCaseMethod)) {
                throw (Exception) cause;
            } else {
                final Class<?> useCaseClass = useCase.getClass();
                throw methodInvocationException(useCaseClass, useCase, useCaseMethod, event, e);
            }
        }
    }

    private ParameterInjectionInformation getInjectionInformation(final Object useCase, final Map<String, Object> map) {
        final Class<?> useCaseClass = useCase.getClass();
        final String methodName = useCaseMethod.getName();
        return ParameterInjectionInformation.injectionInformation(useCaseClass, methodName, map);
    }

    private boolean isDeclaredByMethod(final Throwable cause, final Method method) {
        final Class<?>[] exceptionTypes = method.getExceptionTypes();
        final Class<? extends Throwable> exceptionClass = cause.getClass();
        return Arrays.asList(exceptionTypes).contains(exceptionClass);
    }

}
