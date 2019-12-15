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

package de.quantummaid.messagemaid.configuration;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.MAX_VALUE;
import static java.util.concurrent.TimeUnit.SECONDS;

@ToString
@EqualsAndHashCode
public class AsynchronousConfiguration {
    public static final int DEFAULT_CORE_POOL_SIZE = 2;
    public static final int DEFAULT_MAXIMUM_POOL_SIZE = 2;
    public static final int DEFAULT_MAXIMUM_TIMEOUT = 60;
    public static final TimeUnit DEFAULT_TIMEUNIT = SECONDS;
    public static final LinkedBlockingQueue<Runnable> DEFAULT_WORKING_QUEUE = new LinkedBlockingQueue<>();
    @Getter
    @Setter
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    @Getter
    @Setter
    private int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;
    @Getter
    @Setter
    private int maximumTimeout = DEFAULT_MAXIMUM_TIMEOUT;
    @Getter
    @Setter
    private TimeUnit timeoutTimeUnit = DEFAULT_TIMEUNIT;
    @Getter
    @Setter
    private BlockingQueue<Runnable> threadPoolWorkingQueue = DEFAULT_WORKING_QUEUE;

    public AsynchronousConfiguration() {
    }

    public AsynchronousConfiguration(final int corePoolSize, final int maximumPoolSize, final int maximumTimeout,
                                     final TimeUnit timeoutTimeUnit, final BlockingQueue<Runnable> threadPoolWorkingQueue) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.maximumTimeout = maximumTimeout;
        this.timeoutTimeUnit = timeoutTimeUnit;
        this.threadPoolWorkingQueue = threadPoolWorkingQueue;
    }

    public static AsynchronousConfiguration constantPoolSizeAsynchronousConfiguration(final int poolSize) {
        final LinkedBlockingQueue<Runnable> threadPoolWorkingQueue = new LinkedBlockingQueue<>();
        return new AsynchronousConfiguration(poolSize, poolSize, MAX_VALUE, SECONDS, threadPoolWorkingQueue);
    }

    public static AsynchronousConfiguration constantPoolSizeAsynchronousConfiguration(final int poolSize,
                                                                                      final int waitingQueueBound) {
        final ArrayBlockingQueue<Runnable> threadPoolWorkingQueue = new ArrayBlockingQueue<>(waitingQueueBound);
        return new AsynchronousConfiguration(poolSize, poolSize, MAX_VALUE, SECONDS, threadPoolWorkingQueue);
    }

}
