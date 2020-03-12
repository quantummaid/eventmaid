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

package de.quantummaid.eventmaid.shared.pipeChannelMessageBus.testActions;

import de.quantummaid.eventmaid.shared.exceptions.TestException;
import de.quantummaid.eventmaid.channel.ChannelProcessingFrame;
import de.quantummaid.eventmaid.filtering.Filter;
import de.quantummaid.eventmaid.filtering.FilterActions;
import de.quantummaid.eventmaid.identification.MessageId;
import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import de.quantummaid.eventmaid.shared.testMessages.TestMessageOfInterest;

import java.util.HashMap;

import static de.quantummaid.eventmaid.processingContext.ProcessingContext.processingContext;

public final class TestFilter {
    public static final String CHANGED_CONTENT = "CHANGED";
    public static final String ADDED_ERROR_CONTENT = "ERROR_CHANGED";

    public static Filter<TestMessageOfInterest> aContentChangingFilter() {
        return (TestMessageOfInterest testMessageOfInterest, FilterActions<TestMessageOfInterest> filterActions) -> {
            testMessageOfInterest.setContent(CHANGED_CONTENT);
            filterActions.pass(testMessageOfInterest);
        };
    }

    public static <T> Filter<T> aContentAppendingFilter(final String contentToAppend) {
        return (message, filterActions) -> {
            final TestMessageOfInterest testMessageOfInterest = (TestMessageOfInterest) message;
            testMessageOfInterest.setContent(testMessageOfInterest.getContent() + contentToAppend);
            filterActions.pass(message);
        };
    }

    public static <T> Filter<T> aMessageDroppingFilter() {
        return (message, filterActions) -> {
            filterActions.block(message);
        };
    }

    public static <T> Filter<T> aMessageFilterThatDoesNotCallAnyMethod() {
        return (message, filterActions) -> {

        };
    }

    public static <T> Filter<T> anErrorThrowingFilter(final RuntimeException exception) {
        return (message, filterActions) -> {
            throw exception;
        };
    }

    public static <T> Filter<T> anErrorThrowingFilter() {
        final TestException exception = new TestException();
        return anErrorThrowingFilter(exception);
    }

    public static Filter<ProcessingContext<Object>> aRawFilterThatChangesTheCompleteProcessingContext() {
        return (processingContext, filterActions) -> {
            final EventType eventType = processingContext.getEventType();
            final MessageId messageId = processingContext.getMessageId();
            final ChannelProcessingFrame<Object> currentProcessingFrame = processingContext.getCurrentProcessingFrame();
            final ChannelProcessingFrame<Object> initialProcessingFrame = processingContext.getInitialProcessingFrame();
            final ProcessingContext<Object> newProcessingContext = processingContext(eventType, messageId, null,
                    CHANGED_CONTENT, ADDED_ERROR_CONTENT, new HashMap<>(), initialProcessingFrame, currentProcessingFrame);
            filterActions.pass(newProcessingContext);
        };
    }

}
