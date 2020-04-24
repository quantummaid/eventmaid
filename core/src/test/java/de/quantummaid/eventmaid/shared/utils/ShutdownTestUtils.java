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

package de.quantummaid.eventmaid.shared.utils;

import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty;
import de.quantummaid.eventmaid.shared.eventtype.TestEventType;
import de.quantummaid.eventmaid.shared.pipechannelmessagebus.testActions.CloseActions;
import de.quantummaid.eventmaid.shared.pipechannelmessagebus.testActions.SendingAndReceivingActions;
import de.quantummaid.eventmaid.shared.polling.PollingUtils;
import de.quantummaid.eventmaid.shared.properties.SharedTestProperties;
import de.quantummaid.eventmaid.shared.subscriber.BlockingTestSubscriber;
import de.quantummaid.eventmaid.shared.testmessages.TestMessage;
import de.quantummaid.eventmaid.shared.testmessages.TestMessageOfInterest;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ShutdownTestUtils {

    public static void shutdownTheSut(final CloseActions closeActions) {
        shutdownTheSut(closeActions, true);
    }

    public static void shutdownTheSut(final CloseActions closeActions, final boolean finishRemainingTasks) {
        closeActions.close(finishRemainingTasks);
    }

    public static void shutdownTheSutAsynchronouslyXTimes(final CloseActions closeActions, final int shutdownCalls) {
        final ExecutorService executorService = Executors.newFixedThreadPool(shutdownCalls);
        for (int i = 0; i < shutdownCalls; i++) {
            executorService.execute(() -> closeActions.close(false));
        }
        executorService.shutdown();
        try {
            final boolean terminationSuccessful = executorService.awaitTermination(1, SECONDS);
            if (!terminationSuccessful) {
                throw new RuntimeException("Executor service did not shutdown in a clean way.");
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendMessagesBeforeShutdownAsynchronously(final SendingAndReceivingActions sutActions,
                                                                final TestEnvironment testEnvironment,
                                                                final int numberOfMessages,
                                                                final boolean finishRemainingTasks) {
        sendMessagesBeforeShutdownAsynchronously(sutActions, testEnvironment, numberOfMessages, finishRemainingTasks,
                numberOfMessages);
    }

    public static void sendMessagesBeforeShutdownAsynchronously(final SendingAndReceivingActions sutActions,
                                                                final TestEnvironment testEnvironment,
                                                                final int numberOfMessages,
                                                                final boolean finishRemainingTasks,
                                                                final int expectedNumberOfBlockedThreads) {
        final Semaphore semaphore = new Semaphore(0);
        final BlockingTestSubscriber<TestMessage> subscriber = BlockingTestSubscriber.blockingTestSubscriber(semaphore);
        SendingTestUtils.addABlockingSubscriberAndThenSendXMessagesInEachThread(sutActions, subscriber, numberOfMessages,
                1, testEnvironment, expectedNumberOfBlockedThreads);
        sutActions.close(finishRemainingTasks);
        semaphore.release(1337);
    }

    public static void awaitTermination(final CloseActions closeActions, final TestEnvironment testEnvironment) {
        final int seconds = 1;
        awaitTermination(closeActions, testEnvironment, seconds);
    }

    public static void awaitTermination(final CloseActions closeActions,
                                        final TestEnvironment testEnvironment,
                                        final int seconds) {
        try {
            final boolean terminatedSuccessful = closeActions.await(seconds, SECONDS);
            testEnvironment.setProperty(TestEnvironmentProperty.RESULT, terminatedSuccessful);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void callAwaitWithoutACloseIsCalled(final CloseActions closeActions, final TestEnvironment testEnvironment) {
        try {
            final boolean terminatedSuccessful = closeActions.await(0, SECONDS);
            testEnvironment.setProperty(TestEnvironmentProperty.RESULT, terminatedSuccessful);
            testEnvironment.setProperty(TestEnvironmentProperty.EXPECTED_RESULT, false);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void shutDownTheSutThenSendAMessage(final SendingAndReceivingActions sutActions,
                                                      final TestEnvironment testEnvironment) {
        sutActions.close(false);
        try {
            sutActions.await(1, SECONDS);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(SharedTestProperties.EVENT_TYPE, TestEventType.testEventType());
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            sutActions.send(eventType, message);
        } catch (final Exception e) {
            testEnvironment.setProperty(TestEnvironmentProperty.EXCEPTION, e);
        }
    }

    public static void closeAndThenAwaitTermination(final SendingAndReceivingActions sutActions,
                                                    final TestEnvironment testEnvironment) {
        final int timeoutInSeconds = 1;
        closeAndThenAwaitTermination(sutActions, testEnvironment, timeoutInSeconds);
    }

    public static void closeAndThenAwaitTermination(final SendingAndReceivingActions sutActions,
                                                    final TestEnvironment testEnvironment,
                                                    final int timeoutInSeconds) {
        try {
            sutActions.close(true);
            final boolean terminatedSuccessful = sutActions.await(timeoutInSeconds, SECONDS);
            testEnvironment.setProperty(TestEnvironmentProperty.RESULT, terminatedSuccessful);
        } catch (final InterruptedException e) {
            testEnvironment.setPropertyIfNotSet(TestEnvironmentProperty.EXCEPTION, e);
        }
    }

    public static void closeAndThenWaitForPendingTasksToFinished(final SendingAndReceivingActions sutActions,
                                                                 final int numberOfPendingMessages,
                                                                 final TestEnvironment testEnvironment) {
        try {
            final Semaphore semaphore = new Semaphore(0);
            sendMessagesToBlockingSubscriber(sutActions, numberOfPendingMessages, testEnvironment, semaphore);

            sutActions.close(true);
            releaseSemaphoreDelayedInDifferentThread(testEnvironment, semaphore);
            final boolean terminatedSuccessful = sutActions.await(1, SECONDS);
            testEnvironment.setProperty(TestEnvironmentProperty.RESULT, terminatedSuccessful);
        } catch (final InterruptedException | BrokenBarrierException e) {
            testEnvironment.setPropertyIfNotSet(TestEnvironmentProperty.EXCEPTION, e);
        }
    }

    private static void releaseSemaphoreDelayedInDifferentThread(final TestEnvironment testEnvironment,
                                                                 final Semaphore semaphore)
            throws InterruptedException, BrokenBarrierException {
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
        new Thread(() -> {
            try {
                cyclicBarrier.await();
                MILLISECONDS.sleep(10);
                semaphore.release(1000);
            } catch (InterruptedException | BrokenBarrierException e) {
                testEnvironment.setProperty(TestEnvironmentProperty.EXCEPTION, e);
            }
        }).start();
        cyclicBarrier.await();
    }

    private static BlockingTestSubscriber<TestMessage> sendMessagesToBlockingSubscriber(
            final SendingAndReceivingActions sutActions,
            final int numberOfPendingMessages,
            final TestEnvironment testEnvironment,
            final Semaphore semaphore) {
        return sendMessagesToBlockingSubscriber(sutActions, numberOfPendingMessages, numberOfPendingMessages,
                testEnvironment, semaphore);
    }

    private static BlockingTestSubscriber<TestMessage> sendMessagesToBlockingSubscriber(
            final SendingAndReceivingActions sutActions,
            final int numberOfPendingMessages,
            final int expectedNumberOfBlockedThreads,
            final TestEnvironment testEnvironment,
            final Semaphore semaphore) {
        final BlockingTestSubscriber<TestMessage> subscriber = BlockingTestSubscriber.blockingTestSubscriber(semaphore);
        testEnvironment.setProperty(SharedTestProperties.SINGLE_RECEIVER, subscriber);
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(SharedTestProperties.EVENT_TYPE, TestEventType.testEventType());
        sutActions.subscribe(eventType, subscriber);
        SendingTestUtils.sendXMessagesInTheirOwnThreadThatWillBeBlocked(sutActions, numberOfPendingMessages, testEnvironment);
        PollingUtils.pollUntilEquals(subscriber::getNumberOfBlockedThreads, expectedNumberOfBlockedThreads);
        return subscriber;
    }

    public static void sendMessagesBeforeAndAfterShutdownAsynchronously(final SendingAndReceivingActions sutActions,
                                                                        final TestEnvironment testEnvironment,
                                                                        final int numberOfMessagesBeforeShutdown,
                                                                        final int numberOfMessagesAfterShutdown,
                                                                        final boolean finishRemainingTask) {
        final Semaphore semaphore = new Semaphore(0);
        final BlockingTestSubscriber<TestMessage> subscriber = BlockingTestSubscriber.blockingTestSubscriber(semaphore);
        SendingTestUtils.addABlockingSubscriberAndThenSendXMessagesInEachThread(sutActions, subscriber, numberOfMessagesBeforeShutdown,
                testEnvironment);
        sutActions.close(finishRemainingTask);
        SendingTestUtils.sendXMessagesAsynchronouslyThatWillFail(sutActions, numberOfMessagesAfterShutdown, testEnvironment);
        semaphore.release(1337);
        if (finishRemainingTask) {
            PollingUtils.pollUntilListHasSize(subscriber::getReceivedMessages, numberOfMessagesBeforeShutdown);
        }
    }

    public static void callCloseThenAwaitWithBlockedSubscriberWithoutReleasingLock(final SendingAndReceivingActions sutActions,
                                                                                   final TestEnvironment testEnvironment,
                                                                                   final int numberOfMessagesSend,
                                                                                   final int expectedNumberOfBlockedThreads) {
        try {
            final Semaphore semaphore = new Semaphore(0);
            final BlockingTestSubscriber<TestMessage> subscriber = sendMessagesToBlockingSubscriber(sutActions,
                    numberOfMessagesSend, expectedNumberOfBlockedThreads, testEnvironment, semaphore);
            PollingUtils.pollUntilEquals(subscriber::getNumberOfBlockedThreads, expectedNumberOfBlockedThreads);
            final List<?> messagesSend = testEnvironment.getPropertyAsType(SharedTestProperties.MESSAGES_SEND, List.class);
            PollingUtils.pollUntilListHasSize(messagesSend, numberOfMessagesSend);
            final int expectedNumberOfQueuedMessages = numberOfMessagesSend - expectedNumberOfBlockedThreads;
            PollingUtils.pollUntilEquals(sutActions::numberOfQueuedMessages, expectedNumberOfQueuedMessages);
            sutActions.close(true);
            final boolean terminatedSuccessful = sutActions.await(15, MILLISECONDS);
            testEnvironment.setProperty(TestEnvironmentProperty.RESULT, terminatedSuccessful);
            testEnvironment.setProperty(SharedTestProperties.SINGLE_RECEIVER, subscriber);
            testEnvironment.setProperty(SharedTestProperties.EXECUTION_END_SEMAPHORE, semaphore);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
