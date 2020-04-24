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

package de.quantummaid.eventmaid.channel.exception;

import de.quantummaid.eventmaid.processingcontext.ProcessingContext;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * A {@code ChannelExceptionHandler}, that will throw every exception on the current {@code Thread}.
 *
 * @param <T> the type of the message
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class ErrorThrowingChannelExceptionHandler<T> implements ChannelExceptionHandler<T> {

    /**
     * Factory method to create a new {@code ErrorThrowingChannelExceptionHandler}.
     *
     * @param <T> the type of messages of the {@code Channel}
     * @return a new {@code ErrorThrowingChannelExceptionHandler}
     */
    public static <T> ErrorThrowingChannelExceptionHandler<T> errorThrowingChannelExceptionHandler() {
        return new ErrorThrowingChannelExceptionHandler<>();
    }

    @Override
    public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(final ProcessingContext<T> message, final Exception e) {
        return true;
    }

    @Override
    public void handleSubscriberException(final ProcessingContext<T> message, final Exception e) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new ExceptionInSubscriberException(e);
        }
    }

    @Override
    public void handleFilterException(final ProcessingContext<T> message, final Exception e) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new ExceptionInFilterException(e);
        }
    }
}
