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
import de.quantummaid.eventmaid.useCases.useCaseAdapter.parameterInjecting.ParameterInjector;

import java.util.Map;

/**
 * Invokes a specific method on the use case.
 */
public interface UseCaseMethodInvoker {

    /**
     * Takes the use case instance and the current event to invoke the method on the use case instance. The {@link Deserializer}
     * is used to deserialize the event into the parameters of the method. The method's return value is deserialized with the
     * {@link Serializer}.
     *
     * @param useCase             the use case instance
     * @param event               the request message
     * @param requestDeserializer the {@code Deserializer} for the parameters
     * @param responseSerializer  the {@code Serializer} for the return value
     * @param parameterInjector   the configured {@code ParameterInjector} with all injections
     * @return the serialized return value as {@link Map}
     * @throws MethodInvocationException when the method cannot be invoked
     * @throws ClassCastException        when the event is not a valid {@link Map} of type {@code <String, Object>}
     * @throws Exception                 all exceptions declared by the use case method are rethrown
     */
    Map<String, Object> invoke(Object useCase,
                               Object event,
                               Deserializer requestDeserializer,
                               Serializer responseSerializer,
                               ParameterInjector parameterInjector) throws Exception;

}
