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

package de.quantummaid.eventmaid.qcec.constraining.givenWhenThen;

import de.quantummaid.eventmaid.shared.givenWhenThen.TestAction;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.givenWhenThen.TestValidation;
import de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Then {
    private final TestConstraintEnforcer testConstraintEnforcer;
    private final ConstraintActionBuilder constraintActionBuilder;

    public void then(final ConstraintValidationBuilder constraintValidationBuilder) {
        final TestEnvironment testEnvironment = testConstraintEnforcer.getEnvironment();
        final TestAction<TestConstraintEnforcer> testAction = constraintActionBuilder.build();
        try {
            testAction.execute(testConstraintEnforcer, testEnvironment);
        } catch (final Exception e) {
            testEnvironment.setProperty(TestEnvironmentProperty.EXCEPTION, e);
        }
        final TestValidation validation = constraintValidationBuilder.build();
        validation.validate(testEnvironment);
    }
}
