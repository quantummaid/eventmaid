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

import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.eventType.TestEventType;
import de.quantummaid.eventmaid.shared.pipeChannelMessageBus.testActions.CorrelationIdSendingActions;
import de.quantummaid.eventmaid.shared.pipeChannelMessageBus.testActions.ProcessingContextSendingActions;
import de.quantummaid.eventmaid.shared.pipeChannelMessageBus.testActions.SendingActions;
import de.quantummaid.eventmaid.shared.pipeChannelMessageBus.testActions.SubscribeActions;
import de.quantummaid.eventmaid.shared.polling.PollingUtils;
import de.quantummaid.eventmaid.shared.properties.SharedTestProperties;
import de.quantummaid.eventmaid.shared.subscriber.BlockingTestSubscriber;
import de.quantummaid.eventmaid.shared.testMessages.TestMessage;
import de.quantummaid.eventmaid.shared.testMessages.TestMessageOfInterest;
import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.identification.MessageId;
import de.quantummaid.eventmaid.processingContext.EventType;
import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static de.quantummaid.eventmaid.identification.CorrelationId.newUniqueCorrelationId;
import static de.quantummaid.eventmaid.processingContext.ProcessingContext.processingContext;
import static de.quantummaid.eventmaid.processingContext.ProcessingContext.processingContextForError;
import static de.quantummaid.eventmaid.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SendingTestUtils {

    public static void sendSingleMessage(final SendingActions sendingActions,
                                         final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(SharedTestProperties.EVENT_TYPE, TestEventType.testEventType());
        sendSingleMessage(sendingActions, testEnvironment, eventType);
    }

    public static void sendSingleMessage(final SendingActions sendingActions,
                                         final TestEnvironment testEnvironment,
                                         final EventType eventType) {
        final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
        sendSingleMessage(sendingActions, testEnvironment, eventType, message);
    }

    public static void sendSingleMessage(final SendingActions sendingActions,
                                         final TestEnvironment testEnvironment,
                                         final EventType eventType,
                                         final TestMessage message) {
        testEnvironment.setProperty(SharedTestProperties.SINGLE_SEND_MESSAGE, message);
        final MessageId messageId = sendingActions.send(eventType, message);
        testEnvironment.setProperty(SharedTestProperties.SEND_MESSAGE_ID, messageId);
        testEnvironment.setProperty(SharedTestProperties.NUMBER_OF_MESSAGES_SHOULD_BE_SEND, 1);
    }

    public static void sendSeveralMessages(final SendingActions sendingActions,
                                           final int numberOfMessages,
                                           final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(SharedTestProperties.EVENT_TYPE, TestEventType.testEventType());
        testEnvironment.setProperty(SharedTestProperties.NUMBER_OF_MESSAGES_SHOULD_BE_SEND, numberOfMessages);
        for (int i = 0; i < numberOfMessages; i++) {
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            sendingActions.send(eventType, message);
            testEnvironment.addToListProperty(SharedTestProperties.MESSAGES_SEND, message);
        }
    }

    public static void sendMessageWithCorrelationId(final CorrelationIdSendingActions sendingActions,
                                                    final TestEnvironment testEnvironment) {
        final CorrelationId corId = newUniqueCorrelationId();
        final CorrelationId correlationId = testEnvironment.getPropertyOrSetDefault(SharedTestProperties.EXPECTED_CORRELATION_ID, corId);

        sendMessageWithCorrelationId(sendingActions, testEnvironment, correlationId);
    }

    public static void sendMessageWithCorrelationId(final CorrelationIdSendingActions sendingActions,
                                                    final TestEnvironment testEnvironment,
                                                    final CorrelationId correlationId) {
        final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
        testEnvironment.setProperty(SharedTestProperties.SINGLE_SEND_MESSAGE, message);

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(SharedTestProperties.EVENT_TYPE, TestEventType.testEventType());
        final MessageId messageId = sendingActions.send(eventType, message, correlationId);
        testEnvironment.setProperty(SharedTestProperties.SEND_MESSAGE_ID, messageId);
        testEnvironment.setProperty(SharedTestProperties.NUMBER_OF_MESSAGES_SHOULD_BE_SEND, 1);
    }

    public static void sendMessageWithErrorPayloadIsSend(final ProcessingContextSendingActions sendingActions,
                                                         final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(SharedTestProperties.EVENT_TYPE, TestEventType.testEventType());
        final TestMessageOfInterest errorPayload = TestMessageOfInterest.messageWithErrorContent();
        testEnvironment.setProperty(SharedTestProperties.SEND_ERROR_PAYLOAD, errorPayload);
        final ProcessingContext<TestMessage> processingContext = processingContextForError(eventType, errorPayload);
        sendProcessingContext(sendingActions, testEnvironment, processingContext);
    }

    public static void sendMessageAsProcessingContext(final ProcessingContextSendingActions sendingActions,
                                                      final TestEnvironment testEnvironment,
                                                      final TestMessage message) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(SharedTestProperties.EVENT_TYPE, TestEventType.testEventType());
        sendMessageAsProcessingContext(sendingActions, testEnvironment, message, eventType);
    }

    public static void sendMessageAsProcessingContext(final ProcessingContextSendingActions sendingActions,
                                                      final TestEnvironment testEnvironment,
                                                      final TestMessage message,
                                                      final EventType eventType) {
        final ProcessingContext<TestMessage> processingContext = processingContext(eventType, message);
        sendProcessingContext(sendingActions, testEnvironment, processingContext);
    }

    public static void sendProcessingContext(final ProcessingContextSendingActions sendingActions,
                                             final TestEnvironment testEnvironment,
                                             final ProcessingContext<TestMessage> processingContext) {
        testEnvironment.setProperty(SharedTestProperties.SINGLE_SEND_MESSAGE, processingContext);
        final MessageId messageId = sendingActions.send(processingContext);
        testEnvironment.setProperty(SharedTestProperties.SEND_MESSAGE_ID, messageId);
        testEnvironment.setProperty(SharedTestProperties.NUMBER_OF_MESSAGES_SHOULD_BE_SEND, 1);
    }

    public static void sendValidMessagesAsynchronouslyNew(final SendingActions sendingActions,
                                                          final TestEnvironment testEnvironment,
                                                          final int numberOfSender,
                                                          final int numberOfMessagesPerSender,
                                                          final boolean expectCleanShutdown) {
        final TestMessageFactory messageFactory = TestMessageFactory.messageFactoryForValidMessages(numberOfMessagesPerSender);
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(SharedTestProperties.EVENT_TYPE, TestEventType.testEventType());
        sendXMessagesAsynchronously(numberOfSender, messageFactory, eventType, sendingActions,
                testEnvironment, expectCleanShutdown);
    }

    public static void sendInvalidMessagesAsynchronouslyNew(final SendingActions sendingActions,
                                                            final TestEnvironment testEnvironment,
                                                            final int numberOfSender, final int numberOfMessagesPerSender) {

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(SharedTestProperties.EVENT_TYPE, TestEventType.testEventType());
        sendXMessagesAsynchronously(numberOfSender, TestMessageFactory.messageFactoryForInvalidMessages(numberOfMessagesPerSender), eventType,
                sendingActions, testEnvironment, true);
    }

    public static void sendMixtureOfValidAndInvalidMessagesAsynchronouslyNew(final SendingActions sendingActions,
                                                                             final TestEnvironment testEnvironment,
                                                                             final int numberOfSender,
                                                                             final int numberOfMessagesPerSender) {
        final TestMessageFactory messageFactory = TestMessageFactory.messageFactoryForRandomValidOrInvalidTestMessages(numberOfMessagesPerSender);

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(SharedTestProperties.EVENT_TYPE, TestEventType.testEventType());
        final boolean cleanShutdown = true;
        sendXMessagesAsynchronously(numberOfSender, messageFactory, eventType, sendingActions, testEnvironment, cleanShutdown);
    }

    private static void sendXMessagesAsynchronously(final int numberOfSender,
                                                    final MessageFactory messageFactory,
                                                    final EventType eventType,
                                                    final SendingActions sendingActions,
                                                    final TestEnvironment testEnvironment,
                                                    final boolean expectCleanShutdown) {
        if (numberOfSender <= 0) {
            return;
        }
        final int numberOfMessages = messageFactory.numberOfMessages();
        final int expectedNumberOfMessagesSend = numberOfSender * numberOfMessages;
        testEnvironment.setProperty(SharedTestProperties.NUMBER_OF_MESSAGES_SHOULD_BE_SEND, expectedNumberOfMessagesSend);
        final CyclicBarrier sendingStartBarrier = new CyclicBarrier(numberOfSender);
        final ExecutorService executorService = newFixedThreadPool(numberOfSender);
        for (int i = 0; i < numberOfSender; i++) {
            executorService.execute(() -> {
                final List<TestMessage> messagesToSend = new ArrayList<>();

                for (int j = 0; j < numberOfMessages; j++) {
                    final TestMessage message = messageFactory.createMessage();
                    messagesToSend.add(message);
                    testEnvironment.addToListProperty(SharedTestProperties.MESSAGES_SEND, message);
                }
                try {
                    final int timeout = 3;
                    sendingStartBarrier.await(timeout, SECONDS);
                } catch (final InterruptedException | BrokenBarrierException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
                for (final TestMessage message : messagesToSend) {
                    sendingActions.send(eventType, message);
                }
            });
        }
        executorService.shutdown();
        if (expectCleanShutdown) {
            try {
                final int timeout = 3;
                final boolean isTerminated = executorService.awaitTermination(timeout, SECONDS);
                if (!isTerminated) {
                    throw new RuntimeException("ExecutorService did not shutdown within timeout.");
                }
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void sendXMessagesAsynchronouslyThatWillFail(final SendingActions sendingActions,
                                                               final int numberOfMessages,
                                                               final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(SharedTestProperties.EVENT_TYPE, TestEventType.testEventType());
        final ExecutorService executorService = newFixedThreadPool(numberOfMessages);
        for (int i = 0; i < numberOfMessages; i++) {
            executorService.execute(() -> {
                try {
                    final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
                    sendingActions.send(eventType, message);
                } catch (final Exception ignored) {
                    //ignore
                }
            });
        }
        executorService.shutdown();
    }

    public static void sendXMessagesInTheirOwnThreadThatWillBeBlocked(final SendingActions sendingActions,
                                                                      final int numberOfMessages,
                                                                      final TestEnvironment testEnvironment) {
        final ExecutorService executorService = newFixedThreadPool(numberOfMessages);
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(SharedTestProperties.EVENT_TYPE, TestEventType.testEventType());
        testEnvironment.setPropertyIfNotSet(SharedTestProperties.NUMBER_OF_MESSAGES_SHOULD_BE_SEND, numberOfMessages);
        for (int i = 0; i < numberOfMessages; i++) {
            executorService.execute(() -> {
                final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
                testEnvironment.addToListProperty(SharedTestProperties.MESSAGES_SEND, message);
                sendingActions.send(eventType, message);
            });
        }
    }

    public static <T extends SendingActions & SubscribeActions> Semaphore addABlockingSubscriberAndThenSendXMessagesInEachThread(
            final T sutActions,
            final int numberOfMessages,
            final TestEnvironment testEnvironment) {
        final Semaphore semaphore = new Semaphore(0);
        final BlockingTestSubscriber<TestMessage> subscriber = BlockingTestSubscriber.blockingTestSubscriber(semaphore);
        addABlockingSubscriberAndThenSendXMessagesInEachThread(sutActions, subscriber, numberOfMessages, testEnvironment);
        return semaphore;
    }

    public static <T extends SendingActions & SubscribeActions> Semaphore addABlockingSubscriberAndThenSendXMessagesInEachThread(
            final T sutActions,
            final int numberOfMessages,
            final int expectedNumberOfBlockedThreads,
            final TestEnvironment testEnvironment) {
        final Semaphore semaphore = new Semaphore(0);
        final BlockingTestSubscriber<TestMessage> subscriber = BlockingTestSubscriber.blockingTestSubscriber(semaphore);
        addABlockingSubscriberAndThenSendXMessagesInEachThread(sutActions, subscriber, numberOfMessages,
                1, testEnvironment, expectedNumberOfBlockedThreads);
        return semaphore;
    }

    public static <T extends SendingActions & SubscribeActions> void addABlockingSubscriberAndThenSendXMessagesInEachThread(
            final T sutActions,
            final BlockingTestSubscriber<TestMessage> subscriber,
            final int numberOfMessages,
            final TestEnvironment testEnvironment) {
        addABlockingSubscriberAndThenSendXMessagesInEachThread(sutActions, subscriber, numberOfMessages,
                1, testEnvironment, numberOfMessages);
    }

    public static <T extends SendingActions & SubscribeActions> void addABlockingSubscriberAndThenSendXMessagesInEachThread(
            final T sutActions,
            final BlockingTestSubscriber<TestMessage> subscriber,
            final int numberOfSenders,
            final int numberOfMessagesPerSender,
            final TestEnvironment testEnvironment,
            final int expectedNumberOfBlockedThreads) {
        final EventType eventType = TestEventType.testEventType();
        sutActions.subscribe(eventType, subscriber);
        testEnvironment.setProperty(SharedTestProperties.SINGLE_RECEIVER, subscriber);

        final TestMessageFactory messageFactory = TestMessageFactory.messageFactoryForValidMessages(numberOfMessagesPerSender);
        sendXMessagesAsynchronously(numberOfSenders, messageFactory, eventType, sutActions, testEnvironment, false);
        PollingUtils.pollUntilEquals(subscriber::getNumberOfBlockedThreads, expectedNumberOfBlockedThreads);
    }
}
