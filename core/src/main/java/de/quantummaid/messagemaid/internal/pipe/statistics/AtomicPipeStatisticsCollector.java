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

package de.quantummaid.messagemaid.internal.pipe.statistics;

import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import static de.quantummaid.messagemaid.internal.pipe.statistics.PipeStatistics.pipeStatistics;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class AtomicPipeStatisticsCollector implements PipeStatisticsCollector {
    private final AtomicLong acceptedMessages = new AtomicLong();
    private final AtomicLong queuedMessages = new AtomicLong();
    private final AtomicLong successfulMessages = new AtomicLong();
    private final AtomicLong failedMessages = new AtomicLong();

    public static AtomicPipeStatisticsCollector atomicPipeStatisticsCollector() {
        return new AtomicPipeStatisticsCollector();
    }

    @Override
    public PipeStatistics getCurrentStatistics() {
        final BigInteger acceptedMessages = asBigInt(this.acceptedMessages);
        final BigInteger queuedMessages = asBigInt(this.queuedMessages);
        final BigInteger successfulMessages = asBigInt(this.successfulMessages);
        final BigInteger failedMessages = asBigInt(this.failedMessages);
        final Date timestamp = new Date();
        return pipeStatistics(timestamp, acceptedMessages, queuedMessages, successfulMessages, failedMessages);
    }

    @Override
    public void informMessageAccepted() {
        acceptedMessages.incrementAndGet();
    }

    @Override
    public void informMessageQueued() {
        queuedMessages.incrementAndGet();
    }

    @Override
    public void informMessageDequeued() {
        queuedMessages.decrementAndGet();
    }

    @Override
    public void informMessageDeliveredSucceeded() {
        successfulMessages.incrementAndGet();
    }

    @Override
    public void informMessageDeliveryFailed() {
        failedMessages.incrementAndGet();
    }

    private BigInteger asBigInt(final AtomicLong atomicLong) {
        final long value = atomicLong.get();
        return BigInteger.valueOf(value);
    }
}
