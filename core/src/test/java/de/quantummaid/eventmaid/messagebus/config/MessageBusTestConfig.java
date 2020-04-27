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

package de.quantummaid.eventmaid.messagebus.config;

import de.quantummaid.eventmaid.configuration.AsynchronousConfiguration;
import de.quantummaid.eventmaid.messagebus.MessageBusType;
import lombok.*;

import static de.quantummaid.eventmaid.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static de.quantummaid.eventmaid.messagebus.MessageBusType.ASYNCHRONOUS;
import static de.quantummaid.eventmaid.messagebus.MessageBusType.SYNCHRONOUS;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageBusTestConfig {
    public static final int ASYNCHRONOUS_DELIVERY_POOL_SIZE = 3;
    @Getter
    private final MessageBusType type;
    @Getter
    private final AsynchronousConfiguration asynchronousConfiguration;

    static MessageBusTestConfig aSynchronousMessageBus() {
        return new MessageBusTestConfig(SYNCHRONOUS, null);
    }

    static MessageBusTestConfig anAsynchronousMessageBus() {
        final int poolSize = ASYNCHRONOUS_DELIVERY_POOL_SIZE;
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousConfiguration(poolSize);
        return new MessageBusTestConfig(ASYNCHRONOUS, asynchronousConfiguration);
    }

    public boolean isAsynchronous() {
        return type == ASYNCHRONOUS;
    }

}
