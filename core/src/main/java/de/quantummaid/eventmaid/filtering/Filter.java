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

package de.quantummaid.eventmaid.filtering;

import de.quantummaid.eventmaid.channel.Channel;
import de.quantummaid.eventmaid.messageBus.MessageBus;

/**
 * A {@link Channel} and a {@link MessageBus} can accept {@code Filter} to alter the flow of transported messages.
 *
 * @param <T> the type of messages of the {@code Channel}
 * @see <a href="https://github.com/quantummaid/eventmaid#adding-filter-to-channel">EventMaid Documentation</a>
 */
public interface Filter<T> {

    /**
     * The {@code handle} method is called for each message, that traversed the {@code Channel} up to this {@code Filter}.
     * For each message the {@code Filter} should call either {@code filterActions.pass} to continue the message's propagation
     * through the {@code Channel} or it should call {@code filterActions.block} to stop the delivery of the message.
     *
     * @param message       the current message
     * @param filterActions the {@code FilterActions}
     */
    void apply(T message, FilterActions<T> filterActions);
}
