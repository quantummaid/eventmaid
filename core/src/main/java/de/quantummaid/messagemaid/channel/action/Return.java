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

package de.quantummaid.messagemaid.channel.action;

import de.quantummaid.messagemaid.channel.ChannelProcessingFrame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

/**
 * The {@code Return} {@code Action} is used to return from a previously executed {@code Call} {@code Action}.
 *
 * @param <T> the type of messages of the {@code Channel}
 *
 * @see <a href="https://github.com/quantummaid/messagemaid#call-and-return">MessageMaid Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class Return<T> implements Action<T> {
    @Getter
    @Setter
    private ChannelProcessingFrame<T> relatedCallFrame;

    /**
     * Factory method to create a new {@code Return} {@code Action}.
     *
     * @param <T> the type of messages of the {@code Channel}
     *
     * @return a new {@code Return} {@code Action}
     */
    public static <T> Return<T> aReturn() {
        return new Return<>();
    }
}
