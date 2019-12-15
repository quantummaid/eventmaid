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

package de.quantummaid.messagemaid.identification;

import de.quantummaid.messagemaid.internal.enforcing.InvalidInputException;
import de.quantummaid.messagemaid.internal.enforcing.StringValidator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * Unique identifier for a messages.
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageId {
    private final UUID value;

    /**
     * Creates a new {@code MessageId} using the string representation of an {@link UUID}.
     *
     * @param value the string representation of the {@code UUID}
     * @return a new {@code MessageId}
     */
    public static MessageId fromString(final String value) {
        final String cleaned = StringValidator.cleaned(value);
        try {
            return new MessageId(UUID.fromString(cleaned));
        } catch (final IllegalArgumentException e) {
            throw new InvalidInputException("Must be a valid uuid.");
        }
    }

    /**
     * Creates a new, randomly generated {@code MessageId}.
     *
     * @return the new, randomly generated {@code MessageId}
     */
    public static MessageId newUniqueMessageId() {
        final UUID uuid = randomUUID();
        return new MessageId(uuid);
    }

    /**
     * Returns a String representing the {@code MessageId}.
     *
     * @return a representative string
     */
    public String stringValue() {
        return this.value.toString();
    }
}
