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

package de.quantummaid.messagemaid.messageBus.givenWhenThen;

import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.shared.environment.TestEnvironment;
import de.quantummaid.messagemaid.shared.givenWhenThen.SetupAction;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusSetup {
    private final MessageBus messageBus;
    private final TestEnvironment testEnvironment;
    private final List<SetupAction<MessageBus>> setupActions;

    public static MessageBusSetup setup(final MessageBus messageBus,
                                        final TestEnvironment testEnvironment,
                                        final List<SetupAction<MessageBus>> setupActions) {
        return new MessageBusSetup(messageBus, testEnvironment, setupActions);
    }

}
