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

package de.quantummaid.eventmaid.useCases.shared;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseInvocationTestProperties {
    public static final String USE_CASE_BUS = "USE_CASE_BUS";
    public static final String MESSAGE_FUNCTION_USED = "MESSAGE_FUNCTION_USED";

    public static final String SIMULATE_MISSING_REQUEST_SERIALIZATION_PARAMETER = "SIMULATE_MISSING_REQUEST_SERIALIZATION_PARAM";
    public static final String SIMULATE_MISSING_REQUEST_DESERIALIZATION_PARAMETER = "SIMULATE_MISSING_REQUEST_DESERIALIZATION_P";
    public static final String SIMULATE_MISSING_RESPONSE_SERIALIZATION_PARAMETER = "SIM_MISSING_RESPONSE_SERIALIZATION_P";
    public static final String SIMULATE_MISSING_RESPONSE_EXCEPTION_SERIALIZATION_PARAM = "SIM_MISSING_RESP_EXC_SER_PARAM";
    public static final String SIMULATE_MISSING_RESPONSE_DESERIALIZATION_PARAMETER = "SIM_MISSING_RESPONSE_DESERIALIZATION_P";

    public static final String REQUEST_EXPECTED_RESULT_TUPLE = "REQUEST_EXPECTED_RESULT_TUPLE";
    public static final String EXPECTED_ERROR_PAYLOAD_CLASS = "EXPECTED_ERROR_PAYLOAD_CLASS";
}
