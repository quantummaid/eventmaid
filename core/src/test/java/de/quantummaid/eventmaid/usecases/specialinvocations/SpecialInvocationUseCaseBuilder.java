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

package de.quantummaid.eventmaid.usecases.specialinvocations;

import de.quantummaid.eventmaid.configuration.AsynchronousConfiguration;
import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.messagebus.givenwhenthen.MessageBusTestExceptionHandler;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.usecases.noparameter.NoParameterUseCase;
import de.quantummaid.eventmaid.usecases.singleeventparameter.SingleEventParameterUseCase;
import de.quantummaid.eventmaid.usecases.singleeventparameter.SingleParameterEvent;
import de.quantummaid.eventmaid.usecases.singleeventparameter.SingleParameterResponse;
import de.quantummaid.eventmaid.usecases.usecaseadapter.usecaseinstantiating.UseCaseInstantiator;
import de.quantummaid.eventmaid.usecases.usecasebus.UseCaseBus;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static de.quantummaid.eventmaid.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static de.quantummaid.eventmaid.messagebus.MessageBusBuilder.aMessageBus;
import static de.quantummaid.eventmaid.messagebus.MessageBusType.ASYNCHRONOUS;
import static de.quantummaid.eventmaid.processingcontext.EventType.eventTypeFromString;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.*;
import static de.quantummaid.eventmaid.usecases.singleeventparameter.SingleParameterEvent.singleParameterEvent;
import static de.quantummaid.eventmaid.usecases.singleeventparameter.SingleParameterResponse.singleParameterResponse;
import static de.quantummaid.eventmaid.usecases.specialinvocations.UseCaseTestInvocation.USE_CASE_INVOCATIONS;
import static de.quantummaid.eventmaid.usecases.usecaseadapter.UseCaseInvocationBuilder.anUseCaseAdapter;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.SECONDS;
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
        final UseCaseBus useCaseBus = anUseCaseAdapter()
                .invokingUseCase(ExceptionInConstructorUseCase.class).forType("Test").callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsing(new UseCaseInstantiator() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> T instantiate(final Class<T> type) {
                        return (T) ExceptionInConstructorUseCase.init(exceptionToThrow);
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
        testEnvironment.setPropertyIfNotSet(TEST_OBJECT, eventTypeFromString("Test"));
        return this;
    }

    public SpecialInvocationUseCaseBuilder forAnUseCaseThrowingAnExceptionDuringStaticInitializer() {
        final MessageBus messageBus = asynchronousMessageBus();
        final UseCaseBus useCaseBus = anUseCaseAdapter()
                .invokingUseCase(ExceptionInStaticInitializerUseCase.class).forType("Test").callingTheSingleUseCaseMethod()
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
        testEnvironment.setPropertyIfNotSet(TEST_OBJECT, eventTypeFromString("Test"));
        return this;
    }

    @SuppressWarnings("unchecked")
    public SpecialInvocationUseCaseBuilder withTwoUseCasesDefined() {
        final MessageBus messageBus = asynchronousMessageBus();
        final EventType type1 = eventTypeFromString("A");
        final EventType type2 = eventTypeFromString("B");
        final String mapProperty = "value";
        final UseCaseBus useCaseBus = anUseCaseAdapter()
                .invokingUseCase(NoParameterUseCase.class).forType(type1).callingTheSingleUseCaseMethod()
                .invokingUseCase(SingleEventParameterUseCase.class).forType(type2).callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()

                .serializingUseCaseRequestOntoTheBusOfTypeVoid().using(object -> emptyMap())
                .serializingUseCaseRequestOntoTheBusOfType(SingleParameterEvent.class).using(object -> {
                    final SingleParameterEvent singleParameterEvent = (SingleParameterEvent) object;
                    return Map.of(mapProperty, singleParameterEvent.getMessage());
                })
                .throwingAnExceptionByDefaultIfNoRequestSerializationCanBeApplied()

                .deserializingRequestsToUseCaseParametersOfType(SingleParameterEvent.class).using((targetType, map) -> {
                    return singleParameterEvent((String) ((Map<Object, Object>) map).get(mapProperty));
                })
                .throwAnExceptionByDefaultIfNoUseCaseRequestDeserializationCanBeApplied()

                .serializingUseCaseResponseBackOntoTheBusOfType(SingleParameterResponse.class).using(object -> {
                    return Map.of(mapProperty, object.getMessage());
                })
                .serializingUseCaseResponseBackOntoTheBusOfType(String.class).using(object -> {
                    return Map.of(mapProperty, object);
                })
                .throwingAnExceptionByDefaultIfNoResponseSerializationCanBeApplied()
                .respondingWithAWrappingMissingExceptionSerializationExceptionByDefault()

                .deserializingUseCaseResponsesOfType(SingleParameterResponse.class).using((targetType, map) -> {
                    return singleParameterResponse((String) ((Map<Object, Object>) map).get(mapProperty));
                })
                .deserializingUseCaseResponsesOfType(String.class).using((targetType, map) -> {
                    return (String) ((Map<Object, Object>) map).get(mapProperty);
                })
                .throwAnExceptionByDefaultIfNoResponseDeserializationCanBeApplied()
                .build(messageBus);
        testEnvironment.setPropertyIfNotSet(MOCK, messageBus);
        testEnvironment.setPropertyIfNotSet(SUT, useCaseBus);

        final List<UseCaseTestInvocation> eventTypes = List.of(
                () -> useCaseBus.invokeAndWait(type1, null, String.class, null, 1, SECONDS),
                () -> {
                    final SingleParameterEvent singleParameterEvent = singleParameterEvent("Test");
                    return useCaseBus.invokeAndWait(type2, singleParameterEvent, SingleParameterResponse.class, null, 1, SECONDS);
                });
        testEnvironment.setProperty(USE_CASE_INVOCATIONS, eventTypes);
        return this;
    }

    public SpecialInvocationUseCaseBuilder forAnUseCaseThrowingAnExceptionDuringZeroArgsConstructorWhenUsing0ArgsInstantiator(
            final RuntimeException exceptionToThrow) {
        ExceptionInZeroArgsConstructorUseCase.setExceptionToThrow(exceptionToThrow);
        final MessageBus messageBus = asynchronousMessageBus();
        final EventType type = eventTypeFromString("Test");
        final UseCaseBus useCaseBus = anUseCaseAdapter()
                .invokingUseCase(ExceptionInZeroArgsConstructorUseCase.class).forType(type).callingTheSingleUseCaseMethod()
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

    public SpecialInvocationUseCaseBuilder forAnUseCaseThrowingAnExceptionDuringInstantiationWhenUsing0ArgsInstantiator() {
        final MessageBus messageBus = asynchronousMessageBus();
        final EventType type = eventTypeFromString("Test");
        final UseCaseBus useCaseBus = anUseCaseAdapter()
                .invokingUseCase(AbstractUseCase.class).forType(type).callingTheSingleUseCaseMethod()
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
