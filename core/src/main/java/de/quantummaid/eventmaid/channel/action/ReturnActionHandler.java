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

package de.quantummaid.eventmaid.channel.action;

import de.quantummaid.eventmaid.channel.ChannelProcessingFrame;
import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * The {@code ActionHandler} implementation for the {@code Return} {@code Action}. This handler will go back through the
 * {@code ChannelProcessingFrame} history to obtain the last {@code Call} {@code Action}, that was not yet matched with an
 * {@code Return} {@code Action}. If found, it will link those two and will return back to the point the {@code Call} was
 * executed. If no not yet consumed {@code Call} was found, a {@code ReturnWithoutCallException} is thrown.
 *
 * @param <T> the type of messages of the {@code Channel}
 *
 * @see <a href="https://github.com/quantummaid/eventmaid#call-and-return">EventMaid Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class ReturnActionHandler<T> implements ActionHandler<Return<T>, T> {

    /**
     * Factory method for a new {@code ReturnActionHandler}.
     *
     * @param <T> the type of messages of the {@code Channel}
     * @return a new {@code ReturnActionHandler}
     */
    public static <T> ReturnActionHandler<T> returnActionHandler() {
        return new ReturnActionHandler<>();
    }

    /**
     * Will lookup the last {@code Call} {@code Action} and return from it.
     *
     * @param returnAction the {@code Return} {@code Action} to be handled
     * @param processingContext the message
     * @throws ReturnWithoutCallException if not not yet handled {@code Call} could be found
     */
    @Override
    public void handle(final Return<T> returnAction, final ProcessingContext<T> processingContext) {
        final ChannelProcessingFrame<T> currentProcessingFrame = processingContext.getCurrentProcessingFrame();
        final ChannelProcessingFrame<T> callProcessingFrame = locateLastNotYetReturnedCallProcessingFrame(currentProcessingFrame);

        final Call<T> callAction = (Call<T>) callProcessingFrame.getAction();
        callAction.setReturnFrame(currentProcessingFrame);
        returnAction.setRelatedCallFrame(callProcessingFrame);

        final ChannelProcessingFrame<T> nextProcessingFrame = callAction.getProcessingFrameToContinueAfterReturn();
        currentProcessingFrame.setNextFrame(nextProcessingFrame);
        nextProcessingFrame.setPreviousFrame(currentProcessingFrame);

        processingContext.setCurrentProcessingFrame(nextProcessingFrame);
    }

    private ChannelProcessingFrame<T> locateLastNotYetReturnedCallProcessingFrame(final ChannelProcessingFrame<T> latestFrame) {
        ChannelProcessingFrame<T> currentProcessingFrame = latestFrame;
        while (currentProcessingFrame != null) {
            final Action<T> action = currentProcessingFrame.getAction();
            if (action instanceof Call) {
                final Call<T> callAction = (Call<T>) action;
                final ChannelProcessingFrame<T> returnFrame = callAction.getReturnFrame();
                if (returnFrame == null) {
                    return currentProcessingFrame;
                } else {
                    currentProcessingFrame = currentProcessingFrame.getPreviousFrame();
                }
            } else {
                currentProcessingFrame = currentProcessingFrame.getPreviousFrame();
            }
        }
        throw new ReturnWithoutCallException();
    }
}
