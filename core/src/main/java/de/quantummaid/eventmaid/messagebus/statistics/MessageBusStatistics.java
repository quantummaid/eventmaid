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

package de.quantummaid.eventmaid.messagebus.statistics;

import de.quantummaid.eventmaid.channel.action.Action;
import de.quantummaid.eventmaid.filtering.Filter;
import de.quantummaid.eventmaid.messagebus.MessageBus;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigInteger;
import java.util.Date;

import static lombok.AccessLevel.PRIVATE;

/**
 * A collection of statistics about the messages, that have been processed up to the point the statistics were requested.
 *
 * <p>The timestamp defines the approximate time, when the statistics were queried. But no locking during the request is
 * performed. So the values should be seen as approximations.</p>
 *
 * <p>The value of {@link MessageBusStatistics#getAcceptedMessages()} defines the number of messages the {@link MessageBus} has
 * been accepted without an exception. {@link MessageBusStatistics#getQueuedMessages()} returns, how many messages have been
 * accepted, but due to not enough resources have been queued. The processing of queued messages will automatically be continued,
 * when resources become available and the {@code MessageBus} is not closed before.
 * {@link MessageBusStatistics#getBlockedMessages()} and {@link MessageBusStatistics#getForgottenMessages()} relate to the results
 * of {@link Filter Filters} being applied. Messages, that have been blocked by a {@code Filter} stop their propagation
 * through the {@code MessageBus}. Forgotten messages are those messages, that have not explicitly marked as passed or blocked by
 * a {@code Filter}. Usually they are the result of a bug inside on of the {@code Filters}. Once a message passed all
 * {@code Filters} the final {@link Action} is executed. The {@link MessageBusStatistics#getSuccessfulMessages()} returns the
 * number of messages, that have been delivered without exceptions. In case of an exception during the delivery, the message is
 * marked as failed. {@link MessageBusStatistics#getFailedMessages()} returns the amount of those messages.</p>
 *
 * @see <a href="https://github.com/quantummaid/eventmaid#messagebus-statistics">EventMaid Documentation</a>
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusStatistics {
    private final Date timestamp;
    private final BigInteger acceptedMessages;
    private final BigInteger successfulMessages;
    private final BigInteger failedMessages;
    private final BigInteger blockedMessages;
    private final BigInteger forgottenMessages;
    private final BigInteger queuedMessages;

    public static MessageBusStatistics messageBusStatistics(final Date timestamp,
                                                            final BigInteger acceptedMessages,
                                                            final BigInteger successfulMessages,
                                                            final BigInteger failedMessages,
                                                            final BigInteger blockedMessages,
                                                            final BigInteger forgottenMessages,
                                                            final BigInteger queuedMessages) {
        return new MessageBusStatistics(timestamp, acceptedMessages, successfulMessages, failedMessages, blockedMessages,
                forgottenMessages, queuedMessages);
    }

    public Date getTimestamp() {
        final long copyForSafeSharing = timestamp.getTime();
        return new Date(copyForSafeSharing);
    }

    public BigInteger getAcceptedMessages() {
        return this.acceptedMessages;
    }

    public BigInteger getSuccessfulMessages() {
        return this.successfulMessages;
    }

    public BigInteger getFailedMessages() {
        return this.failedMessages;
    }

    public BigInteger getBlockedMessages() {
        return this.blockedMessages;
    }

    public BigInteger getForgottenMessages() {
        return this.forgottenMessages;
    }

    public BigInteger getQueuedMessages() {
        return this.queuedMessages;
    }
}
