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

package de.quantummaid.eventmaid.messageBus;

import de.quantummaid.eventmaid.channel.Channel;
import de.quantummaid.eventmaid.exceptions.AlreadyClosedException;
import de.quantummaid.eventmaid.filtering.Filter;
import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.identification.MessageId;
import de.quantummaid.eventmaid.internal.autoclosable.NoErrorAutoClosable;
import de.quantummaid.eventmaid.messageBus.exception.MessageBusExceptionListener;
import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Messages of different types can be sent over a {@code MessageBus}. {@link Subscriber Subscribers} interested in specific
 * messages can subscribe on these selected messages. A {@code MessageBus} allows adding {@link Filter Filters} to alter
 * the message flow.
 *
 * @see <a href="https://github.com/quantummaid/eventmaid#messagebus">EventMaid Documentation</a>
 */
public interface MessageBus extends NoErrorAutoClosable {

    /**
     * Sends the message with the given {@link EventType}.
     *
     * @param eventType the {@code EventType} to identify the message
     * @param object    the message
     * @return the {@link MessageId} of the send message
     * @throws AlreadyClosedException if {@code MessageBus} already closed
     */
    MessageId send(EventType eventType, Object object);

    /**
     * Sends the message with the given {@link EventType}.
     *
     * @param eventType     the {@code EventType} to identify the message
     * @param object        the message
     * @param correlationId the {@link CorrelationId} of the message
     * @return the {@link MessageId} of the send message
     * @throws AlreadyClosedException if {@code MessageBus} already closed
     */
    MessageId send(EventType eventType, Object object, CorrelationId correlationId);

    /**
     * Sends the {@link ProcessingContext} on the {@code MessageBus}.
     *
     * @param processingContext the {@code ProcessingContext} to send
     * @return the {@link MessageId} of the send message
     * @throws AlreadyClosedException if {@code MessageBus} already closed
     */
    MessageId send(ProcessingContext<Object> processingContext);

    /**
     * Adds the given {@link Consumer} wrapped in a {@link Subscriber} object for all messages with a matching
     * {@link EventType}.
     *
     * @param eventType the {@code EventType} of interest
     * @param consumer  the {@code Subscriber} to add
     * @return the {@link SubscriptionId} of the {@code Subscriber}
     */
    SubscriptionId subscribe(EventType eventType, Consumer<Object> consumer);

    /**
     * Adds the given {@link Subscriber} for all messages with a matching {@link EventType}.
     *
     * @param eventType  the {@code EventType} of interest
     * @param subscriber the {@code Subscriber} to add
     * @return the {@link SubscriptionId} of the {@code Subscriber}
     */
    SubscriptionId subscribe(EventType eventType, Subscriber<Object> subscriber);

    /**
     * Adds the given {@link Consumer} wrapped in a {@link Subscriber} object for all messages with a matching
     * {@link CorrelationId}.
     *
     * @param correlationId the {@code CorrelationId} of interest
     * @param consumer      the {@code Subscriber} to add
     * @return the {@link SubscriptionId} of the {@code Subscriber}
     */
    SubscriptionId subscribe(CorrelationId correlationId, Consumer<ProcessingContext<Object>> consumer);

    /**
     * Adds the given {@link Subscriber} for all messages with a matching {@link CorrelationId}.
     *
     * @param correlationId the {@code CorrelationId} of interest
     * @param subscriber    the {@code Subscriber} to add
     * @return the {@code SubscriptionId} of the {@code Subscriber}
     */
    SubscriptionId subscribe(CorrelationId correlationId, Subscriber<ProcessingContext<Object>> subscriber);

    /**
     * Adds the given {@link Consumer} wrapped in a {@link Subscriber} object for all messages with a matching
     * {@link EventType}. The {@code Subscriber} gets access to the underlying {@link ProcessingContext}.
     *
     * @param eventType the {@code EventType} of interest
     * @param consumer  the {@code Subscriber} to add
     * @return the {@link SubscriptionId} of the {@code Subscriber}
     */
    SubscriptionId subscribeRaw(EventType eventType, Consumer<ProcessingContext<Object>> consumer);

    /**
     * Adds the {@link Subscriber} object for all messages with a matching
     * {@link EventType}. The {@code Subscriber} gets access to the underlying {@link ProcessingContext}.
     *
     * @param eventType  the {@code EventType} of interest
     * @param subscriber the {@code Subscriber} to add
     * @return the {@link SubscriptionId} of the {@code Subscriber}
     */
    SubscriptionId subscribeRaw(EventType eventType, Subscriber<ProcessingContext<Object>> subscriber);

    /**
     * Removes all {@code Subscribers} with the given {@code SubscriptionId}
     *
     * @param subscriptionId the {@code SubscriptionId} to remove {@code Subscribers}
     */
    void unsubcribe(SubscriptionId subscriptionId);

    /**
     * Adds a {@link Filter} to the accepting {@link Channel}.
     *
     * @param filter the {@code Filter} to be added
     */
    void add(Filter<Object> filter);

    /**
     * Adds the {@link Filter} to the accepting {@link Channel}
     *
     * @param filter   the {@code Filter} to be added
     * @param position the position of the {@code Filter}
     * @throws ArrayIndexOutOfBoundsException if the position is higher than the number of {@code Filter} or negative
     */
    void add(Filter<Object> filter, int position);

    /**
     * Adds a {@link Filter} to the accepting {@link Channel} with access to the {@link ProcessingContext}.
     *
     * @param filter the {@code Filter} to be added
     */
    void addRaw(Filter<ProcessingContext<Object>> filter);

    /**
     * Adds a {@link Filter} to the accepting {@link Channel} with access to the {@link ProcessingContext} at the given position.
     *
     * @param filter   the {@code Filter} to be added
     * @param position the position of the {@code Filter}
     */
    void addRaw(Filter<ProcessingContext<Object>> filter, int position);

    /**
     * Returns all currently added {@code Filters}.
     *
     * @return all {@code Filters}
     */
    List<Filter<Object>> getFilter();

    /**
     * Removes the given {@code Filter}.
     *
     * @param filter the {@code Filter} to remove
     */
    void remove(Filter<Object> filter);

    /**
     * Adds a dynamic {@code MessageBusExceptionListener} for the messages with the {@link EventType}
     *
     * @param eventType         the {@code EventType} to match
     * @param exceptionListener the {@code MessageBusExceptionListener} to add
     * @return a {@code SubscriptionId} identifying exception listener
     */
    SubscriptionId onException(EventType eventType, MessageBusExceptionListener exceptionListener);

    /**
     * Adds a dynamic {@code MessageBusExceptionListener} for the messages matching the {@link CorrelationId}.
     *
     * @param correlationId     the {@code CorrelationId} to match
     * @param exceptionListener the {@code MessageBusExceptionListener} to add
     * @return a {@code SubscriptionId} identifying exception listener
     */
    SubscriptionId onException(CorrelationId correlationId, MessageBusExceptionListener exceptionListener);

    /**
     * Removes all exceptionListener with the given {@code SubscriptionId}.
     *
     * @param subscriptionId the {@code SubscriptionId} to remove exception listener for
     */
    void unregisterExceptionListener(SubscriptionId subscriptionId);

    /**
     * Removes the {@code MessageBusStatusInformation} interface, that allows querying statistics and {@code Subscribers}.
     *
     * @return the {@code MessageBusStatusInformation} interface
     */
    MessageBusStatusInformation getStatusInformation();

    /**
     * Closes the {@code MessageBus}.
     *
     * @param finishRemainingTasks when {@code true}, the {@code MessageBus} tries to deliver queued messages, otherwise
     *                             they are discarded
     */
    void close(boolean finishRemainingTasks);

    /**
     * Blocks the caller until all remaining tasks have completed execution after a {@code close} has been called, the timeout
     * occurs or the current thread is interrupted.
     *
     * @param timeout the duration to wait
     * @param unit    the time unit of the timeout
     * @return {@code true} if this {@code MessageBus} terminated,
     * {@code false} if the timeout elapsed before termination or
     * {@code false} if {@code close} was not yet called
     * @throws InterruptedException if interrupted while waiting
     */
    boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Returns {@code true} if {@code close} has been called on this {@code MessageBus}.
     *
     * @return true, if a {@code close} was already called, or false otherwise
     */
    boolean isClosed();
}
