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

package de.quantummaid.messagemaid.useCases.specialInvocations;

import de.quantummaid.messagemaid.configuration.AsynchronousConfiguration;
import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.messageBus.givenWhenThen.MessageBusTestExceptionHandler;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.shared.environment.TestEnvironment;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.UseCaseInvocationBuilder;
import de.quantummaid.messagemaid.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import de.quantummaid.messagemaid.useCases.useCaseBus.UseCaseBus;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.messagemaid.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static de.quantummaid.messagemaid.messageBus.MessageBusBuilder.aMessageBus;
import static de.quantummaid.messagemaid.messageBus.MessageBusType.ASYNCHRONOUS;
import static de.quantummaid.messagemaid.processingContext.EventType.eventTypeFromString;
import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.*;
import static java.util.Collections.emptyMap;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SpecialInvocationUseCaseBuilder {
    private final TestEnvironment testEnvironment;

    public static SpecialInvocationUseCaseBuilder aUseCaseAdapter() {
        final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();
        return new SpecialInvocationUseCaseBuilder(testEnvironment);
    }

    public SpecialInvocationUseCaseBuilder forAnUseCaseThrowingAnExceptionDuringInitialization(
            final RuntimeException exceptionToThrow) {
        final MessageBus messageBus = asynchronousMessageBus();
        final EventType type = eventTypeFromString("Test");
        final UseCaseBus useCaseBus = UseCaseInvocationBuilder.anUseCaseAdapter()
                .invokingUseCase(ExceptionDuringInitializationUseCase.class).forType(type).callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsing(new UseCaseInstantiator() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> T instantiate(final Class<T> type) {
                        return (T) ExceptionDuringInitializationUseCase.init(exceptionToThrow);
                    }
                })
                .serializingUseCaseRequestOntoTheBusOfTypeVoid().using(object -> emptyMap())
                .throwingAnExceptionByDefaultIfNoRequestSerializationCanBeApplied()
                .throwAnExceptionByDefaultIfNoUseCaseRequestDeserializationCanBeApplied()
                .serializingResponseObjectsOfTypeVoid().using(object -> emptyMap())
                .throwingAnExceptionByDefaultIfNoResponseSerializationCanBeApplied()
                .respondingWithAWrappingMissingExceptionSerializationExceptionByDefault()
                .throwAnExceptionByDefaultIfNoResponseDeserializationCanBeApplied()
                .build(messageBus);
        testEnvironment.setPropertyIfNotSet(MOCK, messageBus);
        testEnvironment.setPropertyIfNotSet(SUT, useCaseBus);
        testEnvironment.setPropertyIfNotSet(TEST_OBJECT, type);
        return this;
    }

    public SpecialInvocationUseCaseBuilder forAnUseCaseThrowingAnExceptionDuringStaticInitializer() {
        final MessageBus messageBus = asynchronousMessageBus();
        final EventType type = eventTypeFromString("Test");
        final UseCaseBus useCaseBus = UseCaseInvocationBuilder.anUseCaseAdapter()
                .invokingUseCase(ExceptionInStaticInitializerUseCase.class).forType(type).callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .serializingUseCaseRequestOntoTheBusOfTypeVoid().using(object -> emptyMap())
                .throwingAnExceptionByDefaultIfNoRequestSerializationCanBeApplied()
                .throwAnExceptionByDefaultIfNoUseCaseRequestDeserializationCanBeApplied()
                .serializingResponseObjectsOfTypeVoid().using(object -> emptyMap())
                .throwingAnExceptionByDefaultIfNoResponseSerializationCanBeApplied()
                .respondingWithAWrappingMissingExceptionSerializationExceptionByDefault()
                .throwAnExceptionByDefaultIfNoResponseDeserializationCanBeApplied()
                .build(messageBus);
        testEnvironment.setPropertyIfNotSet(MOCK, messageBus);
        testEnvironment.setPropertyIfNotSet(SUT, useCaseBus);
        testEnvironment.setPropertyIfNotSet(TEST_OBJECT, type);
        return this;
    }

    private MessageBus asynchronousMessageBus() {
        final int poolSize = 3;
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousConfiguration(poolSize);
        return aMessageBus()
                .forType(ASYNCHRONOUS)
                .withAsynchronousConfiguration(asynchronousConfiguration)
                .withExceptionHandler(MessageBusTestExceptionHandler.allExceptionIgnoringExceptionHandler())
                .build();
    }

    public TestEnvironment build() {
        return testEnvironment;
    }
}
