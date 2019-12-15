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

package de.quantummaid.messagemaid.messageBus.channelCreating;

import de.quantummaid.messagemaid.channel.Channel;
import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.messageBus.exception.MessageBusExceptionHandler;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.subscribing.Subscriber;

/**
 * Whenever a new class specific {@link Channel} is required by the {@link MessageBus}, the {@code MessageBusChannelFactory} is
 * called to create a new {@code Channel}.
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#configuring-the-messagebus">MessageMaid Documentation</a>
 */
public interface MessageBusChannelFactory {

    /**
     * This method is being called, when a new {@code Channel} is requested. Can happen in two cases. First a subscriber is
     * added for a not yet known {@link EventType}. Second, a message with an unknown {@code EventType} was sent. Then a new
     * {@code Channel} is created, that will delivery further messages of the {@code EventType}.
     *
     * @param eventType                  the {@code EventType} for which the {@code Channel} should be created.
     * @param subscriber                 if the request is done for a new {@link Subscriber}, it is given here. {@code null}
     *                                   otherwise
     * @param messageBusExceptionHandler the {@link MessageBusExceptionHandler} configured on the {@code MessageBus}
     * @return the newly created {@code Channel}
     */
    Channel<Object> createChannel(EventType eventType, Subscriber<?> subscriber,
                                  MessageBusExceptionHandler messageBusExceptionHandler);
}
