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

package de.quantummaid.messagemaid.channel.action;

import de.quantummaid.messagemaid.channel.Channel;
import de.quantummaid.messagemaid.channel.ChannelProcessingFrame;
import de.quantummaid.messagemaid.processingContext.ProcessingContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

/**
 * The {@code Call} {@code Action} is used to perform an immediate jump to a different Channel. It can not be used as default
 * {@code Action} of a {@code Channel}.
 *
 * @param <T> the type of messages of the {@code Channel}
 * @see <a href="https://github.com/quantummaid/messagemaid#call-and-return">MessageMaid Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class Call<T> implements Action<T> {
    @Getter
    private final Channel<T> targetChannel;
    @Getter
    @Setter
    private ChannelProcessingFrame<T> returnFrame;
    @Getter
    private ChannelProcessingFrame<T> processingFrameToContinueAfterReturn;

    /**
     * Factory method to create and execute a {@code Call} to the given {@code Channel}.
     *
     * @param targetChannel     the different {@code Channel}, that the message should be sent in
     * @param processingContext the message
     * @param <T>               the type of the other {@code Channel}
     * @return the executed {@code Call}
     */
    public static <T> Call<T> callTo(final Channel<T> targetChannel, final ProcessingContext<T> processingContext) {
        final Call<T> call = new Call<>(targetChannel);
        call.execute(processingContext);
        return call;
    }

    /**
     * Factory method to create a {@code Call} to the given {@code Channel}. The {@code Call} is not executed.
     *
     * @param targetChannel the different {@code Channel}, that the message should be sent in
     * @param <T>           the type of the other {@code Channel}
     * @return the {@code Call} {@code Action}
     */
    public static <T> Call<T> prepareACall(final Channel<T> targetChannel) {
        return new Call<>(targetChannel);
    }

    /**
     * Executed the {@code Call} {@code Action}. The message will be send in the other {@code Channel}.
     *
     * @param processingContext the message
     */
    public void execute(final ProcessingContext<T> processingContext) {
        final ChannelProcessingFrame<T> currentProcessingFrame = processingContext.getCurrentProcessingFrame();
        processingFrameToContinueAfterReturn = currentProcessingFrame.copy();
        currentProcessingFrame.setAction(this);
        targetChannel.send(processingContext);
    }

}
