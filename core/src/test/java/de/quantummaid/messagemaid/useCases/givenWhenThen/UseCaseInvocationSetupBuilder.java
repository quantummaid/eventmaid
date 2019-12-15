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

package de.quantummaid.messagemaid.useCases.givenWhenThen;

import de.quantummaid.messagemaid.shared.exceptions.TestException;
import de.quantummaid.messagemaid.configuration.AsynchronousConfiguration;
import de.quantummaid.messagemaid.mapping.MissingExceptionMappingExceptionMapifier;
import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.messageBus.MessageBusBuilder;
import de.quantummaid.messagemaid.shared.environment.TestEnvironment;
import de.quantummaid.messagemaid.useCases.shared.UseCaseInvocationConfiguration;
import de.quantummaid.messagemaid.useCases.building.*;

import java.util.Map;

import static de.quantummaid.messagemaid.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static de.quantummaid.messagemaid.messageBus.MessageBusBuilder.aMessageBus;
import static de.quantummaid.messagemaid.messageBus.MessageBusType.ASYNCHRONOUS;
import static de.quantummaid.messagemaid.shared.environment.TestEnvironment.emptyTestEnvironment;
import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.MOCK;
import static de.quantummaid.messagemaid.useCases.building.MissingExceptionSerializationException.missingExceptionSerializationException;
import static de.quantummaid.messagemaid.useCases.building.MissingRequestDeserializationException.missingDeserializationException;
import static de.quantummaid.messagemaid.useCases.building.MissingResponseSerializationException.missingResponseSerializationException;
import static de.quantummaid.messagemaid.useCases.givenWhenThen.ExtraInvocationConfiguration.extraInvocationConfiguration;
import static de.quantummaid.messagemaid.useCases.shared.UseCaseInvocationTestProperties.*;

public final class UseCaseInvocationSetupBuilder {
    private final TestEnvironment testEnvironment;
    private final UseCaseInvocationConfiguration invocationConfiguration;
    private final ExtraInvocationConfiguration extraInvocationConfiguration;
    private UseCaseConfigurationStep<Step3Builder<?>, RequestSerializationStep1Builder> useCaseMethodCallingFunction;
    private MessageBusBuilder messageBusBuilder;

    private UseCaseInvocationSetupBuilder(final UseCaseInvocationConfiguration invocationConfiguration) {
        this.testEnvironment = emptyTestEnvironment();
        this.invocationConfiguration = invocationConfiguration;
        this.extraInvocationConfiguration = extraInvocationConfiguration();
        this.messageBusBuilder = aMessageBus();
    }

    public static UseCaseInvocationSetupBuilder aUseCaseAdapterFor(final UseCaseInvocationConfiguration invocationConfiguration) {

        return new UseCaseInvocationSetupBuilder(invocationConfiguration);
    }

    public UseCaseInvocationSetupBuilder invokedUsingTheSingleUseCaseMethod() {
        useCaseMethodCallingFunction = (configuration, step3Builder) -> step3Builder.callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor();
        return this;
    }

    public UseCaseInvocationSetupBuilder invokingTheUseCaseUsingTheDefinedMapping() {
        useCaseMethodCallingFunction = (configuration, step3Builder) -> configuration
                .applyCustomUseCaseMethodCallingConfiguration(step3Builder)
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor();
        return this;
    }

    public UseCaseInvocationSetupBuilder invokedUsingTheSingleUseCaseMethodButACustomInstantiationMechanism() {
        useCaseMethodCallingFunction = (configuration, step3Builder) -> {
            final InstantiationBuilder instantiationBuilder = step3Builder.callingTheSingleUseCaseMethod();
            return configuration.applyCustomUseCaseInstantiationConfiguration(instantiationBuilder);
        };
        return this;
    }

    public UseCaseInvocationSetupBuilder invokingTheUseCaseUsingAMissingRequestSerializationDefinition() {
        useCaseMethodCallingFunction = (configuration, step3Builder) -> step3Builder.callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor();
        testEnvironment.setPropertyIfNotSet(SIMULATE_MISSING_REQUEST_SERIALIZATION_PARAMETER, true);
        return this;
    }

    public UseCaseInvocationSetupBuilder invokingTheUseCaseUsingAMissingParameterDeserializationDefinition() {
        useCaseMethodCallingFunction = (configuration, step3Builder) -> step3Builder.callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor();

        testEnvironment.setPropertyIfNotSet(SIMULATE_MISSING_REQUEST_DESERIALIZATION_PARAMETER, true);

        testEnvironment.setPropertyIfNotSet(EXPECTED_ERROR_PAYLOAD_CLASS, MissingRequestDeserializationException.class);
        final String exceptionMapKey = "EXCEPTION";
        extraInvocationConfiguration.addExceptionsSerializationDefinitions(exceptionSerializationStep1Builder -> {
            exceptionSerializationStep1Builder.serializingExceptionsOfType(MissingRequestDeserializationException.class)
                    .using(e -> Map.of(exceptionMapKey, e.getMessage()));
        });
        extraInvocationConfiguration.addResponseDeserializationDefinitions(responseDeserializationStep1Builder -> {
            responseDeserializationStep1Builder.deserializingUseCaseResponsesOfType(MissingRequestDeserializationException.class)
                    .using((targetType, map) -> missingDeserializationException((String) map.get(exceptionMapKey)));
        });
        return this;
    }

    public UseCaseInvocationSetupBuilder invokingTheUseCaseUsingAMissingResponseSerializationDefinition() {
        useCaseMethodCallingFunction = (configuration, step3Builder) -> step3Builder.callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor();
        testEnvironment.setPropertyIfNotSet(SIMULATE_MISSING_RESPONSE_SERIALIZATION_PARAMETER, true);

        testEnvironment.setPropertyIfNotSet(EXPECTED_ERROR_PAYLOAD_CLASS, MissingResponseSerializationException.class);
        final String exceptionMapKey = "EXCEPTION";
        extraInvocationConfiguration.addExceptionsSerializationDefinitions(exceptionSerializationStep1Builder -> {
            exceptionSerializationStep1Builder.serializingExceptionsOfType(MissingResponseSerializationException.class)
                    .using(e -> Map.of(exceptionMapKey, e.getMessage()));
        });
        extraInvocationConfiguration.addResponseDeserializationDefinitions(responseDeserializationStep1Builder -> {
            responseDeserializationStep1Builder.deserializingUseCaseResponsesOfType(MissingResponseSerializationException.class)
                    .using((targetType, map) -> missingResponseSerializationException((String) map.get(exceptionMapKey)));
        });
        return this;
    }

    public UseCaseInvocationSetupBuilder throwingAnExceptionWithoutMappingWhenInvokingTheUseCase() {
        useCaseMethodCallingFunction = (configuration, step3Builder) -> step3Builder
                .callingBy((useCase, event, callingContext) -> {
                    throw new TestException();
                }).obtainingUseCaseInstancesUsingTheZeroArgumentConstructor();

        testEnvironment.setPropertyIfNotSet(EXPECTED_ERROR_PAYLOAD_CLASS, MissingExceptionSerializationException.class);
        extraInvocationConfiguration.addResponseDeserializationDefinitions(responseDeserializationStep1Builder -> {
            responseDeserializationStep1Builder.deserializingUseCaseResponsesOfType(MissingExceptionSerializationException.class)
                    .using((targetType, map) -> {
                        final String message =
                                (String) map.get(MissingExceptionMappingExceptionMapifier.DEFAULT_EXCEPTION_MAPIFIER_KEY);
                        return missingExceptionSerializationException(message);
                    });
        });
        return this;
    }

    public UseCaseInvocationSetupBuilder invokingTheUseCaseUsingAMissingResponseDeserializationDefinition() {
        useCaseMethodCallingFunction = (configuration, step3Builder) -> step3Builder.callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor();
        testEnvironment.setPropertyIfNotSet(SIMULATE_MISSING_RESPONSE_DESERIALIZATION_PARAMETER, true);
        return this;
    }

    public UseCaseInvocationSetup build() {
        final MessageBus messageBus = createMessageBus();
        testEnvironment.setProperty(MOCK, messageBus);
        return new UseCaseInvocationSetup(testEnvironment, messageBus, invocationConfiguration, extraInvocationConfiguration,
                useCaseMethodCallingFunction);
    }

    private MessageBus createMessageBus() {
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousConfiguration(3);
        this.messageBusBuilder = aMessageBus();
        return messageBusBuilder.forType(ASYNCHRONOUS)
                .withAsynchronousConfiguration(asynchronousConfiguration)
                .build();
    }

}
