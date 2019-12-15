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

package de.quantummaid.messagemaid.channel.config;

import de.quantummaid.messagemaid.channel.ChannelType;
import de.quantummaid.messagemaid.configuration.AsynchronousConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.messagemaid.channel.ChannelType.ASYNCHRONOUS;
import static de.quantummaid.messagemaid.channel.ChannelType.SYNCHRONOUS;
import static de.quantummaid.messagemaid.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelTestConfig {
    public static final int ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE = 5;
    @Getter
    private final ChannelType type;
    @Getter
    private final AsynchronousConfiguration asynchronousConfiguration;

    public static ChannelTestConfig synchronousChannelTestConfig() {
        return new ChannelTestConfig(SYNCHRONOUS, null);
    }

    public static ChannelTestConfig asynchronousChannelTestConfig() {
        final int poolSize = ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE;
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousConfiguration(poolSize);
        return new ChannelTestConfig(ASYNCHRONOUS, asynchronousConfiguration);
    }

    public boolean isAsynchronous() {
        return type == ASYNCHRONOUS;
    }
}
