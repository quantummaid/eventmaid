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

package de.quantummaid.messagemaid.useCases.useCaseAdapter;

import de.quantummaid.messagemaid.mapping.Deserializer;
import de.quantummaid.messagemaid.mapping.Serializer;
import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.serializedMessageBus.SerializedMessageBus;

/**
 * A {@code UseCaseAdapter} has all necessary information, how to invoke the configured use cases. It was configured by
 * {@link UseCaseInvocationBuilder}.
 */
public interface UseCaseAdapter {

    /**
     * Takes the use case invocation information from the {@link UseCaseInvocationBuilder} to subscribe the use cases onto the
     * given {@code SerializedMessageBus}.
     *
     * @param serializedMessageBus the {@code SerializedMessageBus} to subscribe to
     */
    void attachTo(SerializedMessageBus serializedMessageBus);

    /**
     * First the given {@code MessageBus} is enhanced with the {@link Serializer} and{@link Deserializer} from the
     * {@link UseCaseInvocationBuilder}. Then necessary subscriber for the use cases are added to the bus.
     *
     * @param messageBus the {@code MessageBus} to use and enhance
     * @return the configured {@code SerializedMessageBus}
     */
    SerializedMessageBus attachAndEnhance(MessageBus messageBus);

}
