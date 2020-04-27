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

package de.quantummaid.eventmaid.messagebus;

import de.quantummaid.eventmaid.channel.Channel;
import de.quantummaid.eventmaid.filtering.Filter;
import de.quantummaid.eventmaid.filtering.FilterActions;
import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.identification.MessageId;
import de.quantummaid.eventmaid.internal.exceptions.BubbleUpWrappedException;
import de.quantummaid.eventmaid.messagebus.exception.MessageBusExceptionListener;
import de.quantummaid.eventmaid.messagebus.internal.MessageBusStatusInformationAdapter;
import de.quantummaid.eventmaid.messagebus.internal.brokering.MessageBusBrokerStrategy;
import de.quantummaid.eventmaid.messagebus.internal.correlationids.CorrelationBasedSubscriptions;
import de.quantummaid.eventmaid.messagebus.internal.exception.ExceptionListenerHandler;
import de.quantummaid.eventmaid.messagebus.internal.statistics.ChannelBasedMessageBusStatisticsCollector;
import de.quantummaid.eventmaid.messagebus.internal.statistics.MessageBusStatisticsCollector;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.processingcontext.ProcessingContext;
import de.quantummaid.eventmaid.subscribing.ConsumerSubscriber;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static de.quantummaid.eventmaid.messagebus.internal.MessageBusStatusInformationAdapter.statusInformationAdapter;
import static lombok.AccessLevel.PRIVATE;

final class MessageBusImpl implements MessageBus {
    private final Channel<Object> acceptingChannel;
    private final MessageBusBrokerStrategy brokerStrategy;
    private final CorrelationBasedSubscriptions correlationBasedSubscriptions;
    private final ExceptionListenerHandler exceptionListenerHandler;
    private final MessageBusStatusInformationAdapter statusInformationAdapter;

    MessageBusImpl(final Channel<Object> acceptingChannel,
                   final MessageBusBrokerStrategy brokerStrategy,
                   final CorrelationBasedSubscriptions correlationBasedSubscriptions,
                   final ExceptionListenerHandler exceptionListenerHandler) {
        this.acceptingChannel = acceptingChannel;
        this.brokerStrategy = brokerStrategy;
        this.correlationBasedSubscriptions = correlationBasedSubscriptions;
        this.exceptionListenerHandler = exceptionListenerHandler;
        final MessageBusStatisticsCollector statisticsCollector = ChannelBasedMessageBusStatisticsCollector.channelBasedMessageBusStatisticsCollector(acceptingChannel);
        statusInformationAdapter = statusInformationAdapter(statisticsCollector, brokerStrategy, exceptionListenerHandler);
    }

    @Override
    public MessageId send(final EventType eventType, final Object object) {
        final ProcessingContext<Object> processingContext = ProcessingContext.processingContext(eventType, object);
        return send(processingContext);
    }

    @Override
    public MessageId send(final EventType eventType, final Object object, final CorrelationId correlationId) {
        final ProcessingContext<Object> processingContext = ProcessingContext.processingContext(eventType, object, correlationId);
        return send(processingContext);
    }

    @Override
    public MessageId send(final ProcessingContext<Object> processingContext) {
        try {
            return acceptingChannel.send(processingContext);
        } catch (final BubbleUpWrappedException e) {
            throw (RuntimeException) e.getCause();
        }
    }

    @Override
    public SubscriptionId subscribe(final EventType eventType, final Consumer<Object> consumer) {
        final ConsumerSubscriber<Object> subscriber = ConsumerSubscriber.consumerSubscriber(consumer);
        return subscribe(eventType, subscriber);
    }

    @Override
    public SubscriptionId subscribe(final EventType eventType, final Subscriber<Object> subscriber) {
        brokerStrategy.addSubscriber(eventType, subscriber);
        return subscriber.getSubscriptionId();
    }

    @Override
    public SubscriptionId subscribe(final CorrelationId correlationId, final Consumer<ProcessingContext<Object>> consumer) {
        final ConsumerSubscriber<ProcessingContext<Object>> subscriber = ConsumerSubscriber.consumerSubscriber(consumer);
        return subscribe(correlationId, subscriber);
    }

    @Override
    public SubscriptionId subscribe(final CorrelationId correlationId, final Subscriber<ProcessingContext<Object>> subscriber) {
        return correlationBasedSubscriptions.addCorrelationBasedSubscriber(correlationId, subscriber);
    }

    @Override
    public SubscriptionId subscribeRaw(final EventType eventType, final Consumer<ProcessingContext<Object>> consumer) {
        final ConsumerSubscriber<ProcessingContext<Object>> subscriber = ConsumerSubscriber.consumerSubscriber(consumer);
        return subscribeRaw(eventType, subscriber);
    }

    @Override
    public SubscriptionId subscribeRaw(final EventType eventType, final Subscriber<ProcessingContext<Object>> subscriber) {
        brokerStrategy.addRawSubscriber(eventType, subscriber);
        return subscriber.getSubscriptionId();
    }

    @Override
    public void unsubcribe(final SubscriptionId subscriptionId) {
        brokerStrategy.removeSubscriber(subscriptionId);
        correlationBasedSubscriptions.unsubscribe(subscriptionId);
    }

    @Override
    public void add(final Filter<Object> filter) {
        acceptingChannel.addProcessFilter(new FilterAdapter(filter));
    }

    @Override
    public void add(final Filter<Object> filter, final int position) {
        acceptingChannel.addProcessFilter(new FilterAdapter(filter), position);
    }

    @Override
    public void addRaw(final Filter<ProcessingContext<Object>> filter) {
        acceptingChannel.addProcessFilter(filter);
    }

    @Override
    public void addRaw(final Filter<ProcessingContext<Object>> filter, final int position) {
        acceptingChannel.addProcessFilter(filter, position);
    }

    @Override
    public List<Filter<Object>> getFilter() {
        final List<Filter<Object>> filters = new LinkedList<>();
        final List<Filter<ProcessingContext<Object>>> processFilter = acceptingChannel.getProcessFilter();
        for (final Filter<ProcessingContext<Object>> filter : processFilter) {
            if (filter instanceof FilterAdapter) {
                final Filter<Object> originalFilter = ((FilterAdapter) filter).delegate;
                filters.add(originalFilter);
            } else {
                throw new IllegalStateException("Unexpected type of filter. Was the list of filter tampered with?");
            }
        }
        return filters;
    }

    @Override
    public void remove(final Filter<Object> filter) {
        final List<Filter<ProcessingContext<Object>>> processFilter = acceptingChannel.getProcessFilter();
        for (final Filter<ProcessingContext<Object>> processingContextFilter : processFilter) {
            if (processingContextFilter instanceof FilterAdapter) {
                final FilterAdapter filterAdapter = (FilterAdapter) processingContextFilter;
                if (filterAdapter.delegate.equals(filter)) {
                    acceptingChannel.removeProcessFilter(processingContextFilter);
                }
            }
        }
    }

    @Override
    public SubscriptionId onException(final EventType eventType, final MessageBusExceptionListener exceptionListener) {
        return exceptionListenerHandler.register(eventType, exceptionListener);
    }

    @Override
    public SubscriptionId onException(final CorrelationId correlationId,
                                      final MessageBusExceptionListener exceptionListener) {
        return exceptionListenerHandler.register(correlationId, exceptionListener);
    }

    @Override
    public void unregisterExceptionListener(final SubscriptionId subscriptionId) {
        exceptionListenerHandler.unregister(subscriptionId);
    }

    @Override
    public MessageBusStatusInformation getStatusInformation() {
        return statusInformationAdapter;
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        acceptingChannel.close(finishRemainingTasks);
    }

    @Override
    public void close() {
        close(true);
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) {
        return true;
    }

    @Override
    public boolean isClosed() {
        return true;
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static final class FilterAdapter implements Filter<ProcessingContext<Object>> {
        private final Filter<Object> delegate;

        @Override
        public void apply(final ProcessingContext<Object> processingContext,
                          final FilterActions<ProcessingContext<Object>> filterActions) {
            final Object originalPayload = processingContext.getPayload();
            delegate.apply(originalPayload, new FilterActions<Object>() {
                @Override
                public void block(final Object message) {
                    if (originalPayload != message) {
                        processingContext.setPayload(message);
                    }
                    filterActions.block(processingContext);
                }

                @Override
                public void pass(final Object message) {
                    if (originalPayload != message) {
                        processingContext.setPayload(message);
                    }
                    filterActions.pass(processingContext);
                }
            });
        }
    }
}
