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

package de.quantummaid.eventmaid.filtering;

import de.quantummaid.eventmaid.channel.Channel;

/**
 * Each filter can decide if the message continues its propagation with {@code pass} or if the delivery of the message stops with
 * {@code block}.
 *
 * @param <T> the type of messages of the {@link Channel}
 * @see <a href="https://github.com/quantummaid/eventmaid#adding-filter-to-channel">EventMaid Documentation</a>
 */
public interface FilterActions<T> {

    void block(T message);

    void pass(T message);
}
