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

package de.quantummaid.eventmaid.usecases.severalparameter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class SeveralParameterUseCaseCombinedRequest {
    @Getter
    private final int intParameter;
    @Getter
    private final Boolean booleanParameter;
    @Getter
    private final Object objectParameter;
    @Getter
    private final String stringParameter;

    public static SeveralParameterUseCaseCombinedRequest severalParameterUseCaseCombinedRequest(final int intParameter,
                                                                                                final boolean booleanParameter,
                                                                                                final Object objectParameter,
                                                                                                final String stringParameter) {
        return new SeveralParameterUseCaseCombinedRequest(intParameter, booleanParameter, objectParameter, stringParameter);
    }

}
