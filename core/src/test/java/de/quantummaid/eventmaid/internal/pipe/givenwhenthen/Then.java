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

package de.quantummaid.eventmaid.internal.pipe.givenwhenthen;

import de.quantummaid.eventmaid.internal.pipe.Pipe;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.givenwhenthen.SetupAction;
import de.quantummaid.eventmaid.shared.givenwhenthen.TestAction;
import de.quantummaid.eventmaid.shared.givenwhenthen.TestValidation;
import de.quantummaid.eventmaid.shared.testmessages.TestMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.Semaphore;

import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.EXCEPTION;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.SUT;
import static de.quantummaid.eventmaid.shared.properties.SharedTestProperties.EXECUTION_END_SEMAPHORE;
import static java.util.concurrent.TimeUnit.SECONDS;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Then {
    private final PipeSetupBuilder setupBuilder;
    private final PipeActionBuilder actionBuilder;

    public void then(final PipeValidationBuilder testValidationBuilder) {
        final PipeSetup setup = buildSetup(setupBuilder);

        final TestEnvironment testEnvironment = setup.getTestEnvironment();
        final Pipe<TestMessage> sut = setup.getSut();
        executeTestAction(actionBuilder, sut, testEnvironment);

        final TestValidation validation = testValidationBuilder.build();
        validation.validate(testEnvironment);
        closeSut(sut);
    }

    private PipeSetup buildSetup(final PipeSetupBuilder setupBuilder) {
        final PipeSetup setup = setupBuilder.build();
        final TestEnvironment testEnvironment = setup.getTestEnvironment();
        final Pipe<TestMessage> sut = setup.getSut();
        final List<SetupAction<Pipe<TestMessage>>> setupActions = setup.getSetupActions();
        try {
            setupActions.forEach(setupAction -> setupAction.execute(sut, testEnvironment));
        } catch (final Exception e) {
            testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
        }
        testEnvironment.setPropertyIfNotSet(SUT, sut);
        return setup;
    }

    private void executeTestAction(final PipeActionBuilder actionBuilder,
                                   final Pipe<TestMessage> sut,
                                   final TestEnvironment testEnvironment) {
        final List<TestAction<Pipe<TestMessage>>> actions = actionBuilder.build();
        try {
            for (final TestAction<Pipe<TestMessage>> testAction : actions) {
                testAction.execute(sut, testEnvironment);
            }
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        if (testEnvironment.has(EXECUTION_END_SEMAPHORE)) {
            final Semaphore blockingSemaphoreToReleaseAfterExecution = getExecutionEndSemaphore(testEnvironment);
            blockingSemaphoreToReleaseAfterExecution.release(1000);
        }

    }

    private Semaphore getExecutionEndSemaphore(final TestEnvironment testEnvironment) {
        return testEnvironment.getPropertyAsType(EXECUTION_END_SEMAPHORE, Semaphore.class);
    }

    private void closeSut(final Pipe<TestMessage> pipe) {
        final int timeout = 3;
        pipe.close(true);
        try {
            if (!pipe.awaitTermination(timeout, SECONDS)) {
                throw new RuntimeException("Pipe did shutdown within timeout interval.");
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
