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

package de.quantummaid.messagemaid.useCases.shared;

import de.quantummaid.messagemaid.useCases.givenWhenThen.DeAndSerializationDefinition;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.useCases.building.*;

public interface UseCaseInvocationConfiguration {
    Class<?> getUseCaseClass();

    EventType getEventTypeUseCaseIsRegisteredFor();

    DeAndSerializationDefinition<RequestSerializationStep1Builder> getRequestSerializationDefinitions();

    DeAndSerializationDefinition<RequestDeserializationStep1Builder> getRequestDeserializationDefinitions();

    DeAndSerializationDefinition<ResponseSerializationStep1Builder> getResponseSerializationDefinitions();

    DeAndSerializationDefinition<ExceptionSerializationStep1Builder> getExceptionsSerializationDefinitions();

    DeAndSerializationDefinition<ResponseDeserializationStep1Builder> getResponseDeserializationDefinitions();

    RequestExpectedResultTuple createRequestExpectedResultTuple();

    RequestExpectedResultTuple createSerializedRequestExpectedResultTuple();

    InstantiationBuilder applyCustomUseCaseMethodCallingConfiguration(Step3Builder<?> step3Builder);

    RequestSerializationStep1Builder applyCustomUseCaseInstantiationConfiguration(InstantiationBuilder instantiationBuilder);

    default void applyParameterInjection(final FinalStepBuilder finalStepBuilder) {
        //do nothing
    }
}
