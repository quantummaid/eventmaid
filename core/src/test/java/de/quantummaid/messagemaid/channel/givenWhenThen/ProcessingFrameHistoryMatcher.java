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

package de.quantummaid.messagemaid.channel.givenWhenThen;

import de.quantummaid.messagemaid.channel.Channel;
import de.quantummaid.messagemaid.channel.ChannelProcessingFrame;
import de.quantummaid.messagemaid.channel.action.Action;
import de.quantummaid.messagemaid.channel.action.Call;
import de.quantummaid.messagemaid.channel.action.Return;
import de.quantummaid.messagemaid.processingContext.ProcessingContext;
import de.quantummaid.messagemaid.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RequiredArgsConstructor(access = PRIVATE)
final class ProcessingFrameHistoryMatcher {
    private final List<ExpectedProcessingFrame> expectedProcessingFrames = new ArrayList<>();

    static ProcessingFrameHistoryMatcher aProcessingFrameHistory() {
        return new ProcessingFrameHistoryMatcher();
    }

    @SuppressWarnings("rawtypes")
    ProcessingFrameHistoryMatcher withAFrameFor(final Channel<TestMessage> channel, final Class<? extends Action> actionClass) {
        final ExpectedProcessingFrame expectedProcessingFrame = new ExpectedProcessingFrameForActionClass(channel, actionClass);
        expectedProcessingFrames.add(expectedProcessingFrame);
        return this;
    }

    @SuppressWarnings("rawtypes")
    ProcessingFrameHistoryMatcher withAFrameFor(final Channel<TestMessage> channel, final Action<TestMessage> expectedAction) {
        final ExpectedProcessingFrame expectedProcessingFrame = new ExpectedProcessingFrameForAction(channel, expectedAction);
        expectedProcessingFrames.add(expectedProcessingFrame);
        return this;
    }

    void assertCorrect(final ProcessingContext<TestMessage> processingContext) {
        ChannelProcessingFrame<TestMessage> currentProcessingFrame = processingContext.getInitialProcessingFrame();
        assertFirstProcessingFrameCorrect(currentProcessingFrame);
        for (final ExpectedProcessingFrame expectedProcessingFrame : expectedProcessingFrames) {
            expectedProcessingFrame.assertEquals(currentProcessingFrame);
            assertDoubleLinkedListCorrectOf(currentProcessingFrame);

            assertCorrectnessOfCallOrReturnIfPresent(currentProcessingFrame);

            if (currentProcessingFrame.getNextFrame() != null) {
                currentProcessingFrame = currentProcessingFrame.getNextFrame();
            } else {
                assertLastProcessingFrameCorrect(currentProcessingFrame);
                currentProcessingFrame = currentProcessingFrame.getNextFrame();
            }
        }
    }

    private void assertFirstProcessingFrameCorrect(final ChannelProcessingFrame<TestMessage> firstProcessingFrame) {
        final ChannelProcessingFrame<TestMessage> previousFrame = firstProcessingFrame.getPreviousFrame();
        assertThat(previousFrame, equalTo(null));
    }

    private void assertDoubleLinkedListCorrectOf(final ChannelProcessingFrame<TestMessage> currentProcessingFrame) {
        final ChannelProcessingFrame<TestMessage> previousFrame = currentProcessingFrame.getPreviousFrame();
        if (previousFrame != null) {
            assertThat(previousFrame.getNextFrame(), equalTo(currentProcessingFrame));
        }
        final ChannelProcessingFrame<TestMessage> nextFrame = currentProcessingFrame.getNextFrame();
        if (nextFrame != null) {
            assertThat(nextFrame.getPreviousFrame(), equalTo(currentProcessingFrame));
        }
    }

    private void assertCorrectnessOfCallOrReturnIfPresent(final ChannelProcessingFrame<TestMessage> currentProcessingFrame) {
        final Action<TestMessage> action = currentProcessingFrame.getAction();
        final Call<TestMessage> callAction;
        final ChannelProcessingFrame<TestMessage> callProcessingFrame;
        final Return<TestMessage> returnAction;
        final ChannelProcessingFrame<TestMessage> returnProcessingFrame;
        if (action instanceof Call) {
            callAction = (Call<TestMessage>) action;
            callProcessingFrame = currentProcessingFrame;
            returnProcessingFrame = callAction.getReturnFrame();
            returnAction = (Return<TestMessage>) returnProcessingFrame.getAction();
        } else if (action instanceof Return) {
            returnAction = (Return<TestMessage>) action;
            returnProcessingFrame = currentProcessingFrame;
            callProcessingFrame = returnAction.getRelatedCallFrame();
            callAction = (Call<TestMessage>) callProcessingFrame.getAction();
        } else {
            return;
        }
        assertThat(returnProcessingFrame, notNullValue());
        assertThat(callProcessingFrame, notNullValue());
        assertThat(callAction.getReturnFrame(), equalTo(returnProcessingFrame));
        assertThat(returnAction.getRelatedCallFrame(), equalTo(callProcessingFrame));

        assertThat(returnProcessingFrame.getNextFrame(), equalTo(callAction.getProcessingFrameToContinueAfterReturn()));
    }

    private void assertLastProcessingFrameCorrect(final ChannelProcessingFrame<TestMessage> lastProcessingFrame) {
        final ChannelProcessingFrame<TestMessage> nextFrame = lastProcessingFrame.getNextFrame();
        assertThat(nextFrame, equalTo(null));
    }

    private interface ExpectedProcessingFrame {
        void assertEquals(ChannelProcessingFrame<TestMessage> channelProcessingFrame);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    @SuppressWarnings("rawtypes")
    private static final class ExpectedProcessingFrameForActionClass implements ExpectedProcessingFrame {
        private final Channel<TestMessage> expectedChannel;
        private final Class<? extends Action> expectedActionClass;

        public void assertEquals(final ChannelProcessingFrame<TestMessage> channelProcessingFrame) {
            final Channel<TestMessage> actualChannel = channelProcessingFrame.getChannel();
            assertThat(actualChannel, equalTo(expectedChannel));

            final Action<TestMessage> actualAction = channelProcessingFrame.getAction();
            assertThat(actualAction.getClass(), equalTo(expectedActionClass));
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    @SuppressWarnings("rawtypes")
    private static final class ExpectedProcessingFrameForAction implements ExpectedProcessingFrame {
        private final Channel<TestMessage> expectedChannel;
        private final Action<TestMessage> expectedAction;

        public void assertEquals(final ChannelProcessingFrame<TestMessage> channelProcessingFrame) {
            final Channel<TestMessage> actualChannel = channelProcessingFrame.getChannel();
            assertThat(actualChannel, equalTo(expectedChannel));

            final Action<TestMessage> actualAction = channelProcessingFrame.getAction();
            assertThat(actualAction, equalTo(expectedAction));
        }
    }
}
