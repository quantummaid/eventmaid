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

package de.quantummaid.eventmaid.usecases.usecaseadapter.usecaseinstantiating;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static de.quantummaid.eventmaid.usecases.usecaseadapter.usecaseinstantiating.ZeroArgumentsConstructorUseCaseInstantiatorException.zeroArgumentsConstructorUseCaseInstantiatorException;
import static lombok.AccessLevel.PRIVATE;

/**
 * This {@link UseCaseInstantiator} takes the current use case {@link Class} and locates its constructor with
 * {@link Class#getDeclaredConstructor(Class[])} with no arguments. It fails with
 * {@link ZeroArgumentsConstructorUseCaseInstantiatorException} if no suitable constructor was found.
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class ZeroArgumentsConstructorUseCaseInstantiator implements UseCaseInstantiator {

    /**
     * Factory method to create a new {@code ZeroArgumentsConstructorUseCaseInstantiator}.
     *
     * @return the newly created {@code ZeroArgumentsConstructorUseCaseInstantiator}
     */
    public static ZeroArgumentsConstructorUseCaseInstantiator zeroArgumentsConstructorUseCaseInstantiator() {
        return new ZeroArgumentsConstructorUseCaseInstantiator();
    }

    @Override
    public <T> T instantiate(final Class<T> type) {
        try {
            final Constructor<?> constructor = type.getDeclaredConstructor();
            @SuppressWarnings("unchecked")
            final T newInstance = (T) constructor.newInstance();
            return newInstance;
        } catch (final ExceptionInInitializerError e) {
            final Throwable cause = e.getException();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw zeroArgumentsConstructorUseCaseInstantiatorException(type, e);
            }
        } catch (final InvocationTargetException e) {
            throw zeroArgumentsConstructorUseCaseInstantiatorException(type, e.getTargetException());
        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw zeroArgumentsConstructorUseCaseInstantiatorException(type, e);
        }
    }
}
