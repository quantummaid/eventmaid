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

package de.quantummaid.eventmaid.internal.pipe.transport;

import de.quantummaid.eventmaid.internal.exceptions.BubbleUpWrappedException;
import de.quantummaid.eventmaid.internal.pipe.events.PipeEventListener;
import de.quantummaid.eventmaid.internal.pipe.exceptions.NoSuitableSubscriberException;
import de.quantummaid.eventmaid.internal.pipe.exceptions.PipeErrorHandler;
import de.quantummaid.eventmaid.subscribing.AcceptingBehavior;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class SynchronousDelivery<T> {
    private final PipeEventListener<T> eventListener;
    private final PipeErrorHandler<T> pipeErrorHandler;

    public void deliver(final T message, final List<Subscriber<T>> subscribers) {
        if (subscribers.isEmpty()) {
            handleNoSubscriberException(message);
        } else {
            for (final Subscriber<T> subscriber : subscribers) {
                final boolean continueDelivery = deliveryMessageTo(message, subscriber);
                if (!continueDelivery) {
                    return;
                }
            }
            eventListener.messageDeliverySucceeded(message);
        }
    }

    private void handleNoSubscriberException(final T message) {
        final NoSuitableSubscriberException exception = new NoSuitableSubscriberException();
        if (pipeErrorHandler.shouldErrorBeHandledAndDeliveryAborted(message, exception)) {
            eventListener.messageDeliveryFailed(message, exception);
            pipeErrorHandler.handleException(message, exception);
        } else {
            eventListener.messageDeliverySucceeded(message);
        }
    }

    private boolean deliveryMessageTo(final T message, final Subscriber<T> subscriber) {
        try {
            return deliverMessage(message, subscriber);
        } catch (final BubbleUpWrappedException e) {
            throw e;
        } catch (final Exception e) {
            return handleException(message, e);
        }
    }

    private boolean deliverMessage(final T message, final Subscriber<T> subscriber) {
        final AcceptingBehavior acceptingBehavior = subscriber.accept(message);
        final boolean proceedWithDelivery = acceptingBehavior.continueDelivery();
        if (!proceedWithDelivery) {
            eventListener.messageDeliverySucceeded(message);
            return false;
        } else {
            return true;
        }
    }

    private boolean handleException(final T message, final Exception e) {
        try {
            if (pipeErrorHandler.shouldErrorBeHandledAndDeliveryAborted(message, e)) {
                eventListener.messageDeliveryFailed(message, e);
                pipeErrorHandler.handleException(message, e);
                return false;
            } else {
                return true;
            }
        } catch (final BubbleUpWrappedException bubbledException) {
            throw bubbledException;
        } catch (final Exception rethrownException) {
            throw new BubbleUpWrappedException(rethrownException);
        }
    }
}
