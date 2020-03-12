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

package de.quantummaid.eventmaid.qcec.constraining;

import de.quantummaid.eventmaid.qcec.constraining.givenWhenThen.TestConstraintEnforcer;
import de.quantummaid.eventmaid.qcec.constraining.givenWhenThen.ConstraintActionBuilder;
import de.quantummaid.eventmaid.qcec.constraining.givenWhenThen.ConstraintValidationBuilder;
import de.quantummaid.eventmaid.qcec.constraining.givenWhenThen.Given;
import org.junit.jupiter.api.Test;

public interface ConstraintEnforcingSpecs {

    @Test
    default void testConstraintEnforcer_constraintIsReceivedByAll(final TestConstraintEnforcer aConstraintEnforcer) {
        Given.given(aConstraintEnforcer
                .withSeveralSubscriber())
                .when(ConstraintActionBuilder.aPassingConstraintIsEnforced())
                .then(ConstraintValidationBuilder.expectTheConstraintToBeReceivedByAll());
    }

    @Test
    default void testConstraintEnforcer_constraintCanThrowException(final TestConstraintEnforcer aConstraintEnforcer) {
        Given.given(aConstraintEnforcer)
                .when(ConstraintActionBuilder.anExceptionCausingConstraintIsEnforced())
                .then(ConstraintValidationBuilder.expectTheExceptionToBeThrown());
    }

    @Test
    default void testConstraintEnforcer_canUnsubscribe(final TestConstraintEnforcer aConstraintEnforcer) {
        Given.given(aConstraintEnforcer)
                .when(ConstraintActionBuilder.anReceiverUnsubscribes())
                .then(ConstraintValidationBuilder.expectTheConstraintToBeReceivedByAllRemainingSubscribers());
    }
}
