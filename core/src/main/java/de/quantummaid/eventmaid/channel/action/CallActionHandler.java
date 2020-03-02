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
 * The {@code ActionHandler} implementation for the {@code Call} {@code Action}. It will always throw an
 * {@code CallNotAllowedAsFinalChannelAction}, when called.
 *
 * @param <T> the type of messages of the {@code Channel}
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class CallActionHandler<T> implements ActionHandler<Call<T>, T> {

    /**
     * Factory method to create an new {@code CallActionHandler}.
     *
     * @param <T> the type of messages of the {@code Channel}
     * @return a new {@code CallActionHandler}
     */
    public static <T> CallActionHandler<T> callActionHandler() {
        return new CallActionHandler<>();
    }

    /**
     * Will always throw {@code CallNotAllowedAsFinalChannelAction}.
     *
     * @param action            the {@code Call} {@code Action} this handler was written for
     * @param processingContext the message
     * @throws CallNotAllowedAsFinalChannelAction always
     */
    @Override
    public void handle(final Call<T> action, final ProcessingContext<T> processingContext) {
        throw new CallNotAllowedAsFinalChannelAction();
    }
}
