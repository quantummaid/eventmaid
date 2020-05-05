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
import de.quantummaid.eventmaid.channel.action.ActionHandler;
import de.quantummaid.eventmaid.channel.action.ActionHandlerSet;
import de.quantummaid.eventmaid.channel.exception.ChannelExceptionHandler;
import de.quantummaid.eventmaid.channel.internal.events.ChannelEventListener;
import de.quantummaid.eventmaid.channel.internal.filtering.FilterApplier;
import de.quantummaid.eventmaid.channel.internal.filtering.FilterApplierImpl;
import de.quantummaid.eventmaid.channel.internal.filtering.PostFilterActions;
import de.quantummaid.eventmaid.channel.internal.statistics.ChannelStatisticsCollector;
import de.quantummaid.eventmaid.channel.statistics.ChannelStatistics;
import de.quantummaid.eventmaid.filtering.Filter;
import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.identification.MessageId;
import de.quantummaid.eventmaid.internal.exceptions.BubbleUpWrappedException;
import de.quantummaid.eventmaid.internal.pipe.Pipe;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.processingcontext.ProcessingContext;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static de.quantummaid.eventmaid.channel.ChannelProcessingFrame.processingFrame;
import static de.quantummaid.eventmaid.processingcontext.EventType.eventTypeFromObjectClass;
import static de.quantummaid.eventmaid.processingcontext.ProcessingContext.processingContext;
import static lombok.AccessLevel.PRIVATE;

@SuppressWarnings("java:S107")
final class ChannelImpl<T> implements Channel<T> {
    private final Pipe<ProcessingContext<T>> acceptingPipe;
    private final List<Filter<ProcessingContext<T>>> preFilter;
    private final List<Filter<ProcessingContext<T>>> processFilter;
    private final List<Filter<ProcessingContext<T>>> postFilter;
    private final Action<T> defaultAction;
    private final ChannelStatisticsCollector statisticsCollector;
    private final ChannelExceptionHandler<T> exceptionHandler;

    private ChannelImpl(final Pipe<ProcessingContext<T>> acceptingPipe, final Pipe<ProcessingContext<T>> preToProcessPipe,
                        final Pipe<ProcessingContext<T>> processToPostPipe, final Pipe<ProcessingContext<T>> afterPostPipe,
                        final Action<T> defaultAction, final ChannelEventListener<ProcessingContext<T>> eventListener,
                        final ChannelStatisticsCollector statisticsCollector,
                        final ActionHandlerSet<T> actionHandlerSet,
                        final ChannelExceptionHandler<T> exceptionHandler) {
        this.acceptingPipe = acceptingPipe;
        this.defaultAction = defaultAction;
        this.statisticsCollector = statisticsCollector;
        this.exceptionHandler = exceptionHandler;
        this.preFilter = new CopyOnWriteArrayList<>();
        this.processFilter = new CopyOnWriteArrayList<>();
        this.postFilter = new CopyOnWriteArrayList<>();
        acceptingPipe.subscribe(new AdvanceMessageUsingFilter(preFilter, preToProcessPipe, eventListener, exceptionHandler));
        preToProcessPipe.subscribe(
                new AdvanceMessageUsingFilter(processFilter, processToPostPipe, eventListener, exceptionHandler));
        processToPostPipe.subscribe(new AdvanceMessageUsingFilter(postFilter, afterPostPipe, eventListener, exceptionHandler));
        afterPostPipe.subscribe(new ConsumerExecutingActionSetByFilterOrDefaultAction(defaultAction, actionHandlerSet));
    }

    static <T> Channel<T> channel(final Action<T> defaultAction,
                                  final Pipe<ProcessingContext<T>> acceptingPipe,
                                  final Pipe<ProcessingContext<T>> prePipe,
                                  final Pipe<ProcessingContext<T>> processPipe,
                                  final Pipe<ProcessingContext<T>> postPipe,
                                  final ChannelEventListener<ProcessingContext<T>> eventListener,
                                  final ChannelStatisticsCollector statisticsCollector,
                                  final ActionHandlerSet<T> actionHandlerSet,
                                  final ChannelExceptionHandler<T> exceptionHandler) {
        return new ChannelImpl<>(acceptingPipe, prePipe, processPipe, postPipe, defaultAction, eventListener,
                statisticsCollector, actionHandlerSet, exceptionHandler);
    }

    @Override
    public MessageId send(final T message) {
        final EventType eventType = eventTypeFromObjectClass(message);
        final ProcessingContext<T> processingContext = processingContext(eventType, message);
        return send(processingContext);
    }

    @Override
    public MessageId send(final T message, final CorrelationId correlationId) {
        final EventType eventType = eventTypeFromObjectClass(message);
        final ProcessingContext<T> processingContext = processingContext(eventType, message, correlationId);
        return send(processingContext);
    }

    @Override
    public MessageId send(final ProcessingContext<T> processingContext) {
        final MessageId messageId = processingContext.getMessageId();
        try {
            advanceChannelProcessingFrameHistory(processingContext);
            acceptingPipe.send(processingContext);
            return messageId;
        } catch (final BubbleUpWrappedException e) {
            exceptionHandler.handleBubbledUpException(e);
            return messageId;
        }
    }

    private void advanceChannelProcessingFrameHistory(final ProcessingContext<T> processingContext) {
        final ChannelProcessingFrame<T> previousProcessingFrame = processingContext.getCurrentProcessingFrame();
        final ChannelProcessingFrame<T> currentProcessingFrame = processingFrame(this);
        if (noPreviousChannelTraversed(previousProcessingFrame)) {
            processingContext.setInitialProcessingFrame(currentProcessingFrame);
        } else {
            previousProcessingFrame.setNextFrame(currentProcessingFrame);
            currentProcessingFrame.setPreviousFrame(previousProcessingFrame);
        }
        processingContext.setCurrentProcessingFrame(currentProcessingFrame);
    }

    private boolean noPreviousChannelTraversed(final ChannelProcessingFrame<T> previousProcessingFrame) {
        return previousProcessingFrame == null;
    }

    @Override
    public void addPreFilter(final Filter<ProcessingContext<T>> filter) {
        preFilter.add(filter);
    }

    @Override
    public void addPreFilter(final Filter<ProcessingContext<T>> filter, final int position) {
        preFilter.add(position, filter);
    }

    @Override
    public List<Filter<ProcessingContext<T>>> getPreFilter() {
        return preFilter;
    }

    @Override
    public void removePreFilter(final Filter<ProcessingContext<T>> filter) {
        preFilter.remove(filter);
    }

    @Override
    public void addProcessFilter(final Filter<ProcessingContext<T>> filter) {
        processFilter.add(filter);
    }

    @Override
    public void addProcessFilter(final Filter<ProcessingContext<T>> filter, final int position) {
        processFilter.add(position, filter);
    }

    @Override
    public List<Filter<ProcessingContext<T>>> getProcessFilter() {
        return processFilter;
    }

    @Override
    public void removeProcessFilter(final Filter<ProcessingContext<T>> filter) {
        processFilter.remove(filter);
    }

    @Override
    public void addPostFilter(final Filter<ProcessingContext<T>> filter) {
        postFilter.add(filter);
    }

    @Override
    public void addPostFilter(final Filter<ProcessingContext<T>> filter, final int position) {
        postFilter.add(position, filter);
    }

    @Override
    public List<Filter<ProcessingContext<T>>> getPostFilter() {
        return postFilter;
    }

    @Override
    public void removePostFilter(final Filter<ProcessingContext<T>> filter) {
        postFilter.remove(filter);
    }

    @Override
    public Action<T> getDefaultAction() {
        return defaultAction;
    }

    @Override
    public ChannelStatusInformation getStatusInformation() {
        final ChannelStatistics statistics = statisticsCollector.getStatistics();
        return ChannelStatusInformation.channelStatusInformation(statistics);
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        //accepting Pipe is the only stateful pipe.callProcessingFrame
        //Also if closing other pipe, messages still being filtered might be causing ClosedPipeExceptions
        acceptingPipe.close(finishRemainingTasks);
    }

    @Override
    public void close() {
        close(false);
    }

    @Override
    public boolean isClosed() {
        return acceptingPipe.isClosed();
    }

    @Override
    public boolean awaitTermination(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return acceptingPipe.awaitTermination(timeout, timeUnit);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class ConsumerExecutingActionSetByFilterOrDefaultAction implements Consumer<ProcessingContext<T>> {
        private final Action<T> defaultAction;
        private final ActionHandlerSet<T> actionHandlerSet;

        @Override
        public void accept(final ProcessingContext<T> processingContext) {
            final Action<T> action;
            final Action<T> actionSetByFilter = processingContext.getAction();
            if (actionSetByFilter != null) {
                action = actionSetByFilter;
            } else {
                action = defaultAction;
                processingContext.changeAction(defaultAction);
            }
            final ActionHandler<Action<T>, T> actionHandler = actionHandlerSet.getActionHandlerFor(action);
            actionHandler.handle(action, processingContext);
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class AdvanceMessageUsingFilter implements Consumer<ProcessingContext<T>> {
        private final List<Filter<ProcessingContext<T>>> filter;
        private final Pipe<ProcessingContext<T>> nextPipe;
        private final ChannelEventListener<ProcessingContext<T>> eventListener;
        private final ChannelExceptionHandler<T> exceptionHandler;

        @Override
        public void accept(final ProcessingContext<T> preFilterprocessingContext) {
            final FilterApplier<ProcessingContext<T>> filterApplier = new FilterApplierImpl<>();
            try {
                filterApplier.applyAll(preFilterprocessingContext, filter, new PostFilterActions<>() {
                    @Override
                    public void onAllPassed(final ProcessingContext<T> processingContext) {
                        nextPipe.send(processingContext);
                    }

                    @Override
                    public void onBlock(final ProcessingContext<T> processingContext) {
                        eventListener.messageBlocked(processingContext);

                    }

                    @Override
                    public void onForgotten(final ProcessingContext<T> processingContext) {
                        eventListener.messageForgotten(processingContext);
                    }
                });
            } catch (final BubbleUpWrappedException e) {
                throw e;
            } catch (final Exception e) {
                try {
                    eventListener.exceptionInFilter(preFilterprocessingContext, e);
                    exceptionHandler.handleFilterException(preFilterprocessingContext, e);
                } catch (final BubbleUpWrappedException bubbledException) {
                    throw bubbledException;
                } catch (final Exception rethrownException) {
                    throw new BubbleUpWrappedException(e);
                }
            }
        }
    }
}
