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

package de.quantummaid.messagemaid.channel.action;

import de.quantummaid.messagemaid.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * This {@code Action} takes the message and sends it in the given {@code Channel}.
 *
 * @param <T> the type of messages of both {@code Channels}
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#jump">MessageMaid Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class Jump<T> implements Action<T> {
    @Getter
    private final Channel<T> targetChannel;

    /**
     * Factory method to create a new {@code Jump} object, that forwards messages into the given {@code Channel}.
     *
     * @param targetChannel the {@code Channel} to forward messages to
     * @param <T>           the tyoe if messages of both {@code Channels}
     * @return a new {@code Jump} {@code Action}
     */
    public static <T> Jump<T> jumpTo(final Channel<T> targetChannel) {
        return new Jump<>(targetChannel);
    }
}
