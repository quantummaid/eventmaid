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

package de.quantummaid.messagemaid.useCases;

import de.quantummaid.messagemaid.shared.exceptions.TestException;
import de.quantummaid.messagemaid.useCases.specialInvocations.Given;
import de.quantummaid.messagemaid.useCases.specialInvocations.SpecialInvocationUseCaseBuilder;
import de.quantummaid.messagemaid.useCases.specialInvocations.SpecialInvocationUseCaseInvoker;
import de.quantummaid.messagemaid.useCases.specialInvocations.SpecialInvocationValidator;
import org.junit.jupiter.api.Test;

public class UseCaseSpecialInvocationSpecs {

    @Test
    void testUseCaseAdapter_canHandleExceptionDuringInitialization() {
        final TestException expectedException = new TestException();
        Given.given(SpecialInvocationUseCaseBuilder.aUseCaseAdapter()
                .forAnUseCaseThrowingAnExceptionDuringInitialization(expectedException))
                .when(SpecialInvocationUseCaseInvoker.whenTheUSeCaseIsInvoked())
                .then(SpecialInvocationValidator.expectExecutionExceptionContaining(expectedException));
    }

    @Test
    void testUseCaseAdapter_canHandleExceptionDuringStaticInitializer() {
        Given.given(SpecialInvocationUseCaseBuilder.aUseCaseAdapter()
                .forAnUseCaseThrowingAnExceptionDuringStaticInitializer())
                .when(SpecialInvocationUseCaseInvoker.whenTheUSeCaseIsInvoked())
                .then(SpecialInvocationValidator.expectExecutionExceptionContainingExceptionClass(TestException.class));
    }
}
