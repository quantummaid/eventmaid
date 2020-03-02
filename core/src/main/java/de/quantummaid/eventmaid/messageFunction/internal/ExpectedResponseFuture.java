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

import de.quantummaid.eventmaid.messageFunction.ResponseFuture;
import de.quantummaid.eventmaid.messageFunction.followup.FollowUpAction;
import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.*;
import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpectedResponseFuture implements ResponseFuture {
    private final SubscriptionContainer subscriptionContainer;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private volatile boolean isCancelled;
    private volatile ProcessingContext<Object> response;
    private volatile boolean successful;
    private volatile FollowUpAction followUpAction;
    private volatile Exception thrownException;

    public static ExpectedResponseFuture expectedResponseFuture(final SubscriptionContainer subscriptionContainer) {
        return new ExpectedResponseFuture(subscriptionContainer);
    }

    public synchronized void fullFill(final ProcessingContext<Object> response) {
        if (!isCancelled()) {
            final Object errorPayload = response.getErrorPayload();
            this.successful = errorPayload == null;
            this.response = response;
            finishFuture();
            if (followUpAction != null) {
                final Object payload = response.getPayload();
                followUpAction.apply(payload, errorPayload, null);
            }
        }
    }

    public synchronized void fullFillWithException(final Exception e) {
        if (!isCancelled()) {
            this.thrownException = e;
            this.successful = false;
            finishFuture();
            if (followUpAction != null) {
                followUpAction.apply(null, null, e);
            }
        }
    }

    private void finishFuture() {
        countDownLatch.countDown();
        subscriptionContainer.unsubscribe();
    }

    @Override
    public boolean wasSuccessful() {
        return !isCancelled && successful;
    }

    @Override
    public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
        if (!isDone()) {
            isCancelled = true;
        }
        finishFuture();
        return !alreadyCompleted();
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public boolean isDone() {
        return alreadyCompleted() || isCancelled() || hasExceptionInExecution();
    }

    private boolean alreadyCompleted() {
        return response != null;
    }

    private boolean hasExceptionInExecution() {
        return thrownException != null;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return getResponse(() -> response.getPayload());
    }

    @Override
    public Object get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return getResponse(() -> response.getPayload(), timeout, unit);
    }

    @Override
    public Object getErrorResponse() throws InterruptedException, ExecutionException {
        return getResponse(() -> response.getErrorPayload());
    }

    @Override
    public Object getErrorResponse(final long timeout,
                                   final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return getResponse(() -> response.getErrorPayload(), timeout, unit);
    }

    @Override
    public ProcessingContext<Object> getRaw() throws InterruptedException, ExecutionException {
        return getResponse(() -> response);
    }

    @Override
    public ProcessingContext<Object> getRaw(
            final long timeout,
            final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return getResponse(() -> response, timeout, unit);
    }

    private <T> T getResponse(final Supplier<T> responseSupplier) throws InterruptedException, ExecutionException {
        if (!isDone()) {
            countDownLatch.await();
            //if threads is woken up with countDown in "cancel", then it should be handled as Interrupt;
            if (isCancelled()) {
                throw new InterruptedException();
            }
        }
        if (hasExceptionInExecution()) {
            throw new ExecutionException(thrownException);
        } else if (isCancelled()) {
            throw new CancellationException();
        } else {
            return responseSupplier.get();
        }
    }

    private <T> T getResponse(
            final Supplier<T> responseSupplier,
            final long timeout,
            final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!isDone()) {
            if (!countDownLatch.await(timeout, unit)) {
                throw new TimeoutException("Response future timed out");
            }

            //if threads is woken up with countDown in "cancel", then it should be handled as Interrupt;
            if (isCancelled()) {
                throw new InterruptedException();
            }
        }
        if (isCancelled()) {
            throw new CancellationException();
        }
        if (hasExceptionInExecution()) {
            throw new ExecutionException(thrownException);
        } else {
            return responseSupplier.get();
        }
    }

    @Override
    public synchronized void then(final FollowUpAction followUpAction) {
        if (this.followUpAction != null) {
            throw new UnsupportedOperationException("Then can only be called once.");
        } else {
            this.followUpAction = followUpAction;
            if (isDone()) {
                if (isCancelled()) {
                    throw new CancellationException();
                } else {
                    if (response != null) {
                        final Object payload = response.getPayload();
                        final Object errorPayload = response.getErrorPayload();
                        followUpAction.apply(payload, errorPayload, null);
                    } else {
                        followUpAction.apply(null, null, this.thrownException);
                    }
                }
            }
        }
    }
}
