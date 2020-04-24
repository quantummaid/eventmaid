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

package de.quantummaid.eventmaid.usecases.payloadanderrorpayload;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static lombok.AccessLevel.PRIVATE;

/**
 * Messages can have both normal and error payload. This class combines these two objects into a single one.
 *
 * @param <P> the type of the normal payload
 * @param <E> the type of the error payload
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class PayloadAndErrorPayload<P, E> {
    @Getter
    private final P payload;
    @Getter
    private final E errorPayload;

    /**
     * Factory method to create a new {@code PayloadAndErrorPayload} object for the given payloads.
     *
     * @param payload      the normal payload
     * @param errorPayload the error payload
     * @param <P>          the type of the normal payload
     * @param <E>          the type of the error payload
     * @return the newly created {@code PayloadAndErrorPayload} object
     */
    public static <P, E> PayloadAndErrorPayload<P, E> payloadAndErrorPayload(final P payload, final E errorPayload) {
        return new PayloadAndErrorPayload<>(payload, errorPayload);
    }
}
