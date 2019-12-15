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

package de.quantummaid.messagemaid.channel.internal.statistics;

import de.quantummaid.messagemaid.channel.statistics.ChannelStatistics;
import de.quantummaid.messagemaid.internal.pipe.Pipe;
import de.quantummaid.messagemaid.internal.pipe.statistics.PipeStatistics;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeStatisticsBasedChannelStatisticsCollector implements PartialCollectingChannelStatisticsCollector {
    private final Pipe<?> acceptingPipe;
    private final Pipe<?> deliveringPipe;
    private final AtomicLong messagesBlocked = new AtomicLong();
    private final AtomicLong messagesForgotten = new AtomicLong();
    private final AtomicLong exceptionsInFilter = new AtomicLong();

    public static PipeStatisticsBasedChannelStatisticsCollector pipeStatisticsBasedChannelStatisticsCollector(
            final Pipe<?> acceptingPipe,
            final Pipe<?> deliveringPipe) {
        return new PipeStatisticsBasedChannelStatisticsCollector(acceptingPipe, deliveringPipe);
    }

    @Override
    public void informMessageBlocked() {
        messagesBlocked.incrementAndGet();
    }

    @Override
    public void informMessageForgotten() {
        messagesForgotten.incrementAndGet();
    }

    @Override
    public void informExceptionInFilterThrown() {
        exceptionsInFilter.incrementAndGet();
    }

    @Override
    public ChannelStatistics getStatistics() {
        final PipeStatistics acceptingStatistics = statisticsOf(acceptingPipe);
        final PipeStatistics deliveringStatistics = statisticsOf(deliveringPipe);
        final BigInteger acceptedMessages = acceptingStatistics.getAcceptedMessages();
        final BigInteger queuedMessages = acceptingStatistics.getQueuedMessages();
        final BigInteger blockedMessages = asBigInt(messagesBlocked);
        final BigInteger forgottenMessages = asBigInt(messagesForgotten);
        final BigInteger successfulMessages = deliveringStatistics.getSuccessfulMessages();
        final BigInteger pipeFailedMessages = deliveringStatistics.getFailedMessages();
        final BigInteger deliveryFailedBecauseOfExceptionsInFilter = asBigInt(exceptionsInFilter);
        final BigInteger failedMessages = pipeFailedMessages.add(deliveryFailedBecauseOfExceptionsInFilter);
        final Date timestamp = new Date();
        return ChannelStatistics.channelStatistics(timestamp, acceptedMessages, queuedMessages, blockedMessages,
                forgottenMessages, successfulMessages, failedMessages);
    }

    private PipeStatistics statisticsOf(final Pipe<?> pipe) {
        return pipe.getStatusInformation().getCurrentMessageStatistics();
    }

    private BigInteger asBigInt(final AtomicLong atomicLong) {
        final long value = atomicLong.get();
        return BigInteger.valueOf(value);
    }
}
