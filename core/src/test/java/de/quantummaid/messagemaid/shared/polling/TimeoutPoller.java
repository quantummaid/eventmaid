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

package de.quantummaid.messagemaid.shared.polling;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.function.BooleanSupplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TimeoutPoller implements Poller {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(1);
    private static final int POLLING_INTERVALL = 10;

    public static TimeoutPoller timeoutPoller() {
        return new TimeoutPoller();
    }

    @Override
    public void poll(final BooleanSupplier condition) {
        final TestStatePollingTimeoutException exception = new TestStatePollingTimeoutException();
        poll(condition, exception);
    }

    @Override
    public void poll(final BooleanSupplier condition, final String exceptionMessage) {
        final TestStatePollingTimeoutException exception = new TestStatePollingTimeoutException(exceptionMessage);
        poll(condition, exception);
    }

    private void poll(final BooleanSupplier condition, final TestStatePollingTimeoutException exception) {
        final long timeout = calculateTimeoutOffsetInMilliseconds();
        try {
            while (!condition.getAsBoolean()) {
                MILLISECONDS.sleep(POLLING_INTERVALL);
                if (timeout < System.currentTimeMillis()) {
                    throw exception;
                }
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private long calculateTimeoutOffsetInMilliseconds() {
        final long offset = DEFAULT_TIMEOUT.toMillis();
        return System.currentTimeMillis() + offset;
    }
}
