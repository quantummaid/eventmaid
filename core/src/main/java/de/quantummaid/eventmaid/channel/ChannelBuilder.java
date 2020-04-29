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
import de.quantummaid.eventmaid.channel.action.ActionHandlerSet;
import de.quantummaid.eventmaid.channel.action.DefaultActionHandlerSet;
import de.quantummaid.eventmaid.channel.exception.ChannelExceptionHandler;
import de.quantummaid.eventmaid.channel.internal.events.ChannelEventListener;
import de.quantummaid.eventmaid.channel.internal.statistics.ChannelStatisticsCollector;
import de.quantummaid.eventmaid.channel.internal.statistics.PipeStatisticsBasedChannelStatisticsCollector;
import de.quantummaid.eventmaid.configuration.AsynchronousConfiguration;
import de.quantummaid.eventmaid.internal.pipe.Pipe;
import de.quantummaid.eventmaid.internal.pipe.PipeBuilder;
import de.quantummaid.eventmaid.internal.pipe.PipeType;
import de.quantummaid.eventmaid.internal.pipe.exceptions.PipeErrorHandler;
import de.quantummaid.eventmaid.processingcontext.ProcessingContext;

import static de.quantummaid.eventmaid.channel.ChannelType.SYNCHRONOUS;
import static de.quantummaid.eventmaid.channel.exception.ErrorThrowingChannelExceptionHandler.errorThrowingChannelExceptionHandler;
import static de.quantummaid.eventmaid.channel.internal.events.SimpleChannelEventListener.simpleChannelEventListener;
import static de.quantummaid.eventmaid.channel.internal.statistics.PipeStatisticsBasedChannelStatisticsCollector.pipeStatisticsBasedChannelStatisticsCollector;
import static de.quantummaid.eventmaid.internal.enforcing.NotNullEnforcer.ensureNotNull;

/**
 * The {@code ChannelBuilder} class provides a fluent interface to create and configure a {@link Channel}.
 *
 * <p>Most of the configurable properties have default values set by the builder. Only the default {@link Action} has to
 * be set manually. Per default a synchronous {@code Channel} is created with an exception handler, that throws
 * exception once they occur.</p>
 *
 * @param <T> the type of messages, that will be send over the created {@code Channel}
 * @see <a href="https://github.com/quantummaid/eventmaid/configuring-the-channel#">EventMaid Documentation</a>
 */
public class ChannelBuilder<T> {
    private Action<T> action;
    private ActionHandlerSet<T> actionHandlerSet;
    private ChannelEventListener<ProcessingContext<T>> eventListener;
    private ChannelStatisticsCollector statisticsCollector;
    private ChannelExceptionHandler<T> channelExceptionHandler = errorThrowingChannelExceptionHandler();
    private ChannelType type = SYNCHRONOUS;
    private AsynchronousConfiguration asynchronousConfiguration;

    /**
     * Returns a synchronous Channel with the default {@code Action}
     *
     * <p>This is a short, more convenient form for
     * <pre>{@code
     *      aChannel().withDefaultAction(action).build();
     * }</pre>
     *
     * @param defaultAction the {@code Channel's} default {@code Action}
     * @param <T>           type of messages of the created {@code Channel}
     * @return the created {@code Channel}
     */
    public static <T> Channel<T> aChannelWithDefaultAction(final Action<T> defaultAction) {
        return new ChannelBuilder<T>()
                .withDefaultAction(defaultAction)
                .build();
    }

    /**
     * Creates a new {@code ChannelBuilder}
     *
     * @param <T> type of messages of the created {@code Channel}
     * @return a new {@code ChannelBuilder}
     */
    public static <T> ChannelBuilder<T> aChannel() {
        return new ChannelBuilder<>();
    }

    /**
     * Creates a new {@code ChannelBuilder} for the given class
     *
     * @param channelTypeClass class of messages of the created {@code Channel}
     * @param <T>              type of messages of the created {@code Channel}
     * @return a new {@code ChannelBuilder}
     */
    @SuppressWarnings("unused")
    public static <T> ChannelBuilder<T> aChannel(final Class<T> channelTypeClass) {
        return new ChannelBuilder<>();
    }

    /**
     * Sets the type for the {@code Channel}. Can be {@code ChannelType.SYNCHRONOUS} or {@code ChannelType.ASYNCHRONOUS}.
     *
     * <p>Per default the type is set to synchronous and no further configuration is needed. If an asynchronous
     * {@code Channel} is to be created, an additional {@code AsynchronousConfiguration} has to be given. Also setting
     * a different {@code ChannelExceptionHandler} is advised, as the default exception handler throws all exception on
     * the executing Thread.
     * </p>
     *
     * @param type the type of the {@code Channel}. Can be {@code ChannelType.SYNCHRONOUS} or {@code ChannelType.ASYNCHRONOUS}.
     * @return the same {@code ChannelBuilder} instance the method was called one
     */
    public ChannelBuilder<T> forType(final ChannelType type) {
        this.type = type;
        return this;
    }

    /**
     * Adds an {@code AsynchronousConfiguration} to the {@code Channel}.
     *
     * <p>The asynchronous configuration is only used if the type of the
     * {@code Channel} is asynchronous.</p>
     *
     * @param configuration the configuration for the asynchronous {@code Channel}
     * @return the same {@code ChannelBuilder} instance the method was called one
     */
    public ChannelBuilder<T> withAsynchronousConfiguration(final AsynchronousConfiguration configuration) {
        this.asynchronousConfiguration = configuration;
        return this;
    }

    /**
     * Sets the default {@code Action} for the {@code Channel}.
     *
     * <p>If the {@code Action} is a custom one, make sure that a matching handler is contained in the ActionHandlerSet.</p>
     *
     * @param action the default {@code Action} of the {@code Channel}
     * @return the same {@code ChannelBuilder} instance the method was called one
     */
    public ChannelBuilder<T> withDefaultAction(final Action<T> action) {
        this.action = action;
        return this;
    }

    /**
     * Sets a different exception handler for the {@code Channel}.
     *
     * <p>Per default an exception handler is set, that rethrows all exceptions. This is not suitable for an asynchronous
     * setting. So any asynchronous {@code Channel} should have a custom exception handler set.</p>
     *
     * @param channelExceptionHandler the exception handler for the {@code Channel}
     * @return the same {@code ChannelBuilder} instance the method was called one
     */
    public ChannelBuilder<T> withChannelExceptionHandler(final ChannelExceptionHandler<T> channelExceptionHandler) {
        this.channelExceptionHandler = channelExceptionHandler;
        return this;
    }

    /**
     * Overwrites the default {@link ActionHandlerSet}, that can handle all built-in {@code Actions}.
     *
     * <p>Actions only contain relevant data. All logic about handling {@code Actions} at the end of the {@code Channel} is done
     * by the {@code ActionHandler}. For each {@code Action} a matching {@code ActionHandler} should be contained the
     * {@code ActionHandlerSet}. When using custom defined {@code Actions}, the {@code ActionHandlerSet} always have to be
     * modified, as an exception is raised, when an {@code Action} is encountered, for that no handler is known.</p>
     *
     * @param actionHandlerSet the new {@code ActionHandlerSet}
     * @return the same {@code ChannelBuilder} instance the method was called one
     */
    public ChannelBuilder<T> withActionHandlerSet(final ActionHandlerSet<T> actionHandlerSet) {
        this.actionHandlerSet = actionHandlerSet;
        return this;
    }

    /**
     * Creates the configured {@code Channel}.
     *
     * @return the configured {@code Channel}
     */
    public Channel<T> build() {
        ensureNotNull(action, "action");
        Pipe<ProcessingContext<T>> acceptingPipe = null;
        try {
            acceptingPipe = createAcceptingPipe(); // NOSONAR
            final Pipe<ProcessingContext<T>> prePipe = createSynchronousPipe(); // NOSONAR
            final Pipe<ProcessingContext<T>> processPipe = createSynchronousPipe(); // NOSONAR
            final Pipe<ProcessingContext<T>> postPipe = createDeliveringPipe(); // NOSONAR
            createStatisticsCollectorAndEventListenerSetup(acceptingPipe, postPipe);
            final ActionHandlerSet<T> createdActionHandlerSet = createDefaultActionHandlerSetIfAbsent();
            return ChannelImpl.channel(this.action, acceptingPipe, prePipe, processPipe, postPipe, eventListener, statisticsCollector,
                    createdActionHandlerSet, channelExceptionHandler);
        } catch (final RuntimeException e) {
            if (acceptingPipe != null) {
                acceptingPipe.close();
            }
            throw e;
        }
    }

    private Pipe<ProcessingContext<T>> createAcceptingPipe() {
        switch (type) {
            case SYNCHRONOUS:
                return createSynchronousPipe();
            case ASYNCHRONOUS:
                return PipeBuilder.<ProcessingContext<T>>aPipe()
                        .ofType(PipeType.ASYNCHRONOUS)
                        .withAsynchronousConfiguration(asynchronousConfiguration)
                        .build();
            default:
                throw new IllegalArgumentException("Unsupported channel type: " + type);
        }
    }

    private Pipe<ProcessingContext<T>> createSynchronousPipe() {
        final PipeBuilder<ProcessingContext<T>> pipeBuilder = PipeBuilder.aPipe();
        return pipeBuilder.ofType(PipeType.SYNCHRONOUS).build();
    }

    private Pipe<ProcessingContext<T>> createDeliveringPipe() {
        return PipeBuilder.<ProcessingContext<T>>aPipe()
                .ofType(PipeType.SYNCHRONOUS)
                .withErrorHandler(new PipeErrorHandler<>() {
                    @Override
                    public boolean shouldErrorBeHandledAndDeliveryAborted(final ProcessingContext<T> m, final Exception e) {
                        return channelExceptionHandler.shouldSubscriberErrorBeHandledAndDeliveryAborted(m, e);
                    }

                    @Override
                    public void handleException(final ProcessingContext<T> message, final Exception e) {
                        channelExceptionHandler.handleSubscriberException(message, e);
                    }
                }).build();
    }

    private void createStatisticsCollectorAndEventListenerSetup(final Pipe<ProcessingContext<T>> acceptingPipe,
                                                                final Pipe<ProcessingContext<T>> postPipe) {
        if (eventListener == null && statisticsCollector == null) {
            final PipeStatisticsBasedChannelStatisticsCollector createdStatisticsCollector =
                    pipeStatisticsBasedChannelStatisticsCollector(acceptingPipe, postPipe);
            this.statisticsCollector = createdStatisticsCollector;
            this.eventListener = simpleChannelEventListener(createdStatisticsCollector);
        }
    }

    private ActionHandlerSet<T> createDefaultActionHandlerSetIfAbsent() {
        if (this.actionHandlerSet != null) {
            return this.actionHandlerSet;
        } else {
            return DefaultActionHandlerSet.defaultActionHandlerSet();
        }
    }
}
