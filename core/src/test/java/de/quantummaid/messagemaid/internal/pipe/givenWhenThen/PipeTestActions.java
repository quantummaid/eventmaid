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

package de.quantummaid.messagemaid.internal.pipe.givenWhenThen;

import de.quantummaid.messagemaid.identification.MessageId;
import de.quantummaid.messagemaid.internal.pipe.Pipe;
import de.quantummaid.messagemaid.internal.pipe.PipeStatusInformation;
import de.quantummaid.messagemaid.internal.pipe.statistics.PipeStatistics;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.shared.pipeChannelMessageBus.testActions.SendingAndReceivingActions;
import de.quantummaid.messagemaid.shared.testMessages.TestMessage;
import de.quantummaid.messagemaid.subscribing.Subscriber;
import de.quantummaid.messagemaid.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static de.quantummaid.messagemaid.identification.MessageId.newUniqueMessageId;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeTestActions implements SendingAndReceivingActions {
    private final Pipe<TestMessage> pipe;

    public static PipeTestActions pipeTestActions(final Pipe<TestMessage> pipe) {
        return new PipeTestActions(pipe);
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        pipe.close(finishRemainingTasks);
    }

    @Override
    public boolean await(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return pipe.awaitTermination(timeout, timeUnit);
    }

    @Override
    public boolean isClosed() {
        return pipe.isClosed();
    }

    @Override
    public MessageId send(final EventType eventType, final TestMessage message) {
        pipe.send(message);
        return newUniqueMessageId();
    }

    @Override
    public void subscribe(final EventType eventType, final Subscriber<TestMessage> subscriber) {
        pipe.subscribe(subscriber);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        final PipeStatusInformation<TestMessage> statusInformation = pipe.getStatusInformation();
        final List<?> subscribers = statusInformation.getAllSubscribers();
        return (List<Subscriber<?>>) subscribers;
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        pipe.unsubscribe(subscriptionId);
    }

    @Override
    public long numberOfQueuedMessages() {
        return getTheNumberOfQueuedMessages();
    }

    public long getTheNumberOfAcceptedMessages() {
        return getMessageStatistics(PipeStatistics::getAcceptedMessages);
    }

    public long getTheNumberOfQueuedMessages() {
        return getMessageStatistics(PipeStatistics::getQueuedMessages);
    }

    public long getTheNumberOfSuccessfulDeliveredMessages() {
        return getMessageStatistics(PipeStatistics::getSuccessfulMessages);
    }

    public long getTheNumberOfFailedDeliveredMessages() {
        return getMessageStatistics(PipeStatistics::getFailedMessages);
    }

    public Date getTheTimestampOfTheMessageStatistics() {
        final PipeStatistics pipeStatistics = getPipeStatistics();
        final Date timestamp = pipeStatistics.getTimestamp();
        return timestamp;
    }

    private long getMessageStatistics(final Function<PipeStatistics, BigInteger> query) {
        final PipeStatistics pipeStatistics = getPipeStatistics();
        final BigInteger statistic = query.apply(pipeStatistics);
        return statistic.longValueExact();
    }

    private PipeStatistics getPipeStatistics() {
        final PipeStatusInformation<TestMessage> statusInformation = pipe.getStatusInformation();
        return statusInformation.getCurrentMessageStatistics();
    }
}
