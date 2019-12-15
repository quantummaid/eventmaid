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

import de.quantummaid.messagemaid.processingContext.ProcessingContext;

/**
 * {@code ActionHandler} are responsible to handle the execution of the final {@code Action} of a {@code Channel}.
 *
 * <p>{@code Actions} serve only as representative container for the information necessary to execute them. Any logic regarding
 * their execution is handled by the {@code ActionHandlers}. When a message reaches the end of a {@code Channel}, the
 * {@code ActionHandlerSet} serves as a lookup object for an {@code ActionHandler} matching the {@code Channel's} final
 * {@code Action}.</p>
 *
 * @param <T> the type of the {@code Action} to handle
 * @param <R> the type of messages of the {@code Channel}
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#custom-actions">MessageMaid Documentation</a>
 */
public interface ActionHandler<T extends Action<R>, R> {

    /**
     * Handle the execution of the given {@code Action} and message.
     *
     * @param action the {@code Action} this handler was written for
     * @param processingContext the message
     */
    void handle(T action, ProcessingContext<R> processingContext);
}
