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

package de.quantummaid.messagemaid.messageBus.internal.exception;

import de.quantummaid.messagemaid.identification.CorrelationId;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.messageBus.exception.MessageBusExceptionListener;
import de.quantummaid.messagemaid.processingContext.ProcessingContext;
import de.quantummaid.messagemaid.subscribing.SubscriptionId;

import java.util.List;

public interface ExceptionListenerHandler {

    SubscriptionId register(EventType eventType, MessageBusExceptionListener exceptionListener);

    SubscriptionId register(CorrelationId correlationId, MessageBusExceptionListener exceptionListener);

    List<MessageBusExceptionListener> listenerFor(ProcessingContext<?> processingContext);

    List<MessageBusExceptionListener> allListener();

    void unregister(SubscriptionId subscriptionId);
}
