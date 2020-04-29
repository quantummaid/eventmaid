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

package de.quantummaid.eventmaid.messagebus;

import de.quantummaid.eventmaid.channel.Channel;
import de.quantummaid.eventmaid.channel.ChannelType;
import de.quantummaid.eventmaid.configuration.AsynchronousConfiguration;
import de.quantummaid.eventmaid.messagebus.channelcreating.MessageBusChannelFactory;
import de.quantummaid.eventmaid.messagebus.channelcreating.SynchronousMessageBusChannelFactory;
import de.quantummaid.eventmaid.messagebus.exception.MessageBusExceptionHandler;
import de.quantummaid.eventmaid.messagebus.internal.brokering.MessageBusBrokerStrategy;
import de.quantummaid.eventmaid.messagebus.internal.brokering.MessageBusBrokerStrategyImpl;
import de.quantummaid.eventmaid.messagebus.internal.correlationids.CorrelationBasedSubscriptions;
import de.quantummaid.eventmaid.messagebus.internal.correlationids.CorrelationBasedSubscriptionsImpl;
import de.quantummaid.eventmaid.messagebus.internal.exception.DelegatingChannelExceptionHandler;
import de.quantummaid.eventmaid.messagebus.internal.exception.ExceptionListenerHandlerImpl;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.eventmaid.channel.ChannelBuilder.aChannel;
import static de.quantummaid.eventmaid.messagebus.MessageBusConsumeAction.messageBusConsumeAction;
import static de.quantummaid.eventmaid.messagebus.exception.ErrorThrowingMessageBusExceptionHandler.errorThrowingMessageBusExceptionHandler;
import static de.quantummaid.eventmaid.messagebus.internal.correlationids.CorrelationBasedSubscriptionsImpl.correlationBasedSubscriptions;
import static de.quantummaid.eventmaid.messagebus.internal.exception.ErrorListenerDelegatingMessageBusExceptionHandler.errorListenerDelegatingMessageBusExceptionHandler;
import static de.quantummaid.eventmaid.messagebus.internal.exception.ExceptionListenerHandlerImpl.errorListenerHandler;
import static java.util.Objects.requireNonNullElseGet;
import static lombok.AccessLevel.PRIVATE;

/**
 * The {@code MessageBusBuilder} class provides a fluent interface to create and configure a {@link MessageBus}.
 *
 * <p>Most of the configurable properties have default values set by the builder. Per default a synchronous {@code MessageBus}
 * is created with an exception handler, that throws exception once they occur. The default {@link MessageBusChannelFactory}
 * creates synchronous class specific {@code Channels}.</p>
 *
 * @see <a href="https://github.com/quantummaid/eventmaid/configuring-the-messagebus#">EventMaid Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusBuilder {
    private MessageBusChannelFactory channelFactory;
    private MessageBusType type = MessageBusType.SYNCHRONOUS;
    private AsynchronousConfiguration asynchronousConfiguration;
    private MessageBusExceptionHandler exceptionHandler = errorThrowingMessageBusExceptionHandler();

    /**
     * Creates a new {@code MessageBusBuilder}.
     *
     * @return a new {@code MessageBusBuilder}.
     */
    public static MessageBusBuilder aMessageBus() {
        return new MessageBusBuilder();
    }

    /**
     * Overrides the {@code MessageBusType}. Per default {@code MessageBusType.SYNCHRONOUS} is configured.
     *
     * @param type the {@code MessageBusType} to overwrite
     * @return the same {@code MessageBusBuilder} instance the method was called one
     */
    public MessageBusBuilder forType(final MessageBusType type) {
        this.type = type;
        return this;
    }

    /**
     * Overrides the the {@code MessageBusChannelFactory}.
     *
     * @param channelFactory the new {@code MessageBusChannelFactory}.
     * @return the same {@code MessageBusBuilder} instance the method was called one
     */
    public MessageBusBuilder withAChannelFactory(final MessageBusChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
        return this;
    }

    /**
     * In case an asynchronous {@code MessageBus} is created an {@code AsynchronousConfiguration} has to be provides with this
     * method.
     *
     * @param asynchronousConfiguration the required {@code AsynchronousConfiguration}
     * @return the same {@code MessageBusBuilder} instance the method was called one
     */
    public MessageBusBuilder withAsynchronousConfiguration(final AsynchronousConfiguration asynchronousConfiguration) {
        this.asynchronousConfiguration = asynchronousConfiguration;
        return this;
    }

    /**
     * Overrides the default exception throwing {@code MessageBusExceptionHandler}.
     *
     * @param exceptionHandler the new {@code MessageBusExceptionHandler}
     * @return the same {@code MessageBusBuilder} instance the method was called one
     */
    public MessageBusBuilder withExceptionHandler(final MessageBusExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    /**
     * Creates the {@code MessageBus}
     *
     * @return the newly created {@code MessageBus}
     */
    public MessageBus build() {
        final ExceptionListenerHandlerImpl errorListenerHandler = errorListenerHandler();
        final MessageBusExceptionHandler actualExceptionHandler = createExceptionHandler(errorListenerHandler);
        final MessageBusBrokerStrategy brokerStrategy = createBrokerStrategy(actualExceptionHandler);
        final CorrelationBasedSubscriptionsImpl correlationBasedSubscriptions = correlationBasedSubscriptions();

        Channel<Object> acceptingChannel = null;
        try {
            acceptingChannel = createAcceptingChannel(brokerStrategy, actualExceptionHandler,
                    correlationBasedSubscriptions);
            return new MessageBusImpl(acceptingChannel, brokerStrategy, correlationBasedSubscriptions, errorListenerHandler);
        } catch (final RuntimeException e) {
            if (acceptingChannel != null) {
                acceptingChannel.close();
            }
            throw e;
        }
    }

    private MessageBusBrokerStrategy createBrokerStrategy(final MessageBusExceptionHandler exceptionHandler) {
        final MessageBusChannelFactory createdChannelFactory = createChannelFactory();
        return MessageBusBrokerStrategyImpl.messageBusBrokerStrategyImpl(createdChannelFactory, exceptionHandler);
    }

    private MessageBusExceptionHandler createExceptionHandler(final ExceptionListenerHandlerImpl errorListenerHandler) {
        return errorListenerDelegatingMessageBusExceptionHandler(exceptionHandler, errorListenerHandler);
    }

    private MessageBusChannelFactory createChannelFactory() {
        return requireNonNullElseGet(this.channelFactory,
                SynchronousMessageBusChannelFactory::synchronousMessageBusChannelFactory);
    }

    private Channel<Object> createAcceptingChannel(final MessageBusBrokerStrategy brokerStrategy,
                                                   final MessageBusExceptionHandler exceptionHandler,
                                                   final CorrelationBasedSubscriptions correlationBasedSubscriptions) {
        final ChannelType channelType = map(type);
        final DelegatingChannelExceptionHandler<Object> acceptingPipeExceptionHandler =
                DelegatingChannelExceptionHandler.delegatingChannelExceptionHandlerForAcceptingChannel(exceptionHandler);
        final Channel<Object> acceptingChannel = aChannel()
                .forType(channelType)
                .withAsynchronousConfiguration(asynchronousConfiguration)
                .withChannelExceptionHandler(acceptingPipeExceptionHandler)
                .withDefaultAction(messageBusConsumeAction(brokerStrategy, correlationBasedSubscriptions))
                .build();
        acceptingPipeExceptionHandler.setChannel(acceptingChannel);
        return acceptingChannel;
    }

    private ChannelType map(final MessageBusType messageBusType) {
        switch (messageBusType) {
            case SYNCHRONOUS:
                return ChannelType.SYNCHRONOUS;
            case ASYNCHRONOUS:
                return ChannelType.ASYNCHRONOUS;
            default:
                throw new IllegalArgumentException("Unknown type for message bus: " + messageBusType);
        }
    }

}
