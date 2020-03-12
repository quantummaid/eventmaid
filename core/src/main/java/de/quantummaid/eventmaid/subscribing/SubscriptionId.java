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

package de.quantummaid.eventmaid.subscribing;

import de.quantummaid.eventmaid.internal.enforcing.InvalidInputException;
import de.quantummaid.eventmaid.internal.enforcing.StringValidator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.UUID;

/**
 * Each {@code Subscriber} possesses a unique {@code SubscriptionId}, which acts as its identity.
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubscriptionId {

    private final UUID value;

    /**
     * Creates a {@code SubscriptionId} from the given string value. The string has to be a valid {@code UUID}.
     *
     * @param value string form of an UUID
     * @return new {@code SubscriptionId}
     */
    public static SubscriptionId fromString(final String value) {
        final String cleaned = StringValidator.cleaned(value);
        try {
            return new SubscriptionId(UUID.fromString(cleaned));
        } catch (final IllegalArgumentException e) {
            throw new InvalidInputException("Must be a valid uuid.");
        }
    }

    /**
     * Creates a new randomly generated {@code SubscriptionId}.
     *
     * @return randomly generated {@code SubscriptionId}
     */
    public static SubscriptionId newUniqueId() {
        return new SubscriptionId(UUID.randomUUID());
    }

    public String stringValue() {
        return this.value.toString();
    }
}
