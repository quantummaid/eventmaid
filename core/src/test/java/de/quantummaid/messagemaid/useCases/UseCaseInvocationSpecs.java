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

package de.quantummaid.messagemaid.useCases;

import de.quantummaid.messagemaid.useCases.shared.UseCaseInvocationConfiguration;
import de.quantummaid.messagemaid.useCases.givenWhenThen.Given;
import de.quantummaid.messagemaid.useCases.givenWhenThen.UseCaseInvocationActionBuilder;
import de.quantummaid.messagemaid.useCases.givenWhenThen.UseCaseInvocationSetupBuilder;
import de.quantummaid.messagemaid.useCases.givenWhenThen.UseCaseInvocationValidationBuilder;
import de.quantummaid.messagemaid.useCases.building.*;
import org.junit.jupiter.api.Test;

import static de.quantummaid.messagemaid.useCases.givenWhenThen.Given.given;
import static de.quantummaid.messagemaid.useCases.givenWhenThen.UseCaseInvocationActionBuilder.*;
import static de.quantummaid.messagemaid.useCases.givenWhenThen.UseCaseInvocationSetupBuilder.*;
import static de.quantummaid.messagemaid.useCases.givenWhenThen.UseCaseInvocationValidationBuilder.*;


public interface UseCaseInvocationSpecs {

    //UseCaseAdapter + UseCaseBus
    @Test
    default void testUseCaseAdapter_canInvokeUseCaseUsingTheAutomaticMethod(final UseCaseInvocationConfiguration configuration) {
        given(aUseCaseAdapterFor(configuration)
                .invokedUsingTheSingleUseCaseMethod())
                .when(theAssociatedEventIsSend())
                .then(expectTheUseCaseToBeInvokedOnce());
    }

    @Test
    default void testUseCaseAdapter_explicitMappingCanBeDefined(final UseCaseInvocationConfiguration configuration) {
        given(aUseCaseAdapterFor(configuration)
                .invokingTheUseCaseUsingTheDefinedMapping())
                .when(theAssociatedEventIsSend())
                .then(expectTheUseCaseToBeInvokedOnce());
    }

    @Test
    default void testUseCaseAdapter_canUseCustomInstantiation(final UseCaseInvocationConfiguration configuration) {
        given(aUseCaseAdapterFor(configuration)
                .invokedUsingTheSingleUseCaseMethodButACustomInstantiationMechanism())
                .when(theAssociatedEventIsSend())
                .then(expectTheUseCaseToBeInvokedOnce());
    }

    //errors
    @Test
    default void testUseCaseAdapter_failsForMissingRequestSerializationMapping(final UseCaseInvocationConfiguration configuration) {
        given(aUseCaseAdapterFor(configuration)
                .invokingTheUseCaseUsingAMissingRequestSerializationDefinition())
                .when(anEventWithMissingMappingIsSend())
                .then(expectAnExecutionExceptionCauseByExceptionOfType(MissingRequestSerializationException.class));
    }

    @Test
    default void testUseCaseAdapter_failsForMissingRequestDeserializationMapping(final UseCaseInvocationConfiguration configuration) {
        given(aUseCaseAdapterFor(configuration)
                .invokingTheUseCaseUsingAMissingParameterDeserializationDefinition())
                .when(anEventWithMissingMappingIsSend())
                .then(expectAnErrorPayloadOfType(MissingRequestDeserializationException.class));
    }

    @Test
    default void testUseCaseAdapter_failsForMissingResponseSerializationMapping(final UseCaseInvocationConfiguration configuration) {
        given(aUseCaseAdapterFor(configuration)
                .invokingTheUseCaseUsingAMissingResponseSerializationDefinition())
                .when(anEventWithMissingMappingIsSend())
                .then(expectAnErrorPayloadOfType(MissingResponseSerializationException.class));
    }

    @Test
    default void testUseCaseAdapter_failsForMissingExceptionSerializationMapping(final UseCaseInvocationConfiguration configuration) {
        given(aUseCaseAdapterFor(configuration)
                .throwingAnExceptionWithoutMappingWhenInvokingTheUseCase())
                .when(anEventWithMissingMappingIsSend())
                .then(expectAnErrorPayloadOfType(MissingExceptionSerializationException.class));
    }

    @Test
    default void testUseCaseAdapter_failsForMissingResponseDeserializationMapping(final UseCaseInvocationConfiguration configuration) {
        given(aUseCaseAdapterFor(configuration)
                .invokingTheUseCaseUsingAMissingResponseDeserializationDefinition())
                .when(anEventWithMissingMappingIsSend())
                .then(expectAnExecutionExceptionCauseByExceptionOfType(MissingResponseDeserializationException.class));
    }

    //MessageFunction + directly using MessageBus
    @Test
    default void testUseCaseAdapter_canBeUsedInCombinationWithAMessageFunction(final UseCaseInvocationConfiguration configuration) {
        given(aUseCaseAdapterFor(configuration)
                .invokedUsingTheSingleUseCaseMethod())
                .when(theRequestIsExecutedUsingAMessageFunction())
                .then(expectTheResponseToBeReceivedByTheMessageFunction());
    }

    @Test
    default void testUseCaseAdapter_canAMessageFunctionAndACustomMapping(final UseCaseInvocationConfiguration configuration) {
        given(aUseCaseAdapterFor(configuration)
                .invokingTheUseCaseUsingTheDefinedMapping())
                .when(theRequestIsExecutedUsingAMessageFunction())
                .then(expectTheResponseToBeReceivedByTheMessageFunction());
    }


}
