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

package de.quantummaid.eventmaid.messagefunction.internal;

import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class SubscriptionContainer {
    private final MessageBus messageBus;
    private final AtomicReference<SubscriptionId> answerSubscriptionIdRef = new AtomicReference<>(null);
    private final AtomicReference<SubscriptionId> errorSubscriptionId1Ref = new AtomicReference<>(null);
    private final AtomicReference<SubscriptionId> errorSubscriptionId2Ref = new AtomicReference<>(null);

    public static SubscriptionContainer subscriptionContainer(final MessageBus messageBus) {
        return new SubscriptionContainer(messageBus);
    }

    public synchronized void setSubscriptionIds(final SubscriptionId answerSubscriptionId,
                                                final SubscriptionId errorSubscriptionId1,
                                                final SubscriptionId errorSubscriptionId2) {
        this.answerSubscriptionIdRef.set(answerSubscriptionId);
        this.errorSubscriptionId1Ref.set(errorSubscriptionId1);
        this.errorSubscriptionId2Ref.set(errorSubscriptionId2);
    }

    public void unsubscribe() {
        final SubscriptionId answerSubscription = answerSubscriptionIdRef.get();
        if (answerSubscription != null) {
            messageBus.unsubcribe(answerSubscription);
        }
        final SubscriptionId errorSubscriptionId1 = errorSubscriptionId1Ref.get();
        if (errorSubscriptionId1 != null) {
            messageBus.unregisterExceptionListener(errorSubscriptionId1);
        }
        final SubscriptionId errorSubscriptionId2 = errorSubscriptionId2Ref.get();
        if (errorSubscriptionId2 != null) {
            messageBus.unregisterExceptionListener(errorSubscriptionId2);
        }
    }

}
