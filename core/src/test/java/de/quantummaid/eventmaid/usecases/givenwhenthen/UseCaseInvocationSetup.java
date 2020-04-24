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

package de.quantummaid.eventmaid.usecases.givenwhenthen;

import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.usecases.building.*;
import de.quantummaid.eventmaid.usecases.shared.UseCaseInvocationConfiguration;
import de.quantummaid.eventmaid.usecases.usecasebus.UseCaseBus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static de.quantummaid.eventmaid.usecases.shared.UseCaseInvocationTestProperties.*;
import static de.quantummaid.eventmaid.usecases.usecaseadapter.UseCaseInvocationBuilder.anUseCaseAdapter;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
final class UseCaseInvocationSetup {
    @Getter
    private final TestEnvironment testEnvironment;
    @Getter
    private final MessageBus messageBus;
    @Getter
    private final UseCaseInvocationConfiguration invocationConfiguration;

    private final ExtraInvocationConfiguration extraInvocationConfiguration;

    private final UseCaseConfigurationStep<Step3Builder<?>, RequestSerializationStep1Builder> useCaseMethodCallingFunction;

    public UseCaseBus createUseCaseInvoker() {
        final Class<?> useCaseClass = invocationConfiguration.getUseCaseClass();
        final EventType eventType = invocationConfiguration.getEventTypeUseCaseIsRegisteredFor();
        final Step3Builder<?> step3Builder = anUseCaseAdapter().invokingUseCase(useCaseClass)
                .forType(eventType);
        final RequestSerializationStep1Builder requestSerializationB =
                useCaseMethodCallingFunction.apply(invocationConfiguration, step3Builder);

        final RequestDeserializationStep1Builder requestDeserializationB = defineRequestSerialization(requestSerializationB);
        final ResponseSerializationStep1Builder responseSerializationB = defineRequestDeserialization(requestDeserializationB);
        final ExceptionSerializationStep1Builder exSerializationB = defineResponseSerialization(responseSerializationB);
        final ResponseDeserializationStep1Builder responseDeserializationB = defineExceptionSerialization(exSerializationB);
        final FinalStepBuilder finalStepBuilder = defineResponseDeserialization(responseDeserializationB);

        invocationConfiguration.applyParameterInjection(finalStepBuilder);

        final UseCaseBus useCaseBus = finalStepBuilder.build(messageBus);
        return useCaseBus;
    }

    private RequestDeserializationStep1Builder defineRequestSerialization(
            final RequestSerializationStep1Builder requestSerializationB) {

        final boolean applySerializationMappings = !testEnvironment.has(SIMULATE_MISSING_REQUEST_SERIALIZATION_PARAMETER);
        if (applySerializationMappings) {
            final DeAndSerializationDefinition<RequestSerializationStep1Builder> requestSerializationDefinitions =
                    invocationConfiguration.getRequestSerializationDefinitions();
            requestSerializationDefinitions.accept(requestSerializationB);
        }

        final List<DeAndSerializationDefinition<RequestSerializationStep1Builder>> extraDefinitions =
                extraInvocationConfiguration.getRequestSerializationDefinitions();
        applyExtraDefinitions(extraDefinitions, requestSerializationB);

        return requestSerializationB.throwingAnExceptionByDefaultIfNoRequestSerializationCanBeApplied();
    }

    private ResponseSerializationStep1Builder defineRequestDeserialization(
            final RequestDeserializationStep1Builder requestDeserializationB) {

        final boolean applyDeserializationMappings = !testEnvironment.has(SIMULATE_MISSING_REQUEST_DESERIALIZATION_PARAMETER);
        if (applyDeserializationMappings) {
            final DeAndSerializationDefinition<RequestDeserializationStep1Builder> requestDeserializationDefinitions =
                    invocationConfiguration.getRequestDeserializationDefinitions();
            requestDeserializationDefinitions.accept(requestDeserializationB);
        }

        final List<DeAndSerializationDefinition<RequestDeserializationStep1Builder>> extraDefinitions =
                extraInvocationConfiguration.getRequestDeserializationDefinitions();
        applyExtraDefinitions(extraDefinitions, requestDeserializationB);

        return requestDeserializationB.throwAnExceptionByDefaultIfNoUseCaseRequestDeserializationCanBeApplied();
    }

    private ExceptionSerializationStep1Builder defineResponseSerialization(
            final ResponseSerializationStep1Builder responseSerializationBuilder) {

        final boolean applySerializationMappings = !testEnvironment.has(SIMULATE_MISSING_RESPONSE_SERIALIZATION_PARAMETER);
        if (applySerializationMappings) {
            final DeAndSerializationDefinition<ResponseSerializationStep1Builder> responseSerializationDefinitions =
                    invocationConfiguration.getResponseSerializationDefinitions();
            responseSerializationDefinitions.accept(responseSerializationBuilder);
        }

        final List<DeAndSerializationDefinition<ResponseSerializationStep1Builder>> extraDefinitions =
                extraInvocationConfiguration.getResponseSerializationDefinitions();
        applyExtraDefinitions(extraDefinitions, responseSerializationBuilder);

        return responseSerializationBuilder.throwingAnExceptionByDefaultIfNoResponseSerializationCanBeApplied();
    }

    private ResponseDeserializationStep1Builder defineExceptionSerialization(
            final ExceptionSerializationStep1Builder exceptionSerializationBuilder) {

        final boolean applySerializationMappings = !testEnvironment.has(SIMULATE_MISSING_RESPONSE_EXCEPTION_SERIALIZATION_PARAM);
        if (applySerializationMappings) {
            final DeAndSerializationDefinition<ExceptionSerializationStep1Builder> exceptionsSerializationDefinitions =
                    invocationConfiguration.getExceptionsSerializationDefinitions();
            exceptionsSerializationDefinitions.accept(exceptionSerializationBuilder);
        }

        final List<DeAndSerializationDefinition<ExceptionSerializationStep1Builder>> extraDefinitions =
                extraInvocationConfiguration.getExceptionsSerializationDefinitions();
        applyExtraDefinitions(extraDefinitions, exceptionSerializationBuilder);

        return exceptionSerializationBuilder.respondingWithAWrappingMissingExceptionSerializationExceptionByDefault();
    }

    private FinalStepBuilder defineResponseDeserialization(
            final ResponseDeserializationStep1Builder responseDeserializationBuilder) {

        final boolean applySerializationMappings = !testEnvironment.has(SIMULATE_MISSING_RESPONSE_DESERIALIZATION_PARAMETER);
        if (applySerializationMappings) {
            final DeAndSerializationDefinition<ResponseDeserializationStep1Builder> responseDeserializationDefinitions =
                    invocationConfiguration.getResponseDeserializationDefinitions();
            responseDeserializationDefinitions.accept(responseDeserializationBuilder);
        }

        final List<DeAndSerializationDefinition<ResponseDeserializationStep1Builder>> extraDefinitions =
                extraInvocationConfiguration.getResponseDeserializationDefinitions();
        applyExtraDefinitions(extraDefinitions, responseDeserializationBuilder);

        return responseDeserializationBuilder.throwAnExceptionByDefaultIfNoResponseDeserializationCanBeApplied();
    }

    private <T> void applyExtraDefinitions(final List<DeAndSerializationDefinition<T>> extraDefinitions, final T builder) {
        if (extraDefinitions != null) {
            extraDefinitions.forEach(def -> def.accept(builder));
        }
    }

}
