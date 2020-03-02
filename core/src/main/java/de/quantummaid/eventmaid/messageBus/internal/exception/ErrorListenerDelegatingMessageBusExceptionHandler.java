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
import de.quantummaid.eventmaid.internal.exceptions.BubbleUpWrappedException;
import de.quantummaid.eventmaid.messageBus.exception.MessageBusExceptionHandler;
import de.quantummaid.eventmaid.messageBus.exception.MessageBusExceptionListener;
import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ErrorListenerDelegatingMessageBusExceptionHandler implements MessageBusExceptionHandler {
    private final MessageBusExceptionHandler delegate;
    private final ExceptionListenerHandler exceptionListenerHandler;

    public static ErrorListenerDelegatingMessageBusExceptionHandler errorListenerDelegatingMessageBusExceptionHandler(
            final MessageBusExceptionHandler delegate, final ExceptionListenerHandler exceptionListenerHandler) {
        return new ErrorListenerDelegatingMessageBusExceptionHandler(delegate, exceptionListenerHandler);
    }

    @Override
    public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<Object> message,
                                                                         final Exception e,
                                                                         final Channel<Object> channel) {
        return delegate.shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(message, e, channel);
    }

    @Override
    public void handleDeliveryChannelException(final ProcessingContext<Object> message,
                                               final Exception e,
                                               final Channel<Object> channel) {
        try {
            callDeliveryExceptionHandlerIfNotBubbleUpException(message, e, channel);
        } finally {
            callTemporaryHandlerIfNotBubbleUpException(message, e);
        }
    }

    @Override
    public void handleFilterException(final ProcessingContext<Object> message, final Exception e, final Channel<Object> channel) {
        try {
            callFilterExceptionHandlerIfNotBubbleUpException(message, e, channel);
        } finally {
            callTemporaryHandlerIfNotBubbleUpException(message, e);
        }
    }

    private void callDeliveryExceptionHandlerIfNotBubbleUpException(final ProcessingContext<Object> message,
                                                                    final Exception e,
                                                                    final Channel<Object> channel) {
        if (e instanceof BubbleUpWrappedException) {
            return;
        }
        try {
            delegate.handleDeliveryChannelException(message, e, channel);
        } catch (final Exception rethrownException) {
            throw new BubbleUpWrappedException(rethrownException);
        }
    }

    private void callFilterExceptionHandlerIfNotBubbleUpException(final ProcessingContext<Object> message,
                                                                  final Exception e,
                                                                  final Channel<Object> channel) {
        if (e instanceof BubbleUpWrappedException) {
            return;
        }
        try {
            delegate.handleFilterException(message, e, channel);
        } catch (final Exception rethrownException) {
            throw new BubbleUpWrappedException(rethrownException);
        }
    }

    private void callTemporaryHandlerIfNotBubbleUpException(final ProcessingContext<Object> message,
                                                            final Exception e) {
        if (e instanceof BubbleUpWrappedException) {
            return;
        }
        final List<MessageBusExceptionListener> listener = exceptionListenerHandler.listenerFor(message);
        delegate.callTemporaryExceptionListener(message, e, listener);
    }
}
