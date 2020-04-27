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

package de.quantummaid.eventmaid.internal.enforcing;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringValidator {
    private static final int MAXIMUM_LENGTH = 512;
    private static final Pattern SEVERAL_SPACES = Pattern.compile("\\s{2,}");

    public static String cleaned(final String input) {
        final String value = Optional.ofNullable(input).orElse("");
        if (value.length() > MAXIMUM_LENGTH) {
            throw new InvalidInputException(String.format("Value is too long, max %s characters are allowed.", MAXIMUM_LENGTH));
        }
        final Matcher matcher = SEVERAL_SPACES.matcher(value);
        final String singleWhiteSpaced;
        if (matcher.matches()) {
            singleWhiteSpaced = matcher.replaceAll(" ");
        } else {
            singleWhiteSpaced = value;
        }
        return singleWhiteSpaced.trim();
    }
}
