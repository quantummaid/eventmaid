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

package de.quantummaid.eventmaid.internal.pipe.givenWhenThen;

import de.quantummaid.eventmaid.internal.pipe.Pipe;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.givenWhenThen.SetupAction;
import de.quantummaid.eventmaid.shared.testMessages.TestMessage;
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
public final class PipeSetup {
    private final Pipe<TestMessage> sut;
    private final TestEnvironment testEnvironment;
    private final List<SetupAction<Pipe<TestMessage>>> setupActions;

    public static PipeSetup setup(final Pipe<TestMessage> sut,
                                  final TestEnvironment testEnvironment,
                                  final List<SetupAction<Pipe<TestMessage>>> setupActions) {
        return new PipeSetup(sut, testEnvironment, setupActions);
    }

}