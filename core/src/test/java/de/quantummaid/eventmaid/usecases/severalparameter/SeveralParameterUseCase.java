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

import static de.quantummaid.eventmaid.usecases.severalparameter.SeveralParameterUseCaseResponse.severalParameterUseCaseResponse;

public class SeveralParameterUseCase {

    public SeveralParameterUseCaseResponse useCaseMethod(final SeveralParameterUseCaseRequest1 request1,
                                                         final SeveralParameterUseCaseRequest2 request2) {
        final int intParameter = request1.getIntParameter();
        final Boolean booleanParameter = request1.getBooleanParameter();
        final Object objectParameter = request2.getObjectParameter();
        final String stringParameter = request2.getStringParameter();
        return severalParameterUseCaseResponse(intParameter, booleanParameter, objectParameter, stringParameter);
    }
}