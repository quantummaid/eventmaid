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

package de.quantummaid.eventmaid.messagebus.givenwhenthen;

import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.givenwhenthen.SetupAction;
import de.quantummaid.eventmaid.shared.givenwhenthen.TestAction;
import de.quantummaid.eventmaid.shared.givenwhenthen.TestValidation;
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
    private final MessageBusSetupBuilder setupBuilder;
    private final MessageBusActionBuilder actionBuilder;

    public void then(final MessageBusValidationBuilder testValidationBuilder) {
        final MessageBusSetup setup = buildSetup(setupBuilder);

        final TestEnvironment testEnvironment = setup.getTestEnvironment();
        final MessageBus messageBus = setup.getMessageBus();
        executeTestAction(actionBuilder, messageBus, testEnvironment);

        final TestValidation validation = testValidationBuilder.build();
        validation.validate(testEnvironment);
        try {
            closeSut(messageBus);
        } catch (final InterruptedException e) {
            testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
        }
    }

    private MessageBusSetup buildSetup(final MessageBusSetupBuilder setupBuilder) {
        final MessageBusSetup setup = setupBuilder.build();
        final TestEnvironment testEnvironment = setup.getTestEnvironment();
        final MessageBus messageBus = setup.getMessageBus();
        testEnvironment.setProperty(SUT, messageBus);
        final List<SetupAction<MessageBus>> setupActions = setup.getSetupActions();
        try {
            for (final SetupAction<MessageBus> setupAction : setupActions) {
                setupAction.execute(messageBus, testEnvironment);
            }
        } catch (final Exception e) {
            testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
        }
        return setup;
    }

    private void executeTestAction(final MessageBusActionBuilder actionBuilder,
                                   final MessageBus messageBus,
                                   final TestEnvironment testEnvironment) {
        final List<TestAction<MessageBus>> actions = actionBuilder.build();
        try {
            for (final TestAction<MessageBus> testAction : actions) {
                testAction.execute(messageBus, testEnvironment);
            }
        } catch (final Exception e) {
            testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
        }
        if (testEnvironment.has(EXECUTION_END_SEMAPHORE)) {
            final Semaphore blockingSemaphoreToReleaseAfterExecution = getExecutionEndSemaphore(testEnvironment);
            blockingSemaphoreToReleaseAfterExecution.release(1000);
        }
    }

    private Semaphore getExecutionEndSemaphore(final TestEnvironment testEnvironment) {
        return testEnvironment.getPropertyAsType(EXECUTION_END_SEMAPHORE, Semaphore.class);
    }

    private void closeSut(final MessageBus messageBus) throws InterruptedException {
        final int timeout = 3;
        messageBus.close(true);
        if (!messageBus.awaitTermination(timeout, SECONDS)) {
            throw new RuntimeException("Messagebus did shutdown within timeout interval.");
        }
    }
}
