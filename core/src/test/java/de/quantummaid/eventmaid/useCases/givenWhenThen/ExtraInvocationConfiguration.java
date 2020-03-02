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

package de.quantummaid.eventmaid.useCases.givenWhenThen;

import de.quantummaid.eventmaid.useCases.building.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ExtraInvocationConfiguration {
    @Getter
    private List<DeAndSerializationDefinition<RequestSerializationStep1Builder>> requestSerializationDefinitions;
    @Getter
    private List<DeAndSerializationDefinition<RequestDeserializationStep1Builder>> requestDeserializationDefinitions;
    @Getter
    private List<DeAndSerializationDefinition<ResponseSerializationStep1Builder>> responseSerializationDefinitions;
    @Getter
    private List<DeAndSerializationDefinition<ExceptionSerializationStep1Builder>> exceptionsSerializationDefinitions;
    @Getter
    private List<DeAndSerializationDefinition<ResponseDeserializationStep1Builder>> responseDeserializationDefinitions;

    public static ExtraInvocationConfiguration extraInvocationConfiguration() {
        return new ExtraInvocationConfiguration();
    }

    public void addRequestSerializationDefinition(
            final DeAndSerializationDefinition<RequestSerializationStep1Builder> def) {
        requestSerializationDefinitions = addDefinitionToList(requestSerializationDefinitions, def);
    }

    public void addRequestDeserializationDefinitions(
            final DeAndSerializationDefinition<RequestDeserializationStep1Builder> def) {
        requestDeserializationDefinitions = addDefinitionToList(requestDeserializationDefinitions, def);
    }

    public void addResponseSerializationDefinitions(
            final DeAndSerializationDefinition<ResponseSerializationStep1Builder> def) {
        responseSerializationDefinitions = addDefinitionToList(responseSerializationDefinitions, def);
    }

    public void addExceptionsSerializationDefinitions(
            final DeAndSerializationDefinition<ExceptionSerializationStep1Builder> def) {
        exceptionsSerializationDefinitions = addDefinitionToList(exceptionsSerializationDefinitions, def);
    }

    public void addResponseDeserializationDefinitions(
            final DeAndSerializationDefinition<ResponseDeserializationStep1Builder> def) {
        responseDeserializationDefinitions = addDefinitionToList(responseDeserializationDefinitions, def);
    }

    private <T> List<T> addDefinitionToList(final List<T> list, final T definition) {
        if (list == null) {
            final ArrayList<T> newList = new ArrayList<>();
            newList.add(definition);
            return newList;
        } else {
            list.add(definition);
            return list;
        }
    }
}
