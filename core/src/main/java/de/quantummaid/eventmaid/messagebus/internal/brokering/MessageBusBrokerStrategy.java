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

package de.quantummaid.eventmaid.messagebus.internal.brokering;

import de.quantummaid.eventmaid.channel.Channel;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.processingcontext.ProcessingContext;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;

import java.util.List;
import java.util.Map;

@SuppressWarnings("java:S1452")
public interface MessageBusBrokerStrategy {

    Channel<Object> getDeliveringChannelFor(EventType eventType);

    void addSubscriber(EventType eventType, Subscriber<Object> subscriber);

    void addRawSubscriber(EventType eventType, Subscriber<ProcessingContext<Object>> subscriber);

    void removeSubscriber(SubscriptionId subscriptionId);

    List<Subscriber<?>> getAllSubscribers();

    Map<EventType, List<Subscriber<?>>> getSubscribersPerType();

}
