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

package de.quantummaid.messagemaid.internal.pipe;

import de.quantummaid.messagemaid.internal.autoclosable.NoErrorAutoClosable;
import de.quantummaid.messagemaid.subscribing.Subscriber;
import de.quantummaid.messagemaid.subscribing.SubscriptionId;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface Pipe<T> extends NoErrorAutoClosable {

    void send(T message);

    SubscriptionId subscribe(Subscriber<T> subscriber);

    SubscriptionId subscribe(Consumer<T> consumer);

    void unsubscribe(SubscriptionId subscriptionId);

    PipeStatusInformation<T> getStatusInformation();

    void close(boolean finishRemainingTasks);

    boolean isClosed();

    boolean awaitTermination(int timeout, TimeUnit timeUnit) throws InterruptedException;

}
