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

package de.quantummaid.eventmaid.processingcontext;

import de.quantummaid.eventmaid.internal.enforcing.StringValidator;
import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static lombok.AccessLevel.PRIVATE;

/**
 * An {@code EventType} is a representation of a communication on a {@link MessageBus}. All messages with the same
 * {@code EventType} belong the communication. {@link Subscriber Subscribers} can listen on a {@code EventType} to recieve all
 * relevant messages.
 *
 * @see <a href="https://github.com/quantummaid/eventmaid#processing-context">EventMaid Documentation</a>
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class EventType {
    private final String value;

    /**
     * Creates a new {@code EventType} from the given string.
     *
     * @param value the string representing the {@code EventType}
     * @return the newly created {@code EventType}
     */
    public static EventType eventTypeFromString(final String value) {
        final String cleaned = StringValidator.cleaned(value);
        return new EventType(cleaned);
    }

    /**
     * Creates a new {@code EventType} from the canonical name of the {@code Class}.
     *
     * @param aClass the {@code Class} to use the canonical name as {@code EventType}
     * @return the newly created {@code EventType}
     */
    public static EventType eventTypeFromClass(final Class<?> aClass) {
        final String name = aClass.getName();
        return eventTypeFromString(name);
    }

    /**
     * Creates a new {@code EventType} from the canonical name of the object's {@code Class}.
     *
     * @param object the object, from which to use the canonical {@code Class'} name as {@code EventType}
     * @return the newly created {@code EventType}
     */
    public static EventType eventTypeFromObjectClass(final Object object) {
        final Class<?> aClass = object.getClass();
        return eventTypeFromClass(aClass);
    }

    /**
     * Returns a string representation for the {@code EventType}.
     *
     * @return the {@code EventType's} string representation
     */
    public String stringValue() {
        return value;
    }
}
