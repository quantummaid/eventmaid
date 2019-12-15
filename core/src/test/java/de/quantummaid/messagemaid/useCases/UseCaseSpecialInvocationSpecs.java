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
import org.junit.jupiter.api.Test;

import static de.quantummaid.messagemaid.useCases.specialInvocations.Given.given;
import static de.quantummaid.messagemaid.useCases.specialInvocations.SpecialInvocationUseCaseBuilder.aUseCaseAdapter;
import static de.quantummaid.messagemaid.useCases.specialInvocations.SpecialInvocationUseCaseInvoker.whenBothUseCasesAreInvoked;
import static de.quantummaid.messagemaid.useCases.specialInvocations.SpecialInvocationUseCaseInvoker.whenTheUSeCaseIsInvoked;
import static de.quantummaid.messagemaid.useCases.specialInvocations.SpecialInvocationValidator.*;

public class UseCaseSpecialInvocationSpecs {

    @Test
    void testUseCaseAdapter_canHandleExceptionDuringInitialization() {
        final TestException expectedException = new TestException();
        given(aUseCaseAdapter()
                .forAnUseCaseThrowingAnExceptionDuringInitialization(expectedException))
                .when(whenTheUSeCaseIsInvoked())
                .then(expectExecutionExceptionContaining(expectedException));
    }

    @Test
    void testUseCaseAdapter_canHandleExceptionDuringStaticInitializer() {
        given(aUseCaseAdapter()
                .forAnUseCaseThrowingAnExceptionDuringStaticInitializer())
                .when(whenTheUSeCaseIsInvoked())
                .then(expectExecutionExceptionContainingExceptionClass(TestException.class));
    }

    @Test
    void testUseCaseAdapter_canAcceptTwoUseCases() {
        given(aUseCaseAdapter()
                .withToUseCasesDefined())
                .when(whenBothUseCasesAreInvoked())
                .then(expectedBothUseCaseToBeInvoked());
    }
}
