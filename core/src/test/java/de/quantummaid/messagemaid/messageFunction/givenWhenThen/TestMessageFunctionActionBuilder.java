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

package de.quantummaid.messagemaid.messageFunction.givenWhenThen;

import de.quantummaid.messagemaid.messageFunction.testResponses.RequestResponseFuturePair;
import de.quantummaid.messagemaid.messageFunction.testResponses.SimpleTestRequest;
import de.quantummaid.messagemaid.messageFunction.testResponses.SimpleTestResponse;
import de.quantummaid.messagemaid.messageFunction.testResponses.TestRequest;
import de.quantummaid.messagemaid.identification.CorrelationId;
import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.messageFunction.MessageFunction;
import de.quantummaid.messagemaid.messageFunction.ResponseFuture;
import de.quantummaid.messagemaid.processingContext.EventType;
import de.quantummaid.messagemaid.processingContext.ProcessingContext;
import de.quantummaid.messagemaid.shared.environment.TestEnvironment;
import de.quantummaid.messagemaid.shared.givenWhenThen.TestAction;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static de.quantummaid.messagemaid.identification.CorrelationId.newUniqueCorrelationId;
import static de.quantummaid.messagemaid.shared.environment.TestEnvironmentProperty.*;
import static de.quantummaid.messagemaid.shared.eventType.TestEventType.differentTestEventType;
import static de.quantummaid.messagemaid.shared.eventType.TestEventType.testEventType;
import static de.quantummaid.messagemaid.shared.polling.PollingUtils.pollUntil;
import static de.quantummaid.messagemaid.shared.properties.SharedTestProperties.EVENT_TYPE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestMessageFunctionActionBuilder {
    private final TestAction<MessageFunction> testAction;

    private static TestMessageFunctionActionBuilder asAction(final TestAction<MessageFunction> action) {
        return new TestMessageFunctionActionBuilder(action);
    }

    public static TestMessageFunctionActionBuilder aRequestIsSend() {
        return asAction((messageFunction, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            testEnvironment.setProperty(TEST_OBJECT, testRequest);
            try {
                final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
                return responseFuture;
            } catch (final Exception e) {
                testEnvironment.setPropertyIfNotSet(MessageFunctionTestProperties.EXCEPTION_OCCURRED_DURING_SEND, true);
                throw (RuntimeException) e;
            }
        });
    }

    public static TestMessageFunctionActionBuilder severalRequestsAreSend() {
        return asAction((messageFunction, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final List<RequestResponseFuturePair> requestResponsePairs = new LinkedList<>();
            final int numberOfRequests = 5;
            for (int i = 0; i < numberOfRequests; i++) {
                final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
                final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
                final RequestResponseFuturePair requestResponsePair = RequestResponseFuturePair.requestResponseFuturePair(testRequest, responseFuture);
                requestResponsePairs.add(requestResponsePair);
            }
            return requestResponsePairs;
        });
    }

    public static TestMessageFunctionActionBuilder twoRequestsAreSendThatWithOneOfEachResponsesAnswered() {
        return asAction((messageFunction, testEnvironment) -> {
            final MessageBus messageBus = getMessageBus(testEnvironment);

            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final EventType answerEventType = differentTestEventType();
            messageBus.subscribeRaw(eventType, processingContext -> {
                final CorrelationId wrongCorrelationId = newUniqueCorrelationId();
                final SimpleTestResponse wrongResponse = SimpleTestResponse.testResponse(null);
                messageBus.send(answerEventType, wrongResponse, wrongCorrelationId);

                final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
                final TestRequest payload = (TestRequest) processingContext.getPayload();
                final SimpleTestResponse testResponse = SimpleTestResponse.testResponse(payload);
                messageBus.send(answerEventType, testResponse, correlationId);
            });

            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            testEnvironment.setProperty(TEST_OBJECT, testRequest);
            return messageFunction.request(eventType, testRequest);
        });
    }

    public static TestMessageFunctionActionBuilder aFollowUpActionIsAddedBeforeSend() {
        return asAction((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            testEnvironment.setProperty(TEST_OBJECT, testRequest);
            final String expectedResult = "success";
            testEnvironment.setProperty(EXPECTED_RESULT, expectedResult);

            final MessageBus messageBus = getMessageBus(testEnvironment);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final RequestStorage<Object> requestStorage = RequestStorage.requestStorage();
            messageBus.subscribeRaw(eventType, requestStorage::storeRequest);
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            responseFuture.then((testResponse, errorResponse, exception) -> testEnvironment.setProperty(RESULT, expectedResult));
            pollUntil(requestStorage::hasRequestStored);

            final SimpleTestResponse testResponse = SimpleTestResponse.testResponse(testRequest);
            final ProcessingContext<Object> processingContext = requestStorage.getRequest();
            final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
            final EventType answerEventType = differentTestEventType();
            messageBus.send(answerEventType, testResponse, correlationId);
            return null;
        });
    }

    public static TestMessageFunctionActionBuilder aFollowUpActionExecutingOnlyOnceIsAddedBeforeRequest() {
        return asAction((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture request = messageFunction.request(eventType, testRequest);

            request.then((response, errorResponse, exception) -> {
                if (!testEnvironment.has(RESULT)) {
                    if (exception != null) {
                        testEnvironment.setPropertyIfNotSet(RESULT, exception);
                    } else {
                        testEnvironment.setPropertyIfNotSet(RESULT, response);
                    }
                } else {
                    testEnvironment.setPropertyIfNotSet(EXCEPTION, new RuntimeException("FollowUp called twice"));
                }
            });
            return null;
        });
    }

    public static TestMessageFunctionActionBuilder aFollowUpActionForAnExceptionIsAdded() {
        return aFollowUpExpectingExceptionIsAdded();
    }

    public static TestMessageFunctionActionBuilder aFollowUpExpectingExceptionIsAdded() {
        return asAction((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            testEnvironment.setProperty(TEST_OBJECT, testRequest);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            responseFuture.then((testResponse, errorResponse, exception) -> {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, exception);
                testEnvironment.setPropertyIfNotSet(MessageFunctionTestProperties.EXCEPTION_OCCURRED_DURING_FOLLOW_UP, true);
            });
            return null;
        });
    }

    public static TestMessageFunctionActionBuilder aRequestIsSendThatCausesAnException() {
        return asAction((messageFunction, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final SimpleTestRequest request = SimpleTestRequest.testRequest();
            final ResponseFuture responseFuture = messageFunction.request(eventType, request);
            try {
                responseFuture.get();
            } catch (final Exception e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
                return responseFuture;
            }
            return responseFuture;
        });
    }

    public static TestMessageFunctionActionBuilder forTheResponseIsWaitedASpecificTime() {
        return asAction((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final long expectedTimeout = MILLISECONDS.toMillis(100);
            testEnvironment.setProperty(EXPECTED_RESULT, expectedTimeout);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            final long timeoutStart = System.currentTimeMillis();
            try {
                responseFuture.get(100, MILLISECONDS);
                throw new RuntimeException("Future should not return a value");
            } catch (final InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } catch (final TimeoutException e) {
                final long timeoutEnd = System.currentTimeMillis();
                final long duration = timeoutEnd - timeoutStart;
                return duration;
            }
        });
    }

    public static TestMessageFunctionActionBuilder aRequestIsCancelled() {
        return callCancelXTimes(1);
    }

    public static TestMessageFunctionActionBuilder aRequestIsCancelledSeveralTimes() {
        return callCancelXTimes(5);
    }

    private static TestMessageFunctionActionBuilder callCancelXTimes(final int cancelCalls) {
        return asAction((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            responseFuture.then((testResponse, errorResponse, exception) -> {
                throw new RuntimeException("This FollowUpActionShouldNotBeCalled");
            });
            for (int i = 0; i < cancelCalls; i++) {
                final boolean cancelResult = responseFuture.cancel(true);
                testEnvironment.addToListProperty(MessageFunctionTestProperties.CANCEL_RESULTS, cancelResult);
            }
            final MessageBus messageBus = getMessageBus(testEnvironment);
            messageBus.send(eventType, SimpleTestResponse.testResponse(testRequest));
            return responseFuture;
        });
    }

    public static TestMessageFunctionActionBuilder theFutureIsFulfilledAndThenCancelled() {
        return asAction((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final MessageBus messageBus = getMessageBus(testEnvironment);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            messageBus.subscribeRaw(eventType, processingContext -> {
                final SimpleTestResponse response = SimpleTestResponse.testResponse(testRequest);
                final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
                final EventType answerEventType = differentTestEventType();
                messageBus.send(answerEventType, response, correlationId);
            });
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            pollUntil(responseFuture::isDone);
            final boolean cancelResult = responseFuture.cancel(true);
            testEnvironment.setProperty(MessageFunctionTestProperties.CANCEL_RESULTS, cancelResult);
            return responseFuture;
        });
    }

    public static TestMessageFunctionActionBuilder aResponseToACancelledRequestDoesNotExecuteFollowUpAction() {
        return asAction((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            responseFuture.then((testResponse, errorResponse, exception) -> {
                throw new RuntimeException("This FollowUpActionShouldNotBeCalled");
            });
            final boolean cancelResult = responseFuture.cancel(true);
            testEnvironment.addToListProperty(MessageFunctionTestProperties.CANCEL_RESULTS, cancelResult);
            answerCancelledFuture(testEnvironment, testRequest);
            return responseFuture;
        });
    }

    private static void answerCancelledFuture(final TestEnvironment testEnvironment, final SimpleTestRequest testRequest) {
        final MessageBus messageBus = getMessageBus(testEnvironment);
        final SimpleTestResponse response = SimpleTestResponse.testResponse(testRequest);
        final EventType answerEventType = differentTestEventType();
        final RequestStorage<Object> requestStorage = RequestStorage.requestStorage();
        messageBus.subscribeRaw(answerEventType, requestStorage::storeRequest);
        messageBus.send(answerEventType, response);
        pollUntil(requestStorage::hasRequestStored);
    }

    public static TestMessageFunctionActionBuilder theResultOfACancelledRequestIsTaken() {
        return asAction((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            responseFuture.cancel(true);
            try {
                responseFuture.get(1, SECONDS);
            } catch (final Exception e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return responseFuture;
        });
    }

    public static TestMessageFunctionActionBuilder aRequestsIsCancelledWhileOtherThreadsWait() {
        return asAction((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            final CyclicBarrier barrier = new CyclicBarrier(3);
            final Semaphore waitingSemaphoreGet = new Semaphore(0);
            final Semaphore waitingSemaphoreGetTimeout = new Semaphore(0);
            letThreadWaitOnFuture(testEnvironment, responseFuture, waitingSemaphoreGet, barrier, false);
            letThreadWaitOnFuture(testEnvironment, responseFuture, waitingSemaphoreGetTimeout, barrier, true);

            try {
                barrier.await(1, SECONDS);
                MILLISECONDS.sleep(10);
                responseFuture.cancel(true);
                waitingSemaphoreGet.acquire();
                waitingSemaphoreGetTimeout.acquire();
            } catch (final InterruptedException | BrokenBarrierException | TimeoutException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return responseFuture;
        });
    }

    private static void letThreadWaitOnFuture(final TestEnvironment testEnvironment,
                                              final ResponseFuture responseFuture,
                                              final Semaphore semaphore,
                                              final CyclicBarrier barrier,
                                              final boolean withTimeout) {
        new Thread(() -> {
            boolean interruptedExceptionCalled = false;
            try {
                barrier.await(1, SECONDS);
                if (withTimeout) {
                    responseFuture.get(5, SECONDS);
                } else {
                    responseFuture.get();
                }
                final RuntimeException exception = new RuntimeException("Future should not return result");
                testEnvironment.setPropertyIfNotSet(EXCEPTION, exception);
            } catch (InterruptedException e) {
                interruptedExceptionCalled = true;
            } catch (ExecutionException | TimeoutException | BrokenBarrierException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            if (!interruptedExceptionCalled) {
                final String message = "Future should wake waiting threads with InterruptedException";
                final RuntimeException exception = new RuntimeException(message);
                testEnvironment.setPropertyIfNotSet(EXCEPTION, exception);
            }
            semaphore.release();
        }).start();
    }

    public static TestMessageFunctionActionBuilder aFollowUpActionIsAddedToACancelledFuture() {
        return asAction((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            responseFuture.cancel(true);
            try {
                responseFuture.then((response, errorResponse, exception) -> {
                    throw new UnsupportedOperationException();
                });
            } catch (final Exception e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return responseFuture;
        });
    }

    public static TestMessageFunctionActionBuilder theMessageFunctionIsClosed() {
        return asAction((messageFunction, testEnvironment) -> {
            messageFunction.close();
            return null;
        });
    }

    private static MessageBus getMessageBus(final TestEnvironment testEnvironment) {
        return testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
    }

    public TestMessageFunctionActionBuilder andThen(final TestMessageFunctionActionBuilder other) {
        return asAction((messageFunction, testEnvironment) -> {
            this.testAction.execute(messageFunction, testEnvironment);
            other.testAction.execute(messageFunction, testEnvironment);
            return null;
        });
    }

    public TestAction<MessageFunction> build() {
        return testAction;
    }

}
