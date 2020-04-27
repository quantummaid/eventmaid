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

package de.quantummaid.eventmaid.qcec.domainbus.building;

import de.quantummaid.eventmaid.subscribing.SubscriptionId;

import java.util.function.Consumer;

/**
 * Last step in defining the answer to a message. This step sets the consuming action.
 *
 * @param <T> the type of the message
 */
public interface AnswerActionBuilder<T> {

    /**
     * Defines how to react to the message.
     *
     * @param consumer the {@code consumer} to be applied on the message
     * @return the {@code SubscriptionId} of the answer
     */
    SubscriptionId using(Consumer<T> consumer);
}
