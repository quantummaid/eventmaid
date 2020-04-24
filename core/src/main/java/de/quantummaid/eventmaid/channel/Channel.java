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

package de.quantummaid.eventmaid.channel;

import de.quantummaid.eventmaid.channel.action.Action;
import de.quantummaid.eventmaid.exceptions.AlreadyClosedException;
import de.quantummaid.eventmaid.filtering.Filter;
import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.identification.MessageId;
import de.quantummaid.eventmaid.internal.autoclosable.NoErrorAutoClosable;
import de.quantummaid.eventmaid.processingcontext.ProcessingContext;
import de.quantummaid.eventmaid.subscribing.Subscriber;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@code Channel} is the concept used for transporting messages from sender to an consuming {@link Action} at the end of the
 * {@code Channel}.
 *
 * <p>Each {@code Channel} has a default {@code Action}, which, if not changed by a {@link Filter}, is executed for every message
 * at the end of the transport. Different {@code Action} exists, that allow to add {@link Subscriber}, execute specific logic or
 * move the message to different {@code Channels}. During the transport {@code Filter} can be added, that alter the message, its
 * flow or the {@code Action}. {@code Channels} can be synchronous or asynchronous. Synchronous {@code Channel} execute the
 * transport on the Thread calling {@code send}. Asynchronous {@code Channels} provide their own Threads and mechanism to queue
 * messages, for which no Threads is available right away. Messages collect statistics over messages, that can be queried anytime.
 * During creation exception handler can be set, that control the {@code Channel's} behavior, when an exception is thrown.</p>
 *
 * <p>The {@code Channel} implements the {@link AutoCloseable} interface, so that it can be used in try-with-resource statements.
 * </p>
 *
 * @param <T> the type of messages send over this {@code Channel}
 * @see <a href="https://github.com/quantummaid/eventmaid#channel">EventMaid Documentation</a>
 * @see ChannelBuilder
 */
public interface Channel<T> extends NoErrorAutoClosable {

    /**
     * Send the given message over this {@code Channel}.
     *
     * @param message the message to be sent
     * @return the {@code MessageId} of the send message
     * @throws AlreadyClosedException if the {@code Channel} is already closed
     */
    MessageId send(T message);

    /**
     * Send the given message over this {@code Channel} with the given {@code CorrelationId}.
     *
     * @param message       the message to be sent
     * @param correlationId the {@code CorrelationId} of the message
     * @return the {@code MessageId} of the send message
     * @throws AlreadyClosedException if the {@code Channel} is already closed
     */
    MessageId send(T message, CorrelationId correlationId);

    /**
     * Send the given processingContext object over this {@code Channel}.
     *
     * <p>{@code Channels} use {@code ProcessingContext} objects internally to store and share processing relevant information.
     * Examples are a shared key-value map or the history of past {@code Channels}. In case several {@code Channels} are logical
     * connected and the information and history should be kept, {@code Channels} can accept the {@code ProcessingContext}
     * object of the previous {@code Channel} directly.</p>
     *
     * @param processingContext the {@code ProcessingContext} to be sent
     * @return the {@code ProcessingContext's} {@code MessageId}
     * @throws AlreadyClosedException if the {@code Channel} is already closed
     */
    MessageId send(ProcessingContext<T> processingContext);

    /**
     * Adds the {@code Filter} to the list of pre {@code Filter}.
     *
     * <p>Each {@code Channel} has three points, where {@code Filter} can be added: pre, process and post. All pre {@code Filter}
     * will always be executed before the first process {@code Filter}. The same goes for process and post {@code Filter}.</p>
     *
     * @param filter the {@code Filter} to be added
     */
    void addPreFilter(Filter<ProcessingContext<T>> filter);

    /**
     * Adds the {@code Filter} at the given position to the list of pre {@code Filter}.
     *
     * <p>Each {@code Channel} has three points, where {@code Filter} can be added: pre, process and post. All pre {@code Filter}
     * will always be executed before the first process {@code Filter}. The same goes for process and post {@code Filter}.</p>
     *
     * @param filter   the {@code Filter} to be added
     * @param position the position of the {@code Filter}
     * @throws ArrayIndexOutOfBoundsException if the position is higher than the number of {@code Filter} or negative
     */
    void addPreFilter(Filter<ProcessingContext<T>> filter, int position);

    /**
     * Returns a list of all {@code Filter} registered in the pre list.
     *
     * @return list of {@code Filter} in the pre position
     */
    List<Filter<ProcessingContext<T>>> getPreFilter();

    /**
     * Removes the {@code Filter} from the pre list.
     *
     * @param filter the {@code Filter} to be removed
     */
    void removePreFilter(Filter<ProcessingContext<T>> filter);

    /**
     * Adds the {@code Filter} to the list of process {@code Filter}.
     *
     * <p>Each {@code Channel} has three points, where {@code Filter} can be added: pre, process and post. All pre {@code Filter}
     * will always be executed before the first process {@code Filter}. The same goes for process and post {@code Filter}.</p>
     *
     * @param filter the {@code Filter} to be added
     */
    void addProcessFilter(Filter<ProcessingContext<T>> filter);

    /**
     * Adds the {@code Filter} at the given position to the list of process {@code Filter}.
     *
     * <p>Each {@code Channel} has three points, where {@code Filter} can be added: pre, process and post. All pre {@code Filter}
     * will always be executed before the first process {@code Filter}. The same goes for process and post {@code Filter}.</p>
     *
     * @param filter   the {@code Filter} to be added
     * @param position the position of the {@code Filter}
     * @throws ArrayIndexOutOfBoundsException if the position is higher than the number of {@code Filter} or negative
     */
    void addProcessFilter(Filter<ProcessingContext<T>> filter, int position);

    /**
     * Returns a list of all {@code Filter} registered in the process list.
     *
     * @return list of {@code Filter} in the process position
     */
    List<Filter<ProcessingContext<T>>> getProcessFilter();

    /**
     * Removes the {@code Filter} from the process list.
     *
     * @param filter the {@code Filter} to be removed
     */
    void removeProcessFilter(Filter<ProcessingContext<T>> filter);

    /**
     * Adds the {@code Filter} to the list of post {@code Filter}.
     *
     * <p>Each {@code Channel} has three points, where {@code Filter} can be added: pre, process and post. All pre {@code Filter}
     * will always be executed before the first process {@code Filter}. The same goes for process and post {@code Filter}.</p>
     *
     * @param filter the {@code Filter} to be added
     */
    void addPostFilter(Filter<ProcessingContext<T>> filter);

    /**
     * Adds the {@code Filter} at the given position to the list of post {@code Filter}.
     *
     * <p>Each {@code Channel} has three points, where {@code Filter} can be added: pre, process and post. All pre {@code Filter}
     * will always be executed before the first process {@code Filter}. The same goes for process and post {@code Filter}.</p>
     *
     * @param filter   the {@code Filter} to be added
     * @param position the position of the {@code Filter}
     * @throws ArrayIndexOutOfBoundsException if the position is higher than the number of {@code Filter} or negative
     */
    void addPostFilter(Filter<ProcessingContext<T>> filter, int position);

    /**
     * Returns a list of all {@code Filter} registered in the post list.
     *
     * @return list of {@code Filter} in the post position
     */
    List<Filter<ProcessingContext<T>>> getPostFilter();

    /**
     * Removes the {@code Filter} from the post list.
     *
     * @param filter the {@code Filter} to be removed
     */
    void removePostFilter(Filter<ProcessingContext<T>> filter);

    /**
     * Returns the default {@code Action} of this {@code Channel}.
     *
     * @return the default {@code Action} of this {@code Channel}
     */
    Action<T> getDefaultAction();

    /**
     * Returns a {@code ChannelStatusInformation} object, which can be used to query the {@code Channel's} statistics.
     *
     * @return a {@code ChannelStatusInformation} object
     */
    ChannelStatusInformation getStatusInformation();

    /**
     * Closes the {@code Channel} so that it shutdowns.
     *
     * <p>When setting the parameter to true, the {@code Channel} tries to finish remaining tasks, that are still pending. Setting
     * the parameter to false instructs the {@code Channel} to shutdown immediately. It is not defined how unfinished tasks
     * should be handled. Independent of the parameter, the {@code Channel} will be closed. All tries to send messages will
     * result in exceptions.</p>
     *
     * @param finishRemainingTasks boolean flag indicating, whether the {@code Channel} should try to finish pending tasks
     */
    void close(boolean finishRemainingTasks);

    /**
     * Returns {@code true} if {@code close} has been called on this {@code Channel}.
     *
     * @return true, if a {@code close} was already called, or false otherwise
     */
    boolean isClosed();

    /**
     * Blocks the caller until all remaining tasks have completed execution after a {@code close} has been called, the timeout
     * occurs or the current thread is interrupted.
     *
     * @param timeout  the duration to wait
     * @param timeUnit the time unit of the timeout
     * @return {@code true} if this {@code Channel} terminated,
     * {@code false} if the timeout elapsed before termination or
     * {@code false} if {@code close} was not yet called
     * @throws InterruptedException if interrupted while waiting
     */
    boolean awaitTermination(int timeout, TimeUnit timeUnit) throws InterruptedException;
}
