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

package de.quantummaid.eventmaid.channel.givenWhenThen;

import de.quantummaid.eventmaid.channel.Channel;
import de.quantummaid.eventmaid.channel.ChannelProcessingFrame;
import de.quantummaid.eventmaid.channel.ChannelStatusInformation;
import de.quantummaid.eventmaid.channel.action.Action;
import de.quantummaid.eventmaid.channel.action.Subscription;
import de.quantummaid.eventmaid.channel.statistics.ChannelStatistics;
import de.quantummaid.eventmaid.filtering.Filter;
import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.identification.MessageId;
import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.pipeChannelMessageBus.testActions.*;
import de.quantummaid.eventmaid.shared.testMessages.TestMessage;
import de.quantummaid.eventmaid.shared.testMessages.TestMessageOfInterest;
import de.quantummaid.eventmaid.shared.utils.SendingTestUtils;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static de.quantummaid.eventmaid.channel.action.Call.callTo;
import static de.quantummaid.eventmaid.channel.givenWhenThen.ChannelTestProperties.MODIFIED_META_DATUM;
import static de.quantummaid.eventmaid.channel.givenWhenThen.FilterPosition.PROCESS;
import static de.quantummaid.eventmaid.processingContext.ProcessingContext.processingContext;
import static de.quantummaid.eventmaid.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelTestActions implements SendingAndReceivingActions, ProcessingContextSendingActions,
        CorrelationIdSendingActions, RawSubscribeActions, FilterTestActions {
    static final TestMessageOfInterest DEFAULT_TEST_MESSAGE = messageOfInterest();
    static final EventType DEFAULT_EVENT_TYPE = EventType.eventTypeFromString("defaultEventType");

    private final Channel<TestMessage> channel;

    public static ChannelTestActions channelTestActions(final Channel<TestMessage> channel) {
        return new ChannelTestActions(channel);
    }

    static ProcessingContext<TestMessage> sendMessage(final Channel<TestMessage> channel,
                                                      final TestEnvironment testEnvironment,
                                                      final TestMessage message) {
        final ChannelTestActions testActions = channelTestActions(channel);
        final ProcessingContext<TestMessage> processingContext = processingContext(DEFAULT_EVENT_TYPE, message);
        SendingTestUtils.sendProcessingContext(testActions, testEnvironment, processingContext);
        return processingContext;
    }

    static void addFilterExecutingACall(final Channel<TestMessage> channel, final Channel<TestMessage> targetChannel) {

        final Filter<ProcessingContext<TestMessage>> filter = (processingContext, filterActions) -> {
            callTo(targetChannel, processingContext);
            filterActions.pass(processingContext);
        };
        final ChannelTestActions testActions = ChannelTestActions.channelTestActions(channel);
        testActions.addFilter(filter, PROCESS);
    }

    static void addActionChangingFilterToPipe(final Channel<TestMessage> channel,
                                              final FilterPosition filterPosition,
                                              final Action<TestMessage> action) {
        final Filter<ProcessingContext<TestMessage>> filter = (processingContext, filterActions) -> {
            final ChannelProcessingFrame<TestMessage> currentProcessingFrame = processingContext.getCurrentProcessingFrame();
            currentProcessingFrame.setAction(action);
            filterActions.pass(processingContext);
        };
        final ChannelTestActions testActions = channelTestActions(channel);
        testActions.addFilter(filter, filterPosition);
    }

    static void addAFilterChangingMetaData(final Channel<TestMessage> channel, final Object metaDatum) {
        final Filter<ProcessingContext<TestMessage>> filter = (processingContext, filterActions) -> {
            final Map<Object, Object> metaData = processingContext.getContextMetaData();
            metaData.put(MODIFIED_META_DATUM, metaDatum);
            filterActions.pass(processingContext);
        };
        final ChannelTestActions testActions = ChannelTestActions.channelTestActions(channel);
        testActions.addFilter(filter, PROCESS);
    }

    static long queryChannelStatistics(final Channel<TestMessage> channel,
                                       final Function<ChannelStatistics, BigInteger> extraction) {
        final ChannelStatusInformation statusInformation = channel.getStatusInformation();
        final ChannelStatistics statistics = statusInformation.getChannelStatistics();
        final BigInteger result = extraction.apply(statistics);
        return result.longValueExact();
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        channel.close(finishRemainingTasks);
    }

    @Override
    public boolean await(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return channel.awaitTermination(timeout, timeUnit);
    }

    @Override
    public boolean isClosed() {
        return channel.isClosed();
    }

    @Override
    public MessageId send(final EventType eventType, final TestMessage message) {
        return channel.send(message);
    }

    @Override
    public MessageId send(final ProcessingContext<TestMessage> processingContext) {
        return channel.send(processingContext);
    }

    @Override
    public MessageId send(final EventType eventType, final TestMessage message, final CorrelationId correlationId) {
        return channel.send(message, correlationId);
    }

    @Override
    public void subscribe(final EventType eventType, final Subscriber<TestMessage> subscriber) {
        final Subscription<TestMessage> subscription = getActionAsSubscription();
        subscription.addSubscriber(subscriber);
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        final Subscription<TestMessage> subscription = getActionAsSubscription();
        subscription.removeSubscriber(subscriptionId);
    }

    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        final Subscription<TestMessage> subscription = getActionAsSubscription();
        return subscription.getAllSubscribers();
    }

    @Override
    public SubscriptionId subscribeRaw(final EventType eventType, final Subscriber<ProcessingContext<TestMessage>> subscriber) {
        final Subscription<TestMessage> subscription = getActionAsSubscription();
        return subscription.addRawSubscriber(subscriber);
    }

    @Override
    public void addFilter(final Filter<ProcessingContext<TestMessage>> filter, final FilterPosition filterPosition) {
        switch (filterPosition) {
            case PRE:
                channel.addPreFilter(filter);
                break;
            case PROCESS:
                channel.addProcessFilter(filter);
                break;
            case POST:
                channel.addPostFilter(filter);
                break;
            default:
                throw new UnsupportedOperationException("Unknown filterPosition " + filterPosition + ".");
        }
    }

    @Override
    public void addFilter(final Filter<ProcessingContext<TestMessage>> filter,
                          final FilterPosition filterPosition,
                          final int position) {
        switch (filterPosition) {
            case PRE:
                channel.addPreFilter(filter, position);
                break;
            case PROCESS:
                channel.addProcessFilter(filter, position);
                break;
            case POST:
                channel.addPostFilter(filter, position);
                break;
            default:
                throw new UnsupportedOperationException("Unknown filterPosition " + filterPosition + ".");
        }
    }

    @Override
    public List<?> getFilter(final FilterPosition filterPosition) {
        switch (filterPosition) {
            case PRE:
                return channel.getPreFilter();
            case PROCESS:
                return channel.getProcessFilter();
            case POST:
                return channel.getPostFilter();
            default:
                throw new UnsupportedOperationException("Unknown filterPosition " + filterPosition + ".");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeFilter(final Filter<?> filter, final FilterPosition filterPosition) {
        final Filter<ProcessingContext<TestMessage>> processingContextFilter = (Filter<ProcessingContext<TestMessage>>) filter;
        switch (filterPosition) {
            case PRE:
                channel.removePreFilter(processingContextFilter);
                break;
            case PROCESS:
                channel.removeProcessFilter(processingContextFilter);
                break;
            case POST:
                channel.removePostFilter(processingContextFilter);
                break;
            default:
                throw new UnsupportedOperationException("Unknown filterPosition " + filterPosition + ".");
        }
    }

    @Override
    public long numberOfQueuedMessages() {
        return queryChannelStatistics(channel, ChannelStatistics::getQueuedMessages);
    }

    private Subscription<TestMessage> getActionAsSubscription() {
        return (Subscription<TestMessage>) channel.getDefaultAction();
    }
}
