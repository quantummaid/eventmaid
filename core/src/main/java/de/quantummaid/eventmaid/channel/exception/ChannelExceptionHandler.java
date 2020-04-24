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

import de.quantummaid.eventmaid.internal.exceptions.BubbleUpWrappedException;
import de.quantummaid.eventmaid.processingcontext.ProcessingContext;

/**
 * Whenever an exception is thrown within a {@code Filter} or the final {@code Action}, the {@code ChannelExceptionHandler} is
 * invoked.
 *
 * @param <T> the type of messages of the {@code Channel}
 * @see <a href="https://github.com/quantummaid/eventmaid#channelexceptionhandler">EventMaid Documentation</a>
 */
public interface ChannelExceptionHandler<T> {

    /**
     * When an exception is thrown inside the final action, this message can decide, if the message should be ignored or if
     * the delivery should be aborted and {@code handleSubscriberException} should be called.
     *
     * @param message the message, that caused the exception
     * @param e       the thrown exception
     * @return {@code} if the delivery should be aborted and the exception be handled; {@code false} if the exception should be
     * ignored
     */
    boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(ProcessingContext<T> message, Exception e);

    /**
     * When the delivery should be aborted, this method is called to handle the exception.
     *
     * @param message the message, that caused the exception
     * @param e       the thrown exception
     */
    void handleSubscriberException(ProcessingContext<T> message, Exception e);

    /**
     * Method, that is called, when an exception is thrown inside a {@code Filter}.
     *
     * @param message the message, that caused the exception
     * @param e       the thrown exception
     */
    void handleFilterException(ProcessingContext<T> message, Exception e);

    default void handleBubbledUpException(final BubbleUpWrappedException e) {
        throw (RuntimeException) e.getCause();
    }
}
