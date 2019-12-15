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

package de.quantummaid.messagemaid.internal.pipe.givenWhenThen;

import de.quantummaid.messagemaid.shared.exceptions.TestException;
import de.quantummaid.messagemaid.shared.validations.SharedTestValidations;
import de.quantummaid.messagemaid.internal.pipe.Pipe;
import de.quantummaid.messagemaid.shared.environment.TestEnvironment;
import de.quantummaid.messagemaid.shared.givenWhenThen.TestValidation;
import de.quantummaid.messagemaid.shared.subscriber.TestSubscriber;
import de.quantummaid.messagemaid.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.SUT;
import static de.quantummaid.messagemaid.shared.properties.SharedTestProperties.MESSAGES_SEND;
import static de.quantummaid.messagemaid.shared.properties.SharedTestProperties.POTENTIAL_RECEIVERS;
import static de.quantummaid.messagemaid.shared.pipeChannelMessageBus.PipeChannelMessageBusSharedTestValidations.*;
import static de.quantummaid.messagemaid.shared.polling.PollingUtils.pollUntilEquals;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeValidationBuilder {
    private final TestValidation testValidation;

    public static PipeValidationBuilder expectTheMessageToBeReceived() {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertExpectedReceiverReceivedSingleMessage(testEnvironment);
        });
    }

    public static PipeValidationBuilder expectAllMessagesToBeReceivedByAllSubscribers() {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertExpectedReceiverReceivedAllMessages(testEnvironment);
        });
    }

    public static PipeValidationBuilder expectEachMessagesToBeReceivedByOnlyOneSubscriber() {
        return new PipeValidationBuilder(testEnvironment -> {
            final List<TestSubscriber<?>> subscribers = getAsSubscriberList(testEnvironment, POTENTIAL_RECEIVERS);
            final List<?> sendMessages = testEnvironment.getPropertyAsListOfType(MESSAGES_SEND, Object.class);
            final int expectedNumberOfMessages = sendMessages.size();
            pollUntilEquals(() -> subscribers.stream()
                    .mapToInt(s -> s.getReceivedMessages().size())
                    .sum(), expectedNumberOfMessages);
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertEachMessagesToBeReceivedByOnlyOneSubscriber(testEnvironment);
        });
    }

    public static PipeValidationBuilder expectAllRemainingSubscribersToStillBeSubscribed() {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final PipeTestActions sutActions = getPipeTestActions(testEnvironment);
            assertSutStillHasExpectedSubscriber(sutActions, testEnvironment);
        });
    }

    public static PipeValidationBuilder expectTheListOfAllSubscriber() {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertResultEqualsCurrentSubscriber(testEnvironment);
        });
    }

    public static PipeValidationBuilder expectXMessagesToBeDelivered_despiteTheChannelClosed(final int expectedNumberOfMessages) {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertNumberOfMessagesReceived(testEnvironment, expectedNumberOfMessages);
        });
    }

    public static PipeValidationBuilder expectResultToBe(final Object expectedResult) {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultEqualsExpected(testEnvironment, expectedResult);
        });
    }

    public static PipeValidationBuilder expectTimestampToBeInTheLastXSeconds(final long maximumSecondsDifference) {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertTimestampToBeInTheLastXSeconds(testEnvironment, maximumSecondsDifference);
        });
    }

    public static PipeValidationBuilder expectThePipeToBeShutdownInTime() {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final PipeTestActions testActions = getPipeTestActions(testEnvironment);
            assertSutWasShutdownInTime(testActions, testEnvironment);
        });
    }

    public static PipeValidationBuilder expectThePipeToBeShutdown() {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final PipeTestActions testActions = getPipeTestActions(testEnvironment);
            assertSutIsShutdown(testActions);
        });
    }

    public static PipeValidationBuilder expectTheException(final Class<?> expectedExceptionClass) {
        return new PipeValidationBuilder(testEnvironment -> SharedTestValidations.assertExceptionThrownOfType(testEnvironment, expectedExceptionClass));
    }

    public static PipeValidationBuilder expectNoException() {
        return new PipeValidationBuilder(SharedTestValidations::assertNoExceptionThrown);
    }

    public static PipeValidationBuilder expectTheResultToAlwaysBeFalse() {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultEqualsExpected(testEnvironment, false);
        });
    }

    public static PipeValidationBuilder expectTheAwaitToBeTerminatedSuccessful(final int expectedNumberOfReceivedMessages) {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultEqualsExpected(testEnvironment, true);
            assertNumberOfMessagesReceived(testEnvironment, expectedNumberOfReceivedMessages);
        });
    }

    public static PipeValidationBuilder expectTheAwaitToBeTerminatedSuccessful() {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultEqualsExpected(testEnvironment, true);
        });
    }

    public static PipeValidationBuilder expectTheAwaitToBeTerminatedWithFailure() {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultEqualsExpected(testEnvironment, false);
        });
    }

    public static PipeValidationBuilder expectTheExceptionToBeHandled() {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            SharedTestValidations.assertResultOfClass(testEnvironment, TestException.class);
        });
    }

    public static PipeValidationBuilder expectTheDeliveryToBeStillSuccessful() {
        return new PipeValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertNumberOfMessagesReceived(testEnvironment, 0);
            final PipeTestActions testActions = getPipeTestActions(testEnvironment);
            pollUntilEquals(testActions::getTheNumberOfSuccessfulDeliveredMessages, 1L);
        });
    }

    @SuppressWarnings("unchecked")
    private static Pipe<TestMessage> getPipe(final TestEnvironment testEnvironment) {
        return (Pipe<TestMessage>) testEnvironment.getProperty(SUT);
    }

    @SuppressWarnings("unchecked")
    private static List<TestSubscriber<?>> getAsSubscriberList(final TestEnvironment testEnvironment,
                                                               final String property) {
        return (List<TestSubscriber<?>>) testEnvironment.getProperty(property);
    }

    private static PipeTestActions getPipeTestActions(final TestEnvironment testEnvironment) {
        final Pipe<TestMessage> pipe = getPipe(testEnvironment);
        return PipeTestActions.pipeTestActions(pipe);
    }

    public TestValidation build() {
        return testValidation;
    }
}
