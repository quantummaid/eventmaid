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

package de.quantummaid.eventmaid.messageFunction.internal;

import de.quantummaid.eventmaid.messageBus.MessageBus;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class SubscriptionContainer {
    private final MessageBus messageBus;
    private volatile SubscriptionId answerSubscriptionId;
    private volatile SubscriptionId errorSubscriptionId1;
    private volatile SubscriptionId errorSubscriptionId2;

    public static SubscriptionContainer subscriptionContainer(final MessageBus messageBus) {
        return new SubscriptionContainer(messageBus);
    }

    public void setSubscriptionIds(final SubscriptionId answerSubscriptionId,
                                   final SubscriptionId errorSubscriptionId1,
                                   final SubscriptionId errorSubscriptionId2) {
        this.answerSubscriptionId = answerSubscriptionId;
        this.errorSubscriptionId1 = errorSubscriptionId1;
        this.errorSubscriptionId2 = errorSubscriptionId2;
    }

    public void unsubscribe() {
        if (answerSubscriptionId != null) {
            messageBus.unsubcribe(answerSubscriptionId);
        }
        if (errorSubscriptionId1 != null) {
            messageBus.unregisterExceptionListener(errorSubscriptionId1);
        }
        if (errorSubscriptionId2 != null) {
            messageBus.unregisterExceptionListener(errorSubscriptionId2);
        }
    }

}
