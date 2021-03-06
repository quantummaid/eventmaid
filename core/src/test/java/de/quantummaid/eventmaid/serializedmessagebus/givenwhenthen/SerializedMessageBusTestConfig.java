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

package de.quantummaid.eventmaid.serializedmessagebus.givenwhenthen;

import de.quantummaid.eventmaid.configuration.AsynchronousConfiguration;
import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusTestExceptionHandler;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.eventmaid.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static de.quantummaid.eventmaid.messagebus.MessageBusBuilder.aMessageBus;
import static de.quantummaid.eventmaid.messagebus.MessageBusType.ASYNCHRONOUS;
import static de.quantummaid.eventmaid.messagebus.MessageBusType.SYNCHRONOUS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SerializedMessageBusTestConfig {
    private final MessageBus messageBus;

    public static SerializedMessageBusTestConfig synchronousMessageBusTestConfig() {
        final MessageBus messageBus = aMessageBus().forType(SYNCHRONOUS)
                .build();
        return new SerializedMessageBusTestConfig(messageBus);
    }

    public static SerializedMessageBusTestConfig asynchronousMessageBusTestConfig() {
        final int poolSize = 3;
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousConfiguration(poolSize);
        final MessageBus messageBus = aMessageBus().forType(ASYNCHRONOUS)
                .withAsynchronousConfiguration(asynchronousConfiguration)
                .withExceptionHandler(MessageBusTestExceptionHandler.allExceptionIgnoringExceptionHandler())
                .build();
        return new SerializedMessageBusTestConfig(messageBus);
    }

    public MessageBus getMessageBus() {
        return messageBus;
    }
}
