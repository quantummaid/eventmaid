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

package de.quantummaid.eventmaid.usecases.building;

import de.quantummaid.eventmaid.serializedmessagebus.SerializedMessageBus;
import de.quantummaid.eventmaid.usecases.usecaseadapter.UseCaseInvocationBuilder;
import de.quantummaid.eventmaid.usecases.usecaseadapter.UseCaseInvokingResponseEventType;
import de.quantummaid.eventmaid.usecases.usecaseadapter.usecasecalling.Caller;

/**
 * This interface defines how a use case should be invoked. All responses are send back on the same {@link SerializedMessageBus}
 * with the {@link UseCaseInvokingResponseEventType#USE_CASE_RESPONSE_EVENT_TYPE}.
 *
 * @param <U> the type of the currently configured use case
 */
public interface CallingBuilder<U> {

    /**
     * This method invokes the only public method on the current use case instance. All parameters of the method are deserialized
     * as defined by the {@link UseCaseInvocationBuilder} and the optional return value is serialized using the serializing
     * definitions of the {@link UseCaseInvocationBuilder}.
     *
     * @return the next step in the fluent builder interface
     */
    Step1Builder callingTheSingleUseCaseMethod();

    /**
     * With this method the use case is invoked as defined in the given {@link Caller}.
     *
     * @param caller the caller to use
     * @return the next step in the fluent builder interface
     */
    Step1Builder callingBy(Caller<U> caller);
}
