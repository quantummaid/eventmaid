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

package de.quantummaid.messagemaid.useCases.useCaseAdapter.usecaseInstantiating;

import de.quantummaid.messagemaid.useCases.useCaseAdapter.UseCaseAdapter;

/**
 * Whenever a request for a use case is received by a {@link UseCaseAdapter}, the {@code UseCaseInstantiator} is askes to provide
 * an instance for the use case.
 */
public interface UseCaseInstantiator {

    /**
     * This method is called, whenever a use case is to be invoked. It takes the use case's class and returns an instance of the
     * use case.
     *
     * @param type the class of the use case
     * @param <T>  the type of the use case
     * @return the instance of the use case
     */
    <T> T instantiate(Class<T> type);
}
