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

package de.quantummaid.eventmaid.usecases;

import de.quantummaid.eventmaid.shared.exceptions.TestException;
import org.junit.jupiter.api.Test;

import static de.quantummaid.eventmaid.usecases.specialinvocations.Given.given;
import static de.quantummaid.eventmaid.usecases.specialinvocations.SpecialInvocationUseCaseBuilder.aUseCaseAdapter;
import static de.quantummaid.eventmaid.usecases.specialinvocations.SpecialInvocationUseCaseInvoker.whenBothUseCasesAreInvoked;
import static de.quantummaid.eventmaid.usecases.specialinvocations.SpecialInvocationUseCaseInvoker.whenTheUSeCaseIsInvoked;
import static de.quantummaid.eventmaid.usecases.specialinvocations.SpecialInvocationValidator.*;

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
                .withTwoUseCasesDefined())
                .when(whenBothUseCasesAreInvoked())
                .then(expectedBothUseCaseToBeInvoked());
    }

    @Test
    void testUseCaseAdapter_canHandleExceptionsInConstructorWhenUsingZeroArgsInstantiator() {
        final TestException expectedException = new TestException();
        given(aUseCaseAdapter()
                .forAnUseCaseThrowingAnExceptionDuringZeroArgsConstructorWhenUsing0ArgsInstantiator(expectedException))
                .when(whenTheUSeCaseIsInvoked())
                .then(expectExecutionExceptionContainingAnZeroArgumentsConstructorExceptionContaining(expectedException));
    }

    @Test
    void testUseCaseAdapter_zeroArgsInstantiatorFailsWhenClassCannotBeInstantiated() {
        given(aUseCaseAdapter()
                .forAnUseCaseThrowingAnExceptionDuringInstantiationWhenUsing0ArgsInstantiator())
                .when(whenTheUSeCaseIsInvoked())
                .then(expectExecutionExceptionContainingAn0ArgumentsConstructorExceptionContainingClass(
                        InstantiationException.class));
    }
}
