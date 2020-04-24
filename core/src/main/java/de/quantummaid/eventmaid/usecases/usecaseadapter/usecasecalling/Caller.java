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

import java.util.Map;

/**
 * A {@code Caller} is responsible to invoke the correct method on the use case instance. The {@link Deserializer} is used
 * to deserialize the event into the parameters of the method. The {@link Serializer} is used to serialized the use case's return
 * value back into a {@link Map}.
 *
 * @param <U> the type of the use case
 */
@FunctionalInterface
public interface Caller<U> {

    /**
     * Invokes the use case with the data from the event.
     *
     * @param useCase        the use case instance
     * @param event          the data for the use case
     * @param callingContext contains objects for serialization or parameter injection
     * @return the serialized return value
     * @throws Exception all exceptions declared by the use case method are rethrown
     */
    Object call(U useCase,
                Object event,
                CallingContext callingContext) throws Exception; //NOSONAR
}
