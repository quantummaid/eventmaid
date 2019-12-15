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

package de.quantummaid.messagemaid.internal.pipe.givenWhenThen;

import de.quantummaid.messagemaid.shared.exceptions.TestException;
import de.quantummaid.messagemaid.internal.pipe.Pipe;
import de.quantummaid.messagemaid.internal.pipe.PipeBuilder;
import de.quantummaid.messagemaid.internal.pipe.PipeType;
import de.quantummaid.messagemaid.internal.pipe.config.PipeTestConfig;
import de.quantummaid.messagemaid.configuration.AsynchronousConfiguration;
import de.quantummaid.messagemaid.internal.pipe.error.PipeErrorHandler;
import de.quantummaid.messagemaid.shared.environment.TestEnvironment;
import de.quantummaid.messagemaid.shared.givenWhenThen.SetupAction;
import de.quantummaid.messagemaid.shared.testMessages.TestMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.EXCEPTION;
import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.RESULT;
import static de.quantummaid.messagemaid.shared.properties.SharedTestProperties.IS_ASYNCHRONOUS;
import static de.quantummaid.messagemaid.shared.utils.SubscriptionTestUtils.*;

public class PipeSetupBuilder {
    private final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();
    private final List<SetupAction<Pipe<TestMessage>>> setupActions = new LinkedList<>();
    private final PipeBuilder<TestMessage> pipeBuilder = PipeBuilder.aPipe();

    public static PipeSetupBuilder aConfiguredPipe(final PipeTestConfig testConfig) {
        return new PipeSetupBuilder()
                .configuredWith(testConfig);
    }

    public PipeSetupBuilder withoutASubscriber() {
        return this;
    }

    public PipeSetupBuilder withASingleSubscriber() {
        setupActions.add((t, testEnvironment) -> addASingleSubscriber(testActions(t), testEnvironment));
        return this;
    }

    public PipeSetupBuilder withSeveralSubscriber(final int numberOfReceivers) {
        setupActions.add((t, testEnvironment) -> addSeveralSubscriber(testActions(t), testEnvironment, numberOfReceivers));
        return this;
    }

    public PipeSetupBuilder withASubscriberThatBlocksWhenAccepting() {
        setupActions.add((t, testEnvironment) -> addASubscriberThatBlocksWhenAccepting(testActions(t), testEnvironment));
        return this;
    }

    public PipeSetupBuilder withSeveralDeliveryInterruptingSubscriber(final int numberOfReceivers) {
        setupActions.add((t, testEnvironment) -> {
            final PipeTestActions testActions = testActions(t);
            addSeveralDeliveryInterruptingSubscriber(testActions, testEnvironment, numberOfReceivers);
        });
        return this;
    }

    public PipeSetupBuilder withACustomErrorHandler() {
        pipeBuilder.withErrorHandler(errorHandler(e -> testEnvironment.setProperty(RESULT, e)));
        return this;
    }

    public PipeSetupBuilder withACustomErrorHandlerThatSuppressesException() {
        pipeBuilder.withErrorHandler(errorHandler(e -> testEnvironment.setProperty(EXCEPTION, e), TestException.class));
        return this;
    }

    public PipeSetupBuilder causingErrorsWhenDelivering() {
        pipeBuilder.withErrorHandler(errorHandler(e -> {
        }));
        return this;
    }

    private PipeSetupBuilder configuredWith(final PipeTestConfig testConfig) {
        final PipeType pipeType = testConfig.getPipeType();
        final AsynchronousConfiguration asynchronousConfiguration = testConfig.getAsynchronousConfiguration();
        pipeBuilder.ofType(pipeType)
                .withAsynchronousConfiguration(asynchronousConfiguration);
        final boolean isAsynchronous = testConfig.isAsynchronous();
        testEnvironment.setPropertyIfNotSet(IS_ASYNCHRONOUS, isAsynchronous);
        return this;
    }

    public PipeSetup build() {
        final Pipe<TestMessage> pipe = pipeBuilder.build();
        return PipeSetup.setup(pipe, testEnvironment, setupActions);
    }

    private PipeTestActions testActions(final Pipe<TestMessage> pipe) {
        return PipeTestActions.pipeTestActions(pipe);
    }

    private PipeErrorHandler<TestMessage> errorHandler(final Consumer<Exception> exceptionHandlerForNotIgnoredExceptions,
                                                       final Class<?>... ignoredExceptionsClasses) {
        return PipeTestErrorHandler.pipeTestErrorHandler(exceptionHandlerForNotIgnoredExceptions, testEnvironment, ignoredExceptionsClasses);
    }

}
