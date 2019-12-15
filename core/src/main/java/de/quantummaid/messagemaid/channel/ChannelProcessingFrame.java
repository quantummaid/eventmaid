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

package de.quantummaid.messagemaid.channel;

import de.quantummaid.messagemaid.channel.action.Action;
import de.quantummaid.messagemaid.processingContext.ProcessingContext;

/**
 * The {@link ProcessingContext} object stores the history of the traversed {@code Channels} in form of a linked list of
 * {@code ChannelProcessingFrames}. For each {@link Channel} one {@code ChannelProcessingFrame} is added to the end of the list.
 *
 * <p>Each {@code ProcessingContext} stores the corresponding {@code Channel}, the {@link Action} that was executed as well as
 * its preceding and succeeding frame. In case of the first frame in the list, the
 * {@link ChannelProcessingFrame#getPreviousFrame() ChannelProcessingFrame.getPreviousFrame()} returns null. Respective for the
 * last frame the {@code ChannelProcessingFrame#getNextFrame() ChannelStatistics.getNextFrame()} returns null.</p>
 *
 * @param <T> the type of messages of the {@code Channel}
 * @see <a href="https://github.com/quantummaid/messagemaid#processing-context">MessageMaid Documentation</a>
 */
public final class ChannelProcessingFrame<T> {
    private final Channel<T> channel;
    private ChannelProcessingFrame<T> previousFrame;
    private ChannelProcessingFrame<T> nextFrame;
    private Action<T> action;

    private ChannelProcessingFrame(final Channel<T> channel) {
        this.channel = channel;
    }

    private ChannelProcessingFrame(final Channel<T> channel, final ChannelProcessingFrame<T> previousFrame,
                                   final ChannelProcessingFrame<T> nextFrame, final Action<T> action) {
        this.channel = channel;
        this.previousFrame = previousFrame;
        this.nextFrame = nextFrame;
        this.action = action;
    }

    /**
     * Factory method to create a new {@code ChannelProcessingFrame} associated with the given {@code Channel}.
     *
     * @param channel the {@code Channel} this frame relates to
     * @param <T>     the type of the {@code Channel}
     * @return a new {@code ChannelProcessingFrame}
     */
    public static <T> ChannelProcessingFrame<T> processingFrame(final Channel<T> channel) {
        return new ChannelProcessingFrame<>(channel);
    }

    /**
     * Creates a exact shallow copy of the current {@code ChannelProcessingFrame}.
     *
     * @return a shallow copy of the current {@code ChannelProcessingFrame}
     */
    public ChannelProcessingFrame<T> copy() {
        return new ChannelProcessingFrame<>(channel, previousFrame, nextFrame, action);
    }

    public Channel<T> getChannel() {
        return this.channel;
    }

    public ChannelProcessingFrame<T> getPreviousFrame() {
        return this.previousFrame;
    }

    public void setPreviousFrame(final ChannelProcessingFrame<T> previousFrame) {
        this.previousFrame = previousFrame;
    }

    public ChannelProcessingFrame<T> getNextFrame() {
        return this.nextFrame;
    }

    public void setNextFrame(final ChannelProcessingFrame<T> nextFrame) {
        this.nextFrame = nextFrame;
    }

    public Action<T> getAction() {
        return this.action;
    }

    public void setAction(final Action<T> action) {
        this.action = action;
    }
}
