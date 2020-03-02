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

package de.quantummaid.eventmaid.qcec.constraintEnforcing;

import de.quantummaid.eventmaid.messageBus.MessageBus;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * Factory to create a new {@code ConstraintEnforcer} object for the given {@code MessageBus}.
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class ConstraintEnforcerFactory {

    /**
     * Factory method to create a new {@code ConstraintEnforcer} object for the given {@code MessageBus}.
     *
     * @param messageBus the {@code MessageBus} to use
     * @return a new {@code ConstraintEnforcer}
     */
    public static ConstraintEnforcer aConstraintEnforcer(final MessageBus messageBus) {
        return new ConstraintEnforcerImpl(messageBus);
    }
}
