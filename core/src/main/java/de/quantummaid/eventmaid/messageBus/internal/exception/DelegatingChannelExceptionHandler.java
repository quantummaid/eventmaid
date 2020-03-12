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

package de.quantummaid.eventmaid.messageBus.internal.exception;

import de.quantummaid.eventmaid.channel.Channel;
import de.quantummaid.eventmaid.channel.exception.ChannelExceptionHandler;
import de.quantummaid.eventmaid.internal.exceptions.BubbleUpWrappedException;
import de.quantummaid.eventmaid.messageBus.exception.MessageBusExceptionHandler;
import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class DelegatingChannelExceptionHandler<T> implements ChannelExceptionHandler<T> {
    private final MessageBusExceptionHandler messageBusExceptionHandler;
    private final DeliveryAbortDecision<T> deliveryAbortDecision;
    @Setter
    private Channel<Object> channel;

    public static <T> DelegatingChannelExceptionHandler<T> delegatingChannelExceptionHandlerForDeliveryChannel(
            final MessageBusExceptionHandler messageBusExceptionHandler) {
        final DeliveryAbortDecision<T> d = delegatingDeliveryAbortingException(messageBusExceptionHandler);
        return new DelegatingChannelExceptionHandler<>(messageBusExceptionHandler, d);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T> DeliveryAbortDecision<T> delegatingDeliveryAbortingException(
            final MessageBusExceptionHandler messageBusExceptionHandler) {
        return (message, e, c) -> {
            final ProcessingContext ungenericMessage = message;
            final Channel ungericChannel = c;
            //noinspection unchecked
            return messageBusExceptionHandler
                    .shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(ungenericMessage, e, ungericChannel);
        };
    }

    public static <T> DelegatingChannelExceptionHandler<T> delegatingChannelExceptionHandlerForAcceptingChannel(
            final MessageBusExceptionHandler messageBusExceptionHandler) {
        final DeliveryAbortDecision<T> d = (m, e, c) -> true;
        return new DelegatingChannelExceptionHandler<>(messageBusExceptionHandler, d);
    }

    @Override
    public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(final ProcessingContext<T> message, final Exception e) {
        return deliveryAbortDecision.shouldSubscriberErrorBeHandledAndDeliveryAborted(message, e, channel);
    }

    @Override
    public void handleSubscriberException(final ProcessingContext<T> message, final Exception e) {
        handleDeliveryException(message, e);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void handleDeliveryException(final ProcessingContext message, final Exception e) {
        messageBusExceptionHandler.handleDeliveryChannelException(message, e, channel);
    }

    @Override
    public void handleFilterException(final ProcessingContext<T> message, final Exception e) {
        handleFilterExceptionUngenerified(message, e);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void handleFilterExceptionUngenerified(final ProcessingContext message, final Exception e) {
        messageBusExceptionHandler.handleFilterException(message, e, channel);
    }

    @Override
    public void handleBubbledUpException(final BubbleUpWrappedException e) {
        throw e;
    }

    private interface DeliveryAbortDecision<T> {

        boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(ProcessingContext<T> m, Exception e, Channel<?> c);
    }
}
