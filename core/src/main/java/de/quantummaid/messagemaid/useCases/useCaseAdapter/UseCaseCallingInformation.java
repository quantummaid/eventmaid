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

package de.quantummaid.messagemaid.useCases.useCaseAdapter;

import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.usecaseCalling.Caller;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static de.quantummaid.messagemaid.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
final class UseCaseCallingInformation<U> {
    @Getter
    private final Class<U> useCaseClass;
    @Getter
    private final EventType eventType;
    @Getter
    private final Caller<U> caller;

    static <U> UseCaseCallingInformation<U> useCaseInvocationInformation(
            final Class<U> useCaseClass,
            final EventType eventType,
            final Caller<U> caller) {
        ensureNotNull(useCaseClass, "useCaseClass");
        ensureNotNull(eventType, "eventType");
        ensureNotNull(caller, "caller");
        return new UseCaseCallingInformation<>(useCaseClass, eventType, caller);
    }
}
