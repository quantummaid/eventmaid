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

package de.quantummaid.eventmaid.channel.givenwhenthen;

import de.quantummaid.eventmaid.channel.Channel;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.givenwhenthen.TestAction;
import de.quantummaid.eventmaid.shared.givenwhenthen.TestValidation;
import de.quantummaid.eventmaid.shared.testmessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.EXCEPTION;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.SUT;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Then {
    private final ChannelSetupBuilder channelSetupBuilder;
    private final ChannelActionBuilder channelActionBuilder;

    public void then(final ChannelValidationBuilder channelValidationBuilder) {
        final TestEnvironment testEnvironment = channelSetupBuilder.build();
        final List<TestAction<Channel<TestMessage>>> testActions = channelActionBuilder.build();
        @SuppressWarnings("unchecked")
        final Channel<TestMessage> channel = (Channel<TestMessage>) testEnvironment.getProperty(SUT);
        try {
            for (final TestAction<Channel<TestMessage>> testAction : testActions) {
                testAction.execute(channel, testEnvironment);
            }
        } catch (final Exception e) {
            testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
        }
        final TestValidation testValidation = channelValidationBuilder.build();
        testValidation.validate(testEnvironment);
        channel.close(false);
    }
}
