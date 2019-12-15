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

package de.quantummaid.messagemaid.messageBus.exception;

import de.quantummaid.messagemaid.channel.Channel;
import de.quantummaid.messagemaid.channel.statistics.ChannelStatistics;
import de.quantummaid.messagemaid.filtering.Filter;
import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.processingContext.ProcessingContext;
import de.quantummaid.messagemaid.subscribing.Subscriber;

import java.util.List;

/**
 * Whenever an {@link Exception} occurs during the delivering of a message on a {@link MessageBus}, the
 * {@code MessageBusExceptionHandler} is invoked with the message and the exception.
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#messagebus">MessageMaid Documentation</a>
 */
public interface MessageBusExceptionHandler {

    /**
     * In some cases, an {@code Exception} during the delivery to a {@link Subscriber} should be ignored, so that the delivery is
     * not stopped. Therefore for each exception during the delivery this method is invoked. If {@code true} is returned,
     * the delivery stopped, the
     * {@link #handleDeliveryChannelException(ProcessingContext, Exception, Channel)
     * MessageBusExceptionHandler.handleDeliveryChannelException} method is invoked and
     * the message is marked as failed in {@link ChannelStatistics}. If {@code false} is returned, the exception is ignored
     * and the delivery continues.
     *
     * @param message the message for which the {@code Exception} was thrown
     * @param e       the raised {@code Exception}
     * @param channel the {@link EventType} specific channel, in which the message was delivered
     * @return {@code true} if the exception should be handled, {@code false} otherwise
     */
    boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(ProcessingContext<Object> message,
                                                                  Exception e,
                                                                  Channel<Object> channel);

    /**
     * For each {@code Exception} thrown inside a {@code Subscriber}, for which
     * {@link #shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(ProcessingContext, Exception, Channel)
     * MessageBusExceptionHandler.shouldDeliveryChannelErrorBeHandledAndDeliveryAborted} returned {@code true}, this method is
     * invoked.
     *
     * @param message the message, which caused the {@code Exception}
     * @param e       the raised {@code Exception}
     * @param channel the delivery {@code Channel}
     */
    void handleDeliveryChannelException(ProcessingContext<Object> message, Exception e, Channel<Object> channel);

    /**
     * When a {@code Exception} inside a {@link Filter} is thrown, this method is invoked.
     *
     * @param message the message that caused the {@code Exception}
     * @param e       the raised {@code Exception}
     * @param channel the accepting {@code Channel}
     */
    void handleFilterException(ProcessingContext<Object> message, Exception e, Channel<Object> channel);

    /**
     * For each exception, this method is responsible to call the dynamically added {@link MessageBusExceptionListener}.
     *
     * @param message   the message that caused the {@code Exception}
     * @param e         the raised {@code Exception}
     * @param listeners all interested {@code MessageBusExceptionListener}
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    default void callTemporaryExceptionListener(final ProcessingContext<Object> message,
                                                final Exception e,
                                                final List<MessageBusExceptionListener> listeners) {
        listeners.forEach(l -> l.accept(message, e));
    }
}
