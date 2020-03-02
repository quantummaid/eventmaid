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

package de.quantummaid.eventmaid.messageFunction.givenWhenThen;

import de.quantummaid.eventmaid.messageFunction.testResponses.RequestResponseFuturePair;
import de.quantummaid.eventmaid.messageFunction.testResponses.SimpleErrorResponse;
import de.quantummaid.eventmaid.messageFunction.testResponses.TestRequest;
import de.quantummaid.eventmaid.messageFunction.testResponses.TestResponse;
import de.quantummaid.eventmaid.shared.exceptions.TestException;
import de.quantummaid.eventmaid.shared.validations.SharedTestValidations;
import de.quantummaid.eventmaid.messageBus.MessageBus;
import de.quantummaid.eventmaid.messageBus.MessageBusStatusInformation;
import de.quantummaid.eventmaid.messageFunction.ResponseFuture;
import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.givenWhenThen.TestValidation;
import de.quantummaid.eventmaid.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.*;
import static de.quantummaid.eventmaid.shared.polling.PollingUtils.pollUntil;
import static de.quantummaid.eventmaid.shared.polling.PollingUtils.pollUntilListHasSize;
import static de.quantummaid.eventmaid.shared.validations.SharedTestValidations.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestMessageFunctionValidationBuilder {
    private final TestValidation testValidation;

    public static TestMessageFunctionValidationBuilder expectCorrectTheResponseToBeReceived() {
        return expectTheResponseToBeReceived();
    }

    public static TestMessageFunctionValidationBuilder expectTheResponseToBeReceived() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final ResponseFuture responseFuture = getResultResponseFuture(testEnvironment);
            final TestRequest testRequest = testEnvironment.getPropertyAsType(TEST_OBJECT, TestRequest.class);
            assertResponseForRequest(responseFuture, testRequest);
        });
    }

    public static TestMessageFunctionValidationBuilder expectTheFutureToHaveAccessToTheErrorResponse() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final ResponseFuture responseFuture = getResultResponseFuture(testEnvironment);
            final TestRequest testRequest = testEnvironment.getPropertyAsType(TEST_OBJECT, TestRequest.class);
            assertErrorResponseForRequest(responseFuture, testRequest);
        });
    }

    public static TestMessageFunctionValidationBuilder expectTheProcessingContextToBeReceived() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final ResponseFuture responseFuture = getResultResponseFuture(testEnvironment);
            assertCorrectResponseProcessingContext(responseFuture, testEnvironment);
        });
    }

    private static void assertResponseForRequest(final ResponseFuture responseFuture, final TestRequest testRequest) {
        try {
            final Object response = responseFuture.get(1, SECONDS);
            assertTrue(responseFuture.wasSuccessful());
            if (response instanceof TestResponse) {
                final TestResponse testResponse = (TestResponse) response;
                final Object request = testResponse.getCorrelatedRequest();
                assertThat(request, equalTo(testRequest));
            } else {
                fail("Unexpected Result in validation found.");
            }
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            fail(e);
        }
    }

    private static void assertErrorResponseForRequest(final ResponseFuture responseFuture, final TestRequest testRequest) {
        try {
            final Object response = responseFuture.getErrorResponse(1, SECONDS);
            assertFalse(responseFuture.wasSuccessful());
            if (response instanceof SimpleErrorResponse) {
                final TestResponse testResponse = (TestResponse) response;
                final Object request = testResponse.getCorrelatedRequest();
                assertThat(request, equalTo(testRequest));
            } else {
                fail("Unexpected Result in validation found.");
            }
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            fail(e);
        }
    }

    private static void assertCorrectResponseProcessingContext(final ResponseFuture responseFuture,
                                                               final TestEnvironment testEnvironment) {
        final ProcessingContext<Object> expectedProcessingContext = getResponseProcessingContext(testEnvironment);
        final Object errorPayload = expectedProcessingContext.getErrorPayload();
        final boolean wasSuccessful = errorPayload == null;
        assertThat(responseFuture.wasSuccessful(), equalTo(wasSuccessful));
        try {
            final Object response = responseFuture.getRaw(1, SECONDS);
            assertThat(response, equalTo(expectedProcessingContext));
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            fail(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static ProcessingContext<Object> getResponseProcessingContext(final TestEnvironment testEnvironment) {
        pollUntil(() -> testEnvironment.has(MessageFunctionTestProperties.RESPONSE_PROCESSING_CONTEXT));
        return (ProcessingContext<Object>) testEnvironment.getProperty(MessageFunctionTestProperties.RESPONSE_PROCESSING_CONTEXT);
    }

    public static TestMessageFunctionValidationBuilder expectCorrectResponseReceivedForEachRequest() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final List<RequestResponseFuturePair> requestResponseFuturePairs = getResultRequestResponsePair(testEnvironment);
            for (final RequestResponseFuturePair requestResponseFuturePair : requestResponseFuturePairs) {
                final ResponseFuture responseFuture = requestResponseFuturePair.getResponseFuture();
                final TestRequest testRequest = requestResponseFuturePair.getTestRequest();
                assertResponseForRequest(responseFuture, testRequest);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static List<RequestResponseFuturePair> getResultRequestResponsePair(final TestEnvironment testEnvironment) {
        return (List<RequestResponseFuturePair>) testEnvironment.getProperty(RESULT);
    }

    public static TestMessageFunctionValidationBuilder expectTheFollowUpToBeExecuted() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            pollUntil(() -> testEnvironment.has(RESULT));
            final Object result = testEnvironment.getProperty(RESULT);
            final Object expectedResult = testEnvironment.getProperty(EXPECTED_RESULT);
            assertEquals(expectedResult, result);
        });
    }

    public static TestMessageFunctionValidationBuilder expectAExceptionToBeReceivedInFollowUp() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            pollUntil(() -> testEnvironment.has(EXCEPTION));
            final Exception exception = getException(testEnvironment);
            assertPropertyTrue(testEnvironment, MessageFunctionTestProperties.EXCEPTION_OCCURRED_DURING_FOLLOW_UP);
            if (testEnvironment.has(EXPECTED_EXCEPTION_MESSAGE)) {
                assertCorrectExceptionMessage(testEnvironment, exception);
            }
        });
    }

    public static TestMessageFunctionValidationBuilder expectAExceptionToBeThrownOfType(final Class<?> expectedClass) {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            final Exception exception = getException(testEnvironment);
            assertEquals(exception.getClass(), expectedClass);
            if (testEnvironment.has(EXPECTED_EXCEPTION_MESSAGE)) {
                assertCorrectExceptionMessage(testEnvironment, exception);
            }
        });
    }

    private static void assertCorrectExceptionMessage(final TestEnvironment testEnvironment, final Exception exception) {
        final String expectedExceptionMessage = testEnvironment.getPropertyAsType(EXPECTED_EXCEPTION_MESSAGE, String.class);
        assertEquals(exception.getMessage(), expectedExceptionMessage);
    }

    public static TestMessageFunctionValidationBuilder expectAFutureToBeFinishedWithException(final Class<?> expectedClass) {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            final Exception exception = getException(testEnvironment);
            assertEquals(exception.getClass(), expectedClass);
            final ResponseFuture responseFuture = getResultResponseFuture(testEnvironment);
            assertTrue(responseFuture.isDone());
            assertFalse(responseFuture.wasSuccessful());
            assertFalse(responseFuture.isCancelled());

            responseFuture.cancel(true);
            assertFalse(responseFuture.isCancelled());
            try {
                responseFuture.get(1, SECONDS);
            } catch (final Exception e) {
                if (!(e instanceof ExecutionException)) {
                    fail("Unexpected Exception.", e);
                }
            }
        });
    }

    public static TestMessageFunctionValidationBuilder expectTheTimeoutToBeTriggeredAtTheCorrectTime() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final long timeoutInMillis = testEnvironment.getPropertyAsType(RESULT, long.class);
            final long expectedTimeoutInMillis = testEnvironment.getPropertyAsType(EXPECTED_RESULT, long.class);
            final long difference = Math.abs(timeoutInMillis - expectedTimeoutInMillis);
            final long maximumAcceptedDifference = MILLISECONDS.toMillis(1);
            assertThat(difference, lessThanOrEqualTo(maximumAcceptedDifference));
        });
    }

    public static TestMessageFunctionValidationBuilder expectTheRequestToBeCancelledAndNoFollowUpActionToBeExecuted() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final ResponseFuture responseFuture = getResultResponseFuture(testEnvironment);
            assertTrue(responseFuture.isCancelled());
            assertTrue(responseFuture.isDone());
            assertFalse(responseFuture.wasSuccessful());
            assertFutureAllowsNotGettingValue(responseFuture);
        });
    }

    public static TestMessageFunctionValidationBuilder expectAllCancellationsToHaveReturnedTheSameResult() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final ResponseFuture responseFuture = getResultResponseFuture(testEnvironment);
            assertCancellationRequestsSucceeded(testEnvironment);
            assertTrue(responseFuture.isCancelled());
            assertTrue(responseFuture.isDone());
            assertFalse(responseFuture.wasSuccessful());
            assertFutureAllowsNotGettingValue(responseFuture);
        });
    }

    public static TestMessageFunctionValidationBuilder expectTheCancellationToFailed() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final ResponseFuture responseFuture = getResultResponseFuture(testEnvironment);
            final Boolean cancelResults = testEnvironment.getPropertyAsType(MessageFunctionTestProperties.CANCEL_RESULTS, Boolean.class);
            assertFalse(cancelResults);
            assertFalse(responseFuture.isCancelled());
            assertTrue(responseFuture.isDone());
            assertTrue(responseFuture.wasSuccessful());
        });
    }

    @SuppressWarnings("unchecked")
    private static void assertCancellationRequestsSucceeded(final TestEnvironment testEnvironment) {
        final List<Boolean> cancelResults = (List<Boolean>) testEnvironment.getProperty(MessageFunctionTestProperties.CANCEL_RESULTS);
        for (final Boolean cancelResult : cancelResults) {
            assertTrue(cancelResult);
        }
    }

    private static void assertFutureAllowsNotGettingValue(final ResponseFuture responseFuture) {
        boolean cancellationExceptionThrownForGet = false;
        try {
            responseFuture.get();
        } catch (final InterruptedException | ExecutionException e) {
            fail(e);
        } catch (final CancellationException e) {
            cancellationExceptionThrownForGet = true;
        }
        assertTrue(cancellationExceptionThrownForGet);

        boolean cancellationExceptionThrownForGetWithTimeout = false;
        try {
            final int timeout = 1000;
            responseFuture.get(timeout, SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            fail(e);
        } catch (final CancellationException e) {
            cancellationExceptionThrownForGetWithTimeout = true;
        }
        assertTrue(cancellationExceptionThrownForGetWithTimeout);
    }

    public static TestMessageFunctionValidationBuilder expectTheFutureToBeFulFilledOnlyOnce() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            pollUntil(() -> testEnvironment.has(RESULT));
            final Object result = testEnvironment.getProperty(RESULT);
            if (result instanceof Exception) {
                SharedTestValidations.assertResultOfClass(testEnvironment, TestException.class);
            } else {
                SharedTestValidations.assertResultAndExpectedResultAreEqual(testEnvironment);
            }
        });
    }

    public static TestMessageFunctionValidationBuilder expectTheExceptionToBeSetOnlyDuringByFuture() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            expectAExceptionToBeThrownOfType(TestException.class);
            assertPropertyFalseOrUnset(testEnvironment, MessageFunctionTestProperties.EXCEPTION_OCCURRED_DURING_SEND);
            assertPropertyTrue(testEnvironment, MessageFunctionTestProperties.EXCEPTION_OCCURRED_DURING_FOLLOW_UP);
        });
    }

    public static TestMessageFunctionValidationBuilder expectNoUnnecssarySubscribersOnTheMessageBus() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            final ResponseFuture responseFuture = getResultResponseFuture(testEnvironment);
            pollUntil(responseFuture::isDone);
            ensureNoExceptionThrown(testEnvironment);
            final MessageBus messageBus = getMessageBus(testEnvironment);
            final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
            final List<Subscriber<?>> allSubscribers = statusInformation.getAllSubscribers();
            final int initialResponseSubscriber = testEnvironment.getPropertyAsType(MessageFunctionTestProperties.NUMBER_OF_INITIAL_SUBSCRIBERS, Integer.class);
            SharedTestValidations.assertCollectionOfSize(allSubscribers, initialResponseSubscriber);
            final int noRemainingErrorListener = 0;
            pollUntilListHasSize(statusInformation::getAllExceptionListener, noRemainingErrorListener);
        });
    }

    private static MessageBus getMessageBus(final TestEnvironment testEnvironment) {
        return testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
    }

    private static Exception getException(final TestEnvironment testEnvironment) {
        pollUntil(() -> testEnvironment.has(EXCEPTION));
        return testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
    }

    public static TestMessageFunctionValidationBuilder expectNullReceived() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            try {
                final ResponseFuture responseFuture = getResultResponseFuture(testEnvironment);
                final Object response = responseFuture.get(1, SECONDS);
                assertThat(response, nullValue());
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                fail(e);
            }
        });
    }

    private static void ensureNoExceptionThrown(final TestEnvironment testEnvironment) {
        if (testEnvironment.has(EXCEPTION)) {
            final Exception exception = getException(testEnvironment);
            fail("Unexpected exception was thrown.", exception);
        }
    }

    private static ResponseFuture getResultResponseFuture(final TestEnvironment testEnvironment) {
        pollUntil(() -> testEnvironment.has(RESULT));
        return testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
    }

    public TestMessageFunctionValidationBuilder and(final TestMessageFunctionValidationBuilder other) {
        return new TestMessageFunctionValidationBuilder(testValidation -> {
            this.testValidation.validate(testValidation);
            other.testValidation.validate(testValidation);
        });
    }

    public TestValidation build() {
        return testValidation;
    }

}
