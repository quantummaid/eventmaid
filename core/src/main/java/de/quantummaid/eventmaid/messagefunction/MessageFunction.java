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

import de.quantummaid.eventmaid.internal.autoclosable.NoErrorAutoClosable;
import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.processingcontext.EventType;

/**
 * {@code MessageFunctions} simplify the execution of request-reply based communications over an asynchronous {@link MessageBus}.
 *
 * @see <a href="https://github.com/quantummaid/eventmaid#message-function">EventMaid Documentation</a>
 */
public interface MessageFunction extends NoErrorAutoClosable {

    /**
     * Sends the given request over the {@code MessageBus}.
     *
     * <p>The returned {@link ResponseFuture} fulfills, when a response is received or an exception during the transport of
     * request or the reply is thrown.</p>
     *
     * @param eventType the {@code EventType} of the request
     * @param request   the request to send
     * @return a {@code ResponseFuture} that can be queried for the result
     */
    ResponseFuture request(EventType eventType, Object request);

    /**
     * Closes the {@code MessageFunction}. This does not cancel any pending {@link ResponseFuture ResponseFutures}.
     */
    @Override
    void close();
}
