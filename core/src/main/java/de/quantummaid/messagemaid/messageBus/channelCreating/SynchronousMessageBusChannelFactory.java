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
import de.quantummaid.messagemaid.channel.ChannelBuilder;
import de.quantummaid.messagemaid.channel.action.Subscription;
import de.quantummaid.messagemaid.messageBus.exception.MessageBusExceptionHandler;
import de.quantummaid.messagemaid.messageBus.internal.exception.DelegatingChannelExceptionHandler;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.messagemaid.messageBus.internal.exception.DelegatingChannelExceptionHandler.delegatingChannelExceptionHandlerForDeliveryChannel;
import static lombok.AccessLevel.PRIVATE;

/**
 * The default {@link MessageBusChannelFactory}, that creates synchronous {@link Channel Channels}.
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#configuring-the-messagebus">MessageMaid Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class SynchronousMessageBusChannelFactory implements MessageBusChannelFactory {

    /**
     * Factory method to create a new {@code SynchronousMessageBusChannelFactory}.
     *
     * @return the newly {@code SynchronousMessageBusChannelFactory}
     */
    public static SynchronousMessageBusChannelFactory synchronousMessageBusChannelFactory() {
        return new SynchronousMessageBusChannelFactory();
    }

    @Override
    public Channel<Object> createChannel(final EventType eventType,
                                         final Subscriber<?> subscriber,
                                         final MessageBusExceptionHandler messageBusExceptionHandler) {
        final DelegatingChannelExceptionHandler<Object> delegatingChannelExceptionHandler =
                delegatingChannelExceptionHandlerForDeliveryChannel(messageBusExceptionHandler);
        final Channel<Object> channel = ChannelBuilder.aChannel(Object.class)
                .withDefaultAction(Subscription.subscription())
                .withChannelExceptionHandler(delegatingChannelExceptionHandler)
                .build();
        delegatingChannelExceptionHandler.setChannel(channel);
        return channel;
    }
}
