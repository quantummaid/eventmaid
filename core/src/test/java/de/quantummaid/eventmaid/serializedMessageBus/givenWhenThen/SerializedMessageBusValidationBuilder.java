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

package de.quantummaid.eventmaid.serializedMessageBus.givenWhenThen;

import de.quantummaid.eventmaid.shared.exceptions.TestException;
import de.quantummaid.eventmaid.shared.validations.SharedTestValidations;
import de.quantummaid.eventmaid.messageBus.MessageBus;
import de.quantummaid.eventmaid.messageBus.MessageBusStatusInformation;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.givenWhenThen.TestValidation;
import de.quantummaid.eventmaid.shared.subscriber.TestSubscriber;
import de.quantummaid.eventmaid.shared.testMessages.TestMessageOfInterest;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import de.quantummaid.eventmaid.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.*;
import static de.quantummaid.eventmaid.shared.polling.PollingUtils.pollUntil;
import static de.quantummaid.eventmaid.shared.polling.PollingUtils.pollUntilListHasSize;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SerializedMessageBusValidationBuilder {
    private final TestValidation testValidation;

    public static SerializedMessageBusValidationBuilder expectTheCorrectDataToBeReceived() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertCorrectDataToBeReceived(testEnvironment);
        });
    }

    public static SerializedMessageBusValidationBuilder expectTheDataAndTheErrorToBeReceived() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertCorrectDataToBeReceived(testEnvironment);
        });
    }

    public static SerializedMessageBusValidationBuilder expectToHaveWaitedUntilTheCorrectResponseWasReceived() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            assertReceivedResultEqualsExpected(testEnvironment);
        });
    }

    public static SerializedMessageBusValidationBuilder expectTheSendDataToBeReturnedAsErrorData() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final Object sendObject = testEnvironment.getProperty(SerializedMessageBusTestProperties.SEND_DATA);
            assertReceivedAsErrorResponse(testEnvironment, sendObject);
        });
    }

    private static void assertReceivedAsErrorResponse(final TestEnvironment testEnvironment, final Object expectedResult) {
        pollUntil(() -> testEnvironment.has(RESULT));
        final PayloadAndErrorPayload<?, ?> result = (PayloadAndErrorPayload<?, ?>) testEnvironment.getProperty(RESULT);
        final Object errorPayload = result.getErrorPayload();
        SharedTestValidations.assertEquals(errorPayload, expectedResult);
    }

    public static SerializedMessageBusValidationBuilder expectToHaveWaitedUntilTheNotSerializedResponseWasReceived() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final TestMessageOfInterest sendObject = testEnvironment.getPropertyAsType(SerializedMessageBusTestProperties.SEND_DATA, TestMessageOfInterest.class);
            final HashMap<String, Object> expectedResult = new HashMap<>();
            expectedResult.put(SerializedMessageBusSetupBuilder.PAYLOAD_SERIALIZATION_KEY, sendObject.getContent());
            assertReceivedResultEqualsExpected(testEnvironment, expectedResult, null);
        });
    }

    public static SerializedMessageBusValidationBuilder expectTheSendDataToBeReturnedAsNotSerializedErrorData() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertNoExceptionThrown(testEnvironment);
            final TestMessageOfInterest sendObject = testEnvironment.getPropertyAsType(SerializedMessageBusTestProperties.SEND_DATA, TestMessageOfInterest.class);
            final HashMap<String, Object> expectedResult = new HashMap<>();
            expectedResult.put(SerializedMessageBusSetupBuilder.PAYLOAD_SERIALIZATION_KEY, sendObject.getContent());
            assertReceivedAsErrorResponse(testEnvironment, expectedResult);
        });
    }

    public static SerializedMessageBusValidationBuilder expectTheTimeoutToBeOccurred() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertExceptionThrownOfType(testEnvironment, TimeoutException.class);
        });
    }

    public static SerializedMessageBusValidationBuilder expectAnExecutionExceptionWithTheCorrectCause() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertExceptionThrownOfTypeWithCause(testEnvironment, ExecutionException.class, TestException.class);
        });
    }

    public static SerializedMessageBusValidationBuilder expectAnExecutionExceptionFor(final Class<?> expectedCauseClass) {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertExceptionThrownOfTypeWithCause(testEnvironment, ExecutionException.class, expectedCauseClass);
        });
    }

    public static SerializedMessageBusValidationBuilder expectTheException(final Class<?> expectedExceptionClass) {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            SharedTestValidations.assertExceptionThrownOfType(testEnvironment, expectedExceptionClass);
        });
    }

    public static SerializedMessageBusValidationBuilder expectNoRemainingSubscriber() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            final MessageBus underlyingMessageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            final MessageBusStatusInformation statusInformation = underlyingMessageBus.getStatusInformation();
            final List<Subscriber<?>> allSubscribers = statusInformation.getAllSubscribers();
            SharedTestValidations.assertCollectionOfSize(allSubscribers, 0);
        });
    }

    private static void assertCorrectDataToBeReceived(final TestEnvironment testEnvironment) {
        final Object expectedErrorPayload;
        if (testEnvironment.has(SerializedMessageBusTestProperties.SEND_ERROR_DATA)) {
            expectedErrorPayload = testEnvironment.getProperty(SerializedMessageBusTestProperties.SEND_ERROR_DATA);
        } else {
            expectedErrorPayload = null;
        }
        final Object expectedPayload = testEnvironment.getProperty(SerializedMessageBusTestProperties.SEND_DATA);
        final List<TestSubscriber<PayloadAndErrorPayload<?, ?>>> receivers = getExpectedPayloadsReceivers(testEnvironment);
        for (final TestSubscriber<PayloadAndErrorPayload<?, ?>> receiver : receivers) {
            final List<PayloadAndErrorPayload<?, ?>> receivedMessages = receiver.getReceivedMessages();
            pollUntilListHasSize(receivedMessages, 1);
            final PayloadAndErrorPayload<?, ?> payloadAndErrorPayload = receivedMessages.get(0);
            final Object payload = payloadAndErrorPayload.getPayload();
            SharedTestValidations.assertEquals(payload, expectedPayload);
            final Object errorPayload = payloadAndErrorPayload.getErrorPayload();
            SharedTestValidations.assertEquals(errorPayload, expectedErrorPayload);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<TestSubscriber<PayloadAndErrorPayload<?, ?>>> getExpectedPayloadsReceivers(
            final TestEnvironment testEnvironment) {
        return (List<TestSubscriber<PayloadAndErrorPayload<?, ?>>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
    }

    private static void assertReceivedResultEqualsExpected(final TestEnvironment testEnvironment) {
        final Object expectedPayload = testEnvironment.getProperty(SerializedMessageBusTestProperties.SEND_DATA);
        final Object expectedErrorPayload;
        if (testEnvironment.has(SerializedMessageBusTestProperties.SEND_ERROR_DATA)) {
            expectedErrorPayload = testEnvironment.getProperty(SerializedMessageBusTestProperties.SEND_ERROR_DATA);
        } else {
            expectedErrorPayload = null;
        }
        assertReceivedResultEqualsExpected(testEnvironment, expectedPayload, expectedErrorPayload);
    }

    private static void assertReceivedResultEqualsExpected(final TestEnvironment testEnvironment,
                                                           final Object expectedPayload,
                                                           final Object expectedErrorPayload) {
        final PayloadAndErrorPayload<?, ?> payloadAndErrorPayload = getResultPayloads(testEnvironment);
        final Object payload = payloadAndErrorPayload.getPayload();
        SharedTestValidations.assertEquals(payload, expectedPayload);
        final Object errorPayload = payloadAndErrorPayload.getErrorPayload();
        SharedTestValidations.assertEquals(errorPayload, expectedErrorPayload);
    }

    private static PayloadAndErrorPayload<?, ?> getResultPayloads(final TestEnvironment testEnvironment) {
        pollUntil(() -> testEnvironment.has(RESULT));
        return (PayloadAndErrorPayload<?, ?>) testEnvironment.getProperty(RESULT);
    }

    public TestValidation build() {
        return testValidation;
    }
}
