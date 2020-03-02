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

package de.quantummaid.eventmaid.useCases.useCaseAdapter;

import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.useCases.useCaseBus.UseCaseBus;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.eventmaid.processingContext.EventType.eventTypeFromString;
import static lombok.AccessLevel.PRIVATE;

/**
 * The {@link EventType} to identify response from use cases invoked by the {@link UseCaseBus} or {@link UseCaseAdapter}.
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseInvokingResponseEventType {
    /**
     * The {@link EventType}, on which response from use cases invoked by the {@link UseCaseBus} or {@link UseCaseAdapter} are
     * send back.
     */
    public static final EventType USE_CASE_RESPONSE_EVENT_TYPE = eventTypeFromString("UseCaseResponse");
}
