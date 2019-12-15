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

package de.quantummaid.messagemaid.processingContext;

import de.quantummaid.messagemaid.channel.Channel;
import de.quantummaid.messagemaid.channel.ChannelProcessingFrame;
import de.quantummaid.messagemaid.channel.action.Action;
import de.quantummaid.messagemaid.channel.action.Call;
import de.quantummaid.messagemaid.filtering.Filter;
import de.quantummaid.messagemaid.identification.CorrelationId;
import de.quantummaid.messagemaid.identification.MessageId;
import de.quantummaid.messagemaid.internal.enforcing.NotNullEnforcer;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

import static de.quantummaid.messagemaid.identification.CorrelationId.correlationIdFor;
import static de.quantummaid.messagemaid.identification.MessageId.newUniqueMessageId;

/**
 * Message specific root object for all information related to the processing of a message.
 *
 * <p>Each {@code ProcessingContext} envelopes the sent message. The message can be accesses with the
 * {@link ProcessingContext#getPayload()} and {@code #setPayload(T) setPayload()} methods. All {@link Action Actions} and
 * {@link Filter} get access to the {@code ProcessingContext} object. This allows them to share data using the
 * {@code ProcessingContext's} context meta date object. It is a {@code Map<Object, Object}, that can be accessed with
 * {@link ProcessingContext#getContextMetaData()}.</p>
 *
 * <p>In case several {@link Channel Channels} are chained together, a message traverses different channel in a specific order.
 * The transitions are handled via {@code Actions}. Given such a chained {@code Channel} scenario, the history can be of interest.
 * The history is represented in form of a linked list of {@link ChannelProcessingFrame ChannelProcessingFrames}.
 * For each traversed {@code Channel}, a new {@code ChannelProcessingFrame} is added at the end of the list. Once the
 * final {@code Action} is reached, it is also saved in the frame. The {@code ProcessingContext} object gives access to the
 * initial {@code ChannelProcessingFrame} with {@link ProcessingContext#getInitialProcessingFrame()}. The frame of the current
 * {@code Channel} ( or last if outside of one), can be accessed with {@link ProcessingContext#getCurrentProcessingFrame()}. An
 * exception is the {@link Call} {@code Action}. This {@code Action} is always executed once it was created and will never be the
 * final action of a {@code Channel}. In case a {@code Call} is executed, an extra {@code ChannelProcessingFrame} is added with
 * the {@code Call} {@code Action}, to represent the branching of the flow. All subsequent {@code Channel} will be contained
 * normally in the list of frames.</p>
 *
 * @param <T> the type of the processing {@code Channel}
 * @see <a href="https://github.com/quantummaid/messagemaid#processing-context">MessageMaid Documentation</a>
 */

@ToString
@EqualsAndHashCode
public final class ProcessingContext<T> {
    private final EventType eventType;
    private final MessageId messageId;
    private final Map<Object, Object> contextMetaData;
    private CorrelationId correlationId;
    private T payload;
    private Object errorPayload;

    private ChannelProcessingFrame<T> initialProcessingFrame;

    private ChannelProcessingFrame<T> currentProcessingFrame;

    private ProcessingContext(final EventType eventType,
                              final MessageId messageId,
                              final CorrelationId correlationId,
                              final T payload,
                              final Object errorPayload,
                              final Map<Object, Object> contextMetaData,
                              final ChannelProcessingFrame<T> initialProcessingFrame,
                              final ChannelProcessingFrame<T> currentProcessingFrame) {
        NotNullEnforcer.ensureNotNull(eventType, "eventType");
        this.eventType = eventType;
        NotNullEnforcer.ensureNotNull(messageId, "messageId");
        this.messageId = messageId;
        this.correlationId = correlationId;
        NotNullEnforcer.ensureNotNull(contextMetaData, "contextMetaData");
        this.contextMetaData = contextMetaData;
        this.payload = payload;
        this.errorPayload = errorPayload;
        this.initialProcessingFrame = initialProcessingFrame;
        this.currentProcessingFrame = currentProcessingFrame;
    }

    /**
     * Factory method to create a new {@code ProcessingContext}.
     *
     * @param eventType the event type of the message
     * @param payload   the message itself
     * @param <T>       the type of the message
     * @return the newly created {@code ProcessingContext}
     */
    public static <T> ProcessingContext<T> processingContext(final EventType eventType, final T payload) {
        final Map<Object, Object> contextMetaData = new HashMap<>();
        final MessageId messageId = newUniqueMessageId();
        return new ProcessingContext<>(eventType, messageId, null, payload, null, contextMetaData, null, null);
    }

    /**
     * Factory method to create a new {@code ProcessingContext}.
     *
     * @param eventType the event type of the message
     * @param messageId the unique {@code MessageId} of the message
     * @param payload   the message itself
     * @param <T>       the type of the message
     * @return the newly created {@code ProcessingContext}
     */
    public static <T> ProcessingContext<T> processingContext(final EventType eventType,
                                                             final MessageId messageId,
                                                             final T payload) {
        final Map<Object, Object> contextMetaData = new HashMap<>();
        return new ProcessingContext<>(eventType, messageId, null, payload, null, contextMetaData, null, null);
    }

    /**
     * Factory method to create a new {@code ProcessingContext}.
     *
     * @param eventType     the event type of the message
     * @param payload       the message itself
     * @param correlationId the {@code CorrelationId} identifying related messages
     * @param <T>           the type of the message
     * @return the newly created {@code ProcessingContext}
     */
    public static <T> ProcessingContext<T> processingContext(final EventType eventType,
                                                             final T payload,
                                                             final CorrelationId correlationId) {
        final Map<Object, Object> metaData = new HashMap<>();
        final MessageId messageId = newUniqueMessageId();
        return new ProcessingContext<>(eventType, messageId, correlationId, payload, null, metaData, null, null);
    }

    /**
     * Factory method to create a new {@code ProcessingContext}.
     *
     * @param eventType     the event type of the message
     * @param messageId     the unique {@code MessageId} of the message
     * @param correlationId the {@code CorrelationId} identifying related messages
     * @param payload       the message itself
     * @param errorPayload  an additional error message
     * @param <T>           the type of the message
     * @return the newly created {@code ProcessingContext}
     */
    public static <T> ProcessingContext<T> processingContext(final EventType eventType,
                                                             final MessageId messageId,
                                                             final CorrelationId correlationId,
                                                             final T payload,
                                                             final Object errorPayload) {
        final Map<Object, Object> metaData = new HashMap<>();
        return new ProcessingContext<>(eventType, messageId, correlationId, payload, errorPayload, metaData, null, null);
    }

    /**
     * Factory method to create a new {@code ProcessingContext}.
     *
     * @param eventType              the event type of the message
     * @param messageId              the unique {@code MessageId} of the message
     * @param correlationId          the {@code CorrelationId} identifying related messages
     * @param payload                the message itself
     * @param errorPayload           an additional error message
     * @param contextMetaData        a {@code Map} containing additional data of the messages's processing
     * @param initialProcessingFrame the {@code ChannelProcessingFrame} identifying the first {@link Channel}
     * @param currentProcessingFrame the {@code ChannelProcessingFrame} identifying the current {@code Channel}
     * @param <T>                    the type of the message
     * @return the newly created {@code ProcessingContext}
     */
    public static <T> ProcessingContext<T> processingContext(final EventType eventType,
                                                             final MessageId messageId,
                                                             final CorrelationId correlationId,
                                                             final T payload,
                                                             final Object errorPayload,
                                                             final Map<Object, Object> contextMetaData,
                                                             final ChannelProcessingFrame<T> initialProcessingFrame,
                                                             final ChannelProcessingFrame<T> currentProcessingFrame) {
        return new ProcessingContext<>(eventType, messageId, correlationId, payload, errorPayload, contextMetaData,
                initialProcessingFrame, currentProcessingFrame);
    }

    /**
     * Factory method to create a new {@code ProcessingContext}.
     *
     * @param eventType    the event type of the message
     * @param errorPayload an additional error message
     * @param <T>          the type of the message
     * @return the newly created {@code ProcessingContext}
     */
    public static <T> ProcessingContext<T> processingContextForError(final EventType eventType, final Object errorPayload) {
        final Map<Object, Object> contextMetaData = new HashMap<>();
        final MessageId messageId = newUniqueMessageId();
        return new ProcessingContext<>(eventType, messageId, null, null, errorPayload, contextMetaData, null, null);
    }

    /**
     * Factory method to create a new {@code ProcessingContext}.
     *
     * @param eventType    the event type of the message
     * @param payload      the message itself
     * @param errorPayload an additional error message
     * @param <T>          the type of the message
     * @return the newly created {@code ProcessingContext}
     */
    public static <T> ProcessingContext<T> processingContextForPayloadAndError(final EventType eventType,
                                                                               final T payload,
                                                                               final Object errorPayload) {
        final Map<Object, Object> contextMetaData = new HashMap<>();
        final MessageId messageId = newUniqueMessageId();
        return new ProcessingContext<>(eventType, messageId, null, payload, errorPayload, contextMetaData, null, null);
    }

    /**
     * Factory method to create a new {@code ProcessingContext}.
     *
     * @param eventType     the event type of the message
     * @param correlationId the {@code CorrelationId} identifying related messages
     * @param payload       the message itself
     * @param errorPayload  an additional error message
     * @param <T>           the type of the message
     * @return the newly created {@code ProcessingContext}
     */
    public static <T> ProcessingContext<T> processingContextForPayloadAndError(final EventType eventType,
                                                                               final CorrelationId correlationId,
                                                                               final T payload,
                                                                               final Object errorPayload) {
        final Map<Object, Object> contextMetaData = new HashMap<>();
        final MessageId messageId = newUniqueMessageId();
        return new ProcessingContext<>(eventType, messageId, correlationId, payload, errorPayload, contextMetaData, null, null);
    }

    /**
     * Returns, whether the default {@code Action} was overwritten for the current {@code Channel}
     *
     * @return {@code true} if the {@code Action} was changed and {@code false} otherwise
     */
    public boolean actionWasChanged() {
        return currentProcessingFrame.getAction() != null;
    }

    /**
     * Returns the {@code Action}, that overwrites the default one, if existing.
     *
     * @return the changed {@code Action} or null of no new {@code Action} was set
     */
    public Action<T> getAction() {
        return currentProcessingFrame.getAction();
    }

    /**
     * Overwrites the {@code Channel's} default {@code Action}.
     *
     * <p>At the end of the current {@code Channel} not the default {@code Action} is executed anymore. Instead the overwriting
     * {@code Action} is executed.</p>
     *
     * @param action the new {@code Action}
     */
    public void changeAction(final Action<T> action) {
        currentProcessingFrame.setAction(action);
    }

    /**
     * Creates a {@code CorrelationId} matching the current {@code ProcessingContext's}{@code MessageId}.
     *
     * @return a new, related {@code CorrelationId}
     */
    public CorrelationId generateCorrelationIdForAnswer() {
        return correlationIdFor(messageId);
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public MessageId getMessageId() {
        return this.messageId;
    }

    public Map<Object, Object> getContextMetaData() {
        return this.contextMetaData;
    }

    public CorrelationId getCorrelationId() {
        return this.correlationId;
    }

    public void setCorrelationId(final CorrelationId correlationId) {
        this.correlationId = correlationId;
    }

    public T getPayload() {
        return this.payload;
    }

    public void setPayload(final T payload) {
        this.payload = payload;
    }

    public Object getErrorPayload() {
        return this.errorPayload;
    }

    public void setErrorPayload(final Object errorPayload) {
        this.errorPayload = errorPayload;
    }

    public ChannelProcessingFrame<T> getInitialProcessingFrame() {
        return this.initialProcessingFrame;
    }

    public void setInitialProcessingFrame(final ChannelProcessingFrame<T> initialProcessingFrame) {
        this.initialProcessingFrame = initialProcessingFrame;
    }

    public ChannelProcessingFrame<T> getCurrentProcessingFrame() {
        return this.currentProcessingFrame;
    }

    public void setCurrentProcessingFrame(final ChannelProcessingFrame<T> currentProcessingFrame) {
        this.currentProcessingFrame = currentProcessingFrame;
    }
}
