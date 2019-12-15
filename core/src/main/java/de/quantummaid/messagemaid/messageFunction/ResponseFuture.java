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

package de.quantummaid.messagemaid.messageFunction;

import de.quantummaid.messagemaid.messageFunction.followup.FollowUpAction;
import de.quantummaid.messagemaid.processingContext.ProcessingContext;

import java.util.concurrent.*;

/**
 * For each request, the related {@code ResponseFuture} provides methods, to query or wait on the result.
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#responsefuture">MessageMaid Documentation</a>
 */

public interface ResponseFuture extends Future<Object> {

    /**
     * Returns {@code true} if the future was fulfilled with an success response, {@code false} if an exception occurred,
     * the future was cancelled or the error payload is not {@code null}.
     *
     * @return {@code true} if only payload is set, {@code false} otherwise
     */
    boolean wasSuccessful();

    /**
     * Waits until the future is fulfilled and returns the error payload.
     *
     * @return the error payload of the message or {@code null} if none exists
     * @throws CancellationException if the future was cancelled
     * @throws InterruptedException  if the waiting {@link Thread} is interrupted
     * @throws ExecutionException    if the future was fulfilled with an exception
     */
    Object getErrorResponse() throws InterruptedException, ExecutionException;

    /**
     * Waits until the future is fulfilled to return the error payload or the timeout expires.
     *
     * @param timeout the interval to wait
     * @param unit    the unit of the interval
     * @return the error payload of the message or {@code null} if none exists
     * @throws CancellationException if the future was cancelled
     * @throws InterruptedException  if the waiting {@link Thread} is interrupted
     * @throws ExecutionException    if the future was fulfilled with an exception
     * @throws TimeoutException      if the timeout expired
     */
    Object getErrorResponse(long timeout,
                            TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Waits until the future is fulfilled and returns the {@code ProcessingContext} payload.
     *
     * @return the {@code ProcessingContext} of the message
     * @throws CancellationException if the future was cancelled
     * @throws InterruptedException  if the waiting {@link Thread} is interrupted
     * @throws ExecutionException    if the future was fulfilled with an exception
     */
    ProcessingContext<Object> getRaw() throws InterruptedException, ExecutionException;

    /**
     * Waits until the future is fulfilled and returns the {@code ProcessingContext} payload or the timeout expires.
     *
     * @param timeout the interval to wait
     * @param unit    the unit of the interval
     * @return the {@code ProcessingContext} of the message
     * @throws CancellationException if the future was cancelled
     * @throws InterruptedException  if the waiting {@link Thread} is interrupted
     * @throws ExecutionException    if the future was fulfilled with an exception
     * @throws TimeoutException      if the timeout expired
     */
    ProcessingContext<Object> getRaw(long timeout,
                                     TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Adds a {@code FollowUpAction}, that gets executed, once the Future is fulfilled.
     *
     * @param followUpAction the {@code FollowUpAction} to execute
     * @throws UnsupportedOperationException if one {@code FollowUpAction} has already been set
     * @throws CancellationException         if the future has already been cancelled
     */
    void then(FollowUpAction followUpAction);
}
