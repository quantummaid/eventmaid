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

package de.quantummaid.messagemaid.channel.statistics;

import de.quantummaid.messagemaid.channel.Channel;
import de.quantummaid.messagemaid.filtering.Filter;
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
 * <p>The value of {@link ChannelStatistics#getAcceptedMessages()} defines the number of messages the
 * {@link Channel} has been accepted without an exception. {@link ChannelStatistics#getQueuedMessages() }
 * returns, how many messages have been accepted, but due to not enough resources have been queued. The processing of queued
 * messages will automatically be continued, when resources become available and the {@code Channel} is not closed before.
 * {@link ChannelStatistics#getBlockedMessages() } and {@link ChannelStatistics#getForgottenMessages()} relate to the results of
 * {@code Filters} being applied. Messages, that have been blocked by a {@link Filter} stop their propagation through the
 * {@code Channel}. Forgotten messages are those messages, that have not explicitly marked as passed or blocked by a
 * {@code Filter}. Usually they are the result of a bug inside on of the {@code Filters}. Once a message passed all {@code Filter}
 * the final {@code Action} is executed. The {@link ChannelStatistics#getSuccessfulMessages()} returns the number of messages,
 * that have been delivered without exceptions. In case of an exception during the delivery, the message is marked as failed.
 * {@link ChannelStatistics#getFailedMessages()} returns the amount of those messages.</p>
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#channel-statistics">MessageMaid Documentation</a>
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelStatistics {
    private final Date timestamp;
    private final BigInteger acceptedMessages;
    private final BigInteger queuedMessages;
    private final BigInteger blockedMessages;
    private final BigInteger forgottenMessages;
    private final BigInteger successfulMessages;
    private final BigInteger failedMessages;

    public static ChannelStatistics channelStatistics(final Date timestamp,
                                                      final BigInteger acceptedMessages,
                                                      final BigInteger queuedMessages,
                                                      final BigInteger blockedMessages,
                                                      final BigInteger forgottenMessages,
                                                      final BigInteger successfulMessages,
                                                      final BigInteger failedMessages) {
        return new ChannelStatistics(timestamp, acceptedMessages, queuedMessages, blockedMessages, forgottenMessages,
                successfulMessages, failedMessages);
    }

    public Date getTimestamp() {
        final long copyForSafeSharing = timestamp.getTime();
        return new Date(copyForSafeSharing);
    }

    public BigInteger getAcceptedMessages() {
        return this.acceptedMessages;
    }

    public BigInteger getQueuedMessages() {
        return this.queuedMessages;
    }

    public BigInteger getBlockedMessages() {
        return this.blockedMessages;
    }

    public BigInteger getForgottenMessages() {
        return this.forgottenMessages;
    }

    public BigInteger getSuccessfulMessages() {
        return this.successfulMessages;
    }

    public BigInteger getFailedMessages() {
        return this.failedMessages;
    }
}
