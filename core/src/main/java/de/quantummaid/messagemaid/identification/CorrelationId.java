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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.UUID;

/**
 * Unique identifier to match all messages, that are related.
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#mesagefunction">MessageMaid Documentation</a>
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CorrelationId {
    private final MessageId messageId;

    /**
     * Creates a new {@code CorrelationId} using the string representation of an {@link UUID}.
     *
     * @param value the string representation of the {@code UUID}
     * @return a new {@code CorrelationId}
     */
    public static CorrelationId fromString(final String value) {
        final MessageId messageId = MessageId.fromString(value);
        return new CorrelationId(messageId);
    }

    /**
     * Creates a {@code CorrelationId} matching the given {@link MessageId}.
     *
     * @param messageId the {@code MessageId} to match
     * @return a new {@code CorrelationId}
     */
    public static CorrelationId correlationIdFor(final MessageId messageId) {
        return new CorrelationId(messageId);
    }

    /**
     * Creates a new, randomly generated {@code CorrelationId}.
     *
     * @return the new, randomly generated {@code CorrelationId}
     */
    public static CorrelationId newUniqueCorrelationId() {
        final MessageId uniqueMessageId = MessageId.newUniqueMessageId();
        return new CorrelationId(uniqueMessageId);
    }

    /**
     * Checks, if the {@code CorrelationId} is related to the given {@link MessageId}.
     *
     * @param messageId the {@code MessageId} to check
     * @return {@code true} if the ids are related, {@code false} otherwise
     */
    public boolean matches(final MessageId messageId) {
        return this.messageId.equals(messageId);
    }

    /**
     * Returns the String value representing the {@code CorrelationId}.
     *
     * @return a representative string value
     */
    public String stringValue() {
        return this.messageId.stringValue();
    }
}
