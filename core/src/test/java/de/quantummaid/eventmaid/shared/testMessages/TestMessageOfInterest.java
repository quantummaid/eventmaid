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

package de.quantummaid.eventmaid.shared.testMessages;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class TestMessageOfInterest implements TestMessage {
    public static final String CONTENT = "TestContent";
    public static final String ERROR_CONTENT = "ErrorContent";
    @Getter
    @Setter
    private String content;

    protected TestMessageOfInterest(final String content) {
        this.setContent(content);
    }

    public static TestMessageOfInterest messageOfInterest() {
        return new TestMessageOfInterest(CONTENT);
    }

    public static TestMessageOfInterest messageOfInterest(final String content) {
        return new TestMessageOfInterest(content);
    }

    public static TestMessageOfInterest messageWithErrorContent() {
        return new TestMessageOfInterest(ERROR_CONTENT);
    }

}
