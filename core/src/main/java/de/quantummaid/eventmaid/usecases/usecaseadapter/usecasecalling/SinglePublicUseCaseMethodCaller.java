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

package de.quantummaid.eventmaid.usecases.usecaseadapter.usecasecalling;

import de.quantummaid.eventmaid.mapping.Deserializer;
import de.quantummaid.eventmaid.mapping.Serializer;
import de.quantummaid.eventmaid.usecases.usecaseadapter.methodinvoking.UseCaseMethodInvoker;
import de.quantummaid.eventmaid.usecases.usecaseadapter.parameterinjecting.ParameterInjector;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.List;

import static de.quantummaid.eventmaid.internal.reflections.ForbiddenUseCaseMethods.NOT_ALLOWED_USECASE_PUBLIC_METHODS;
import static de.quantummaid.eventmaid.internal.reflections.ReflectionUtils.getAllPublicMethods;
import static de.quantummaid.eventmaid.usecases.usecaseadapter.methodinvoking.SerializingMethodInvoker.serializingMethodInvoker;
import static lombok.AccessLevel.PRIVATE;

/**
 * A {@link Caller} that locates the the only public method of the use case class and invokes it, whenever a request is received.
 *
 * @param <U> the use case type
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class SinglePublicUseCaseMethodCaller<U> implements Caller<U> {
    private final UseCaseMethodInvoker methodInvoker;

    /**
     * Factory method to create a new {@code SinglePublicUseCaseMethodCaller}.
     *
     * @param useCaseClass the class of the use case to call
     * @param <U>          the type of the use case
     * @return the newly created {@code SinglePublicUseCaseMethodCaller}
     * @throws IllegalArgumentException if not suitable method could be found
     */
    public static <U> SinglePublicUseCaseMethodCaller<U> singlePublicUseCaseMethodCaller(final Class<U> useCaseClass) {
        final Method method = locateUseCaseMethod(useCaseClass);
        final UseCaseMethodInvoker methodInvoker = serializingMethodInvoker(method);
        return new SinglePublicUseCaseMethodCaller<>(methodInvoker);
    }

    private static Method locateUseCaseMethod(final Class<?> useCaseClass) {
        final List<Method> useCaseMethods = getAllPublicMethods(useCaseClass, NOT_ALLOWED_USECASE_PUBLIC_METHODS);
        if (useCaseMethods.size() == 1) {
            return useCaseMethods.get(0);
        } else {
            final String message = String.format("Use case classes must have 1 instance method. Found the methods %s " +
                            "for class %s",
                    useCaseMethods, useCaseClass);
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public Object call(final U useCase, final Object event, final CallingContext callingContext) throws Exception {
        final Deserializer deserializer = callingContext.getDeserializer();
        final Serializer serializer = callingContext.getSerializer();
        final ParameterInjector injector = callingContext.getParameterInjector();
        return methodInvoker.invoke(useCase, event, deserializer, serializer, injector);
    }
}
