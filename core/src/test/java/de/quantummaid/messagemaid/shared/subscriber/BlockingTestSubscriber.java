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

package de.quantummaid.messagemaid.shared.subscriber;

import de.quantummaid.messagemaid.subscribing.AcceptingBehavior;
import de.quantummaid.messagemaid.subscribing.SubscriptionId;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static de.quantummaid.messagemaid.subscribing.AcceptingBehavior.MESSAGE_ACCEPTED;

public final class BlockingTestSubscriber<T> implements TestSubscriber<T> {
    private final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
    private final Semaphore semaphoreToWaitUntilExecutionIsDone;
    private final List<T> receivedMessages = new CopyOnWriteArrayList<>();
    private final AtomicInteger blockedThreads = new AtomicInteger(0);

    private BlockingTestSubscriber(final Semaphore semaphoreToWaitUntilExecutionIsDone) {
        this.semaphoreToWaitUntilExecutionIsDone = semaphoreToWaitUntilExecutionIsDone;
    }

    public static <T> BlockingTestSubscriber<T> blockingTestSubscriber(final Semaphore semaphore) {
        return new BlockingTestSubscriber<>(semaphore);
    }

    @Override
    public AcceptingBehavior accept(final T message) {
        blockedThreads.incrementAndGet();
        try {
            semaphoreToWaitUntilExecutionIsDone.acquire();
            blockedThreads.decrementAndGet();
            receivedMessages.add(message);
        } catch (final InterruptedException ignored) {
            receivedMessages.add(message);
        }
        return MESSAGE_ACCEPTED;
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }

    public List<T> getReceivedMessages() {
        return receivedMessages;
    }

    public synchronized int getNumberOfBlockedThreads() {
        return blockedThreads.get();
    }
}
