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

import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * The {@code ActionHandler} implementation for the {@code Consume} {@code Action}. This handler will always execute the
 * consumer given to the {@code Consume} {@code Action}.
 *
 * @param <T> the type of messages of the {@code Channel}
 * @see <a href="https://github.com/quantummaid/eventmaid#consume">EventMaid Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class ConsumerActionHandler<T> implements ActionHandler<Consume<T>, T> {

    /**
     * Factory method for a new {@code ConsumerActionHandler}.
     *
     * @param <T> the type of messages of the {@code Channel}
     * @return a new {@code ConsumerActionHandler}
     */
    public static <T> ConsumerActionHandler<T> consumerActionHandler() {
        return new ConsumerActionHandler<>();
    }

    /**
     * Will call the {@code Consume} {@code Action's} consumer.
     *
     * @param consume           the {@code Consume} {@code Action} to be handled
     * @param processingContext the message
     */
    @Override
    public void handle(final Consume<T> consume, final ProcessingContext<T> processingContext) {
        consume.accept(processingContext);
    }
}

