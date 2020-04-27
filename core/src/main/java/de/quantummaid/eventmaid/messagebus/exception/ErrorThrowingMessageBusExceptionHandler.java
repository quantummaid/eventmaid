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

package de.quantummaid.eventmaid.messagebus.exception;

import de.quantummaid.eventmaid.channel.Channel;
import de.quantummaid.eventmaid.channel.exception.ExceptionInFilterException;
import de.quantummaid.eventmaid.processingcontext.ProcessingContext;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * The default {@link MessageBusExceptionHandler} implementation, that rethrows all exceptions.
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class ErrorThrowingMessageBusExceptionHandler implements MessageBusExceptionHandler {

    /**
     * Factory method to create a new {@code ErrorThrowingMessageBusExceptionHandler}.
     *
     * @return the new {@code ErrorThrowingMessageBusExceptionHandler}.
     */
    public static ErrorThrowingMessageBusExceptionHandler errorThrowingMessageBusExceptionHandler() {
        return new ErrorThrowingMessageBusExceptionHandler();
    }

    @Override
    public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<Object> message,
                                                                         final Exception e,
                                                                         final Channel<Object> channel) {
        return true;
    }

    @Override
    public void handleDeliveryChannelException(final ProcessingContext<Object> message,
                                               final Exception e,
                                               final Channel<Object> channel) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new ExceptionInDeliveryChannelException(e);
        }
    }

    @Override
    public void handleFilterException(final ProcessingContext<Object> message, final Exception e, final Channel<Object> channel) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new ExceptionInFilterException(e);
        }
    }
}
