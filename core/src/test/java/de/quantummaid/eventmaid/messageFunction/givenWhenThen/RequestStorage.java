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

package de.quantummaid.eventmaid.messageFunction.givenWhenThen;

import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class RequestStorage<T> {
    private volatile ProcessingContext<T> request;

    public static <T> RequestStorage<T> requestStorage() {
        return new RequestStorage<>();
    }

    public synchronized void storeRequest(final ProcessingContext<T> request) {
        if (this.request == null) {
            this.request = request;
        } else {
            throw new IllegalStateException("Already stored a request.");
        }
    }

    public synchronized ProcessingContext<T> getRequest() {
        return request;
    }

    public synchronized boolean hasRequestStored() {
        return request != null;
    }
}
