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

package de.quantummaid.messagemaid.messageBus;

import de.quantummaid.messagemaid.channel.Channel;
import de.quantummaid.messagemaid.messageBus.exception.MessageBusExceptionListener;
import de.quantummaid.messagemaid.messageBus.statistics.MessageBusStatistics;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.subscribing.Subscriber;

import java.util.List;
import java.util.Map;

/**
 * Gives access to the {@code MessageBus'} statistics and all of its currently registered {@code Subscribers}.
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#messagebus-statistics">MessageMaid Documentation</a>
 * @see <a href="https://github.com/quantummaid/messagemaid#querying-subscriber">MessageMaid Documentation</a>
 */
public interface MessageBusStatusInformation {

    /**
     * Collects the message statistics for this point in time.
     *
     * @return {@code MessageBusStatistics} with a timestamp
     */
    MessageBusStatistics getCurrentMessageStatistics();

    /**
     * Returns the list of all {@code Subscribers}.
     *
     * @return list of all {@code Subscribers}
     */
    List<Subscriber<?>> getAllSubscribers();

    /**
     * Returns all {@code Subscribers} grouped by their subscribed classes.
     *
     * @return map of classes and their {@code Subscribers}
     */
    Map<EventType, List<Subscriber<?>>> getSubscribersPerType();

    /**
     * Returns the {@code EventType} specific {@code Channel} for the given type or {@code null} if the type has not yet been
     * sent or subscribed.
     *
     * @param eventType the type of interest
     * @return the {@code Channel} of the class or {@code null}
     */
    Channel<Object> getChannelFor(EventType eventType);

    /**
     * Returns all {@code MessageBusExceptionListener} currently registered on the {@code MessageBus}.
     *
     * @return the list of {@code MessageBusExceptionListeners}
     */
    List<MessageBusExceptionListener> getAllExceptionListener();
}
