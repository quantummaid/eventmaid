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

package de.quantummaid.eventmaid.channel.action;

import de.quantummaid.eventmaid.processingcontext.ProcessingContext;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Consumer;

import static lombok.AccessLevel.PRIVATE;

/**
 * The {@code Consume} {@code Action} calls the given consumer for every message that reached the end of the {@code Channel}.
 *
 * @param <T> the type of messages of the {@code Channel}
 *
 * @see <a href="https://github.com/quantummaid/eventmaid#consume">EventMaid Documentation</a>
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class Consume<T> implements Action<T> {
    private final Consumer<ProcessingContext<T>> consumer;

    /**
     * Factory method for creating a new {@code Consume} {@code Action} for a consumer accepting {@code ProcessingContext}.
     *
     * @param consumer consumer to be called for each message
     * @param <T>      the type of the {@code Channels} payload
     * @return a new {@code Consume} {@code Action}
     */
    public static <T> Consume<T> consumeMessage(final Consumer<ProcessingContext<T>> consumer) {
        return new Consume<>(consumer);
    }

    /**
     * Factory method for creating a new {@code Consume} {@code Action} for a consumer accepting {@code ProcessingContext}.
     *
     * @param consumer consumer to be called for each message
     * @param <T>      the type of the {@code Channels} payload
     * @return a new {@code Consume} {@code Action}
     */
    public static <T> Consume<T> consumePayload(final Consumer<T> consumer) {
        return new Consume<>(processingContext -> {
            final T payload = processingContext.getPayload();
            consumer.accept(payload);
        });
    }

    /**
     * Executes the consumer with the given message
     *
     * @param processingContext the message
     */
    public void accept(final ProcessingContext<T> processingContext) {
        consumer.accept(processingContext);
    }
}
