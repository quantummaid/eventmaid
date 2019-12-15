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

package de.quantummaid.messagemaid.useCases.building;

import de.quantummaid.messagemaid.serializedMessageBus.SerializedMessageBus;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.UseCaseInvocationBuilder;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.UseCaseInvokingResponseEventType;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.usecaseCalling.Caller;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.util.Collections.emptyMap;

/**
 * This interface defines how a use case should be invoked. All responses are send back on the same {@link SerializedMessageBus}
 * with the {@link UseCaseInvokingResponseEventType#USE_CASE_RESPONSE_EVENT_TYPE}.
 *
 * @param <U> the type of the currently configured use case
 */
public interface CallingBuilder<U> {

    /**
     * The given {@link BiFunction} gets access to the current use case instance and the event. It should invoke the use case
     * and return the serialized return value.
     *
     * @param caller the {@link BiFunction} invoking the use case
     * @return the next step in the fluent builder interface
     */
    default InstantiationBuilder calling(final BiFunction<U, Object, Map<String, Object>> caller) {
        return callingBy((useCase, event, callingContext) -> {
            final Map<String, Object> responseMap = caller.apply(useCase, event);
            return responseMap;
        });
    }

    /**
     * This method allows for calling use cases without a return value. The {@code BiConsumer} gets access to the current use
     * case instance and event.
     *
     * @param caller the {@code BiConsumer} invoking the use case
     * @return the next step in the fluent builder interface
     */
    default InstantiationBuilder callingVoid(final BiConsumer<U, Object> caller) {
        return callingBy((usecase, event, callingContext) -> {
            caller.accept(usecase, event);
            return emptyMap();
        });
    }

    /**
     * This method invokes the only public method on the current use case instance. All parameters of the method are deserialized
     * as defined by the {@link UseCaseInvocationBuilder} and the optional return value is serialized using the serializing
     * definitions of the {@link UseCaseInvocationBuilder}.
     *
     * @return the next step in the fluent builder interface
     */
    InstantiationBuilder callingTheSingleUseCaseMethod();

    /**
     * With this method the use case is invoked as defined in the given {@link Caller}.
     *
     * @param caller the caller to use
     * @return the next step in the fluent builder interface
     */
    InstantiationBuilder callingBy(Caller<U> caller);
}
