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

package de.quantummaid.eventmaid.messagefunction;

import de.quantummaid.eventmaid.exceptions.AlreadyClosedException;
import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.identification.MessageId;
import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.messagefunction.internal.ExpectedResponseFuture;
import de.quantummaid.eventmaid.messagefunction.internal.SubscriptionContainer;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.processingcontext.ProcessingContext;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import lombok.Getter;
import lombok.NonNull;

final class MessageFunctionImpl implements MessageFunction {
    private final MessageBus messageBus;
    private volatile boolean closed;

    private MessageFunctionImpl(@NonNull final MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    static MessageFunctionImpl messageFunction(@NonNull final MessageBus messageBus) {
        return new MessageFunctionImpl(messageBus);
    }

    @Override
    public ResponseFuture request(final EventType eventType, final Object request) {
        if (closed) {
            throw new AlreadyClosedException();
        }
        final RequestHandle requestHandle = new RequestHandle(messageBus);
        requestHandle.send(eventType, request);
        return requestHandle.getResponseFuture();
    }

    //No automatic cancel right now
    @Override
    public void close() {
        closed = true;
    }

    private static final class RequestHandle {
        @Getter
        private final ExpectedResponseFuture responseFuture;
        private final MessageBus messageBus;
        private final SubscriptionContainer subscriptionContainer;
        private volatile boolean alreadyFinishedOrCancelled;

        RequestHandle(final MessageBus messageBus) {
            this.messageBus = messageBus;
            this.subscriptionContainer = SubscriptionContainer.subscriptionContainer(messageBus);
            this.responseFuture = ExpectedResponseFuture.expectedResponseFuture(this.subscriptionContainer);
        }

        public synchronized void send(final EventType eventType, final Object request) {
            final MessageId messageId = MessageId.newUniqueMessageId();
            final CorrelationId correlationId = CorrelationId.correlationIdFor(messageId);
            final SubscriptionId answerSubscriptionId = messageBus.subscribe(correlationId, this::fulFillFuture);
            final SubscriptionId errorSubscriptionId1 =
                    messageBus.onException(correlationId, (processingContext, e) -> fulFillFuture(e));
            final SubscriptionId errorSubscriptionId2 = messageBus.onException(eventType, (processingContext, e) -> {
                if (processingContext.getPayload() == request) {
                    fulFillFuture(e);
                }
            });
            subscriptionContainer.setSubscriptionIds(answerSubscriptionId, errorSubscriptionId1, errorSubscriptionId2);

            final ProcessingContext<Object> processingContext = ProcessingContext.processingContext(eventType, messageId, request);
            try {
                messageBus.send(processingContext);
            } catch (final Exception e) {
                fulFillFuture(e);
            }
        }

        private synchronized void fulFillFuture(final ProcessingContext<Object> processingContext) {
            if (alreadyFinishedOrCancelled) {
                return;
            }
            alreadyFinishedOrCancelled = true;
            responseFuture.fullFill(processingContext);
        }

        private synchronized void fulFillFuture(final Exception exception) {
            if (alreadyFinishedOrCancelled) {
                return;
            }
            alreadyFinishedOrCancelled = true;
            responseFuture.fullFillWithException(exception);
        }
    }

}
