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

package de.quantummaid.eventmaid.messagebus.internal.exception;

import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.messagebus.exception.MessageBusExceptionListener;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.processingcontext.ProcessingContext;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ExceptionListenerHandlerImpl implements ExceptionListenerHandler {
    private final Map<EventType, List<ListenerInformation>> eventTypeBasedListenerLookupMap = new ConcurrentHashMap<>();
    private final Map<CorrelationId, List<ListenerInformation>> correlationIdBasedListenerLookupMap = new ConcurrentHashMap<>();
    private final Map<SubscriptionId, ListenerInformation> unregisterLookupMap = new ConcurrentHashMap<>();

    public static ExceptionListenerHandlerImpl errorListenerHandler() {
        return new ExceptionListenerHandlerImpl();
    }

    @Override
    public synchronized SubscriptionId register(final EventType eventType,
                                                final MessageBusExceptionListener exceptionListener) {
        final ListenerInformation listenerInformation = new ListenerInformation(eventType, exceptionListener);
        storeListenerInformation(eventType, listenerInformation);

        final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
        unregisterLookupMap.put(subscriptionId, listenerInformation);
        return subscriptionId;
    }

    @Override
    public synchronized SubscriptionId register(final CorrelationId correlationId,
                                                final MessageBusExceptionListener exceptionListener) {
        final ListenerInformation listenerInformation = new ListenerInformation(correlationId, exceptionListener);
        storeListenerInformation(correlationId, listenerInformation);
        final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
        unregisterLookupMap.put(subscriptionId, listenerInformation);
        return subscriptionId;
    }

    private void storeListenerInformation(final EventType eventType, final ListenerInformation listenerInformation) {
        if (eventTypeBasedListenerLookupMap.containsKey(eventType)) {
            final List<ListenerInformation> listenerInformationList = eventTypeBasedListenerLookupMap.get(eventType);
            listenerInformationList.add(listenerInformation);
        } else {
            final List<ListenerInformation> listenerList = new LinkedList<>();
            listenerList.add(listenerInformation);
            eventTypeBasedListenerLookupMap.put(eventType, listenerList);
        }
    }

    private void storeListenerInformation(final CorrelationId correlationId, final ListenerInformation listenerInformation) {
        if (correlationIdBasedListenerLookupMap.containsKey(correlationId)) {
            correlationIdBasedListenerLookupMap.get(correlationId).add(listenerInformation);
        } else {
            final LinkedList<ListenerInformation> listenerInformationList = new LinkedList<>();
            listenerInformationList.add(listenerInformation);
            correlationIdBasedListenerLookupMap.put(correlationId, listenerInformationList);
        }
    }

    @Override
    public synchronized void unregister(final SubscriptionId subscriptionId) {
        if (unregisterLookupMap.containsKey(subscriptionId)) {
            final ListenerInformation listenerInformation = unregisterLookupMap.get(subscriptionId);
            removeClassBasedListener(listenerInformation);
            removeCorrelationIdBasedListener(listenerInformation);
            unregisterLookupMap.remove(subscriptionId);
        }
    }

    private void removeClassBasedListener(final ListenerInformation listenerInformation) {
        final List<EventType> eventTypes = listenerInformation.getRegisteredEventTypes();
        for (final EventType eventType : eventTypes) {
            final List<ListenerInformation> listenerInformationList = eventTypeBasedListenerLookupMap.get(eventType);
            if (listenerInformationList != null) {
                listenerInformationList.remove(listenerInformation);
            }
        }
    }

    private void removeCorrelationIdBasedListener(final ListenerInformation listenerInformation) {
        final List<CorrelationId> correlationIds = listenerInformation.getRegisteredCorrelationIds();
        for (final CorrelationId correlationId : correlationIds) {
            final List<ListenerInformation> listenerInformationList = correlationIdBasedListenerLookupMap.get(correlationId);
            if (listenerInformationList != null) {
                listenerInformationList.remove(listenerInformation);
            }
        }
    }

    @Override
    public List<MessageBusExceptionListener> listenerFor(final ProcessingContext<?> processingContext) {
        final List<MessageBusExceptionListener> listeners = new LinkedList<>();
        final CorrelationId correlationId = processingContext.getCorrelationId();
        if (correlationId != null) {
            final List<MessageBusExceptionListener> correlationBasedListeners = listenerForCorrelationId(correlationId);
            listeners.addAll(correlationBasedListeners);
        }

        final EventType eventType = processingContext.getEventType();
        final List<MessageBusExceptionListener> classBasedListener = listenerForEventType(eventType);
        listeners.addAll(classBasedListener);
        return listeners;
    }

    private List<MessageBusExceptionListener> listenerForEventType(final EventType eventType) {
        if (eventTypeBasedListenerLookupMap.containsKey(eventType)) {
            final List<ListenerInformation> listenerInformationList = eventTypeBasedListenerLookupMap.get(eventType);
            return extractListener(listenerInformationList);
        } else {
            return Collections.emptyList();
        }
    }

    private List<MessageBusExceptionListener> listenerForCorrelationId(final CorrelationId correlationId) {
        if (correlationIdBasedListenerLookupMap.containsKey(correlationId)) {
            final List<ListenerInformation> listenerInformationList = correlationIdBasedListenerLookupMap.get(correlationId);
            return extractListener(listenerInformationList);
        } else {
            return Collections.emptyList();
        }
    }

    private List<MessageBusExceptionListener> extractListener(final List<ListenerInformation> listenerInformationList) {
        return listenerInformationList.stream()
                .map(ListenerInformation::getListener)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageBusExceptionListener> allListener() {
        return this.unregisterLookupMap.values().stream()
                .map(ListenerInformation::getListener)
                .collect(Collectors.toList());
    }

    private static final class ListenerInformation {
        @Getter
        private final List<EventType> registeredEventTypes;
        @Getter
        private final List<CorrelationId> registeredCorrelationIds;
        @Getter
        private final MessageBusExceptionListener listener;

        private <T> ListenerInformation(final EventType eventType, final MessageBusExceptionListener listener) {
            this.registeredEventTypes = new LinkedList<>();
            this.registeredEventTypes.add(eventType);
            this.listener = listener;
            this.registeredCorrelationIds = new LinkedList<>();
        }

        private ListenerInformation(final CorrelationId registeredCorrelationId, final MessageBusExceptionListener listener) {
            this.registeredEventTypes = new LinkedList<>();
            this.listener = listener;
            this.registeredCorrelationIds = new LinkedList<>();
            this.registeredCorrelationIds.add(registeredCorrelationId);
        }
    }
}
