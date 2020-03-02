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

package de.quantummaid.eventmaid.channel.givenWhenThen;

import de.quantummaid.eventmaid.shared.exceptions.TestException;
import de.quantummaid.eventmaid.channel.Channel;
import de.quantummaid.eventmaid.channel.ChannelBuilder;
import de.quantummaid.eventmaid.channel.ChannelType;
import de.quantummaid.eventmaid.channel.action.Action;
import de.quantummaid.eventmaid.channel.action.Consume;
import de.quantummaid.eventmaid.channel.action.Jump;
import de.quantummaid.eventmaid.channel.action.Subscription;
import de.quantummaid.eventmaid.channel.config.ChannelTestConfig;
import de.quantummaid.eventmaid.filtering.Filter;
import de.quantummaid.eventmaid.configuration.AsynchronousConfiguration;
import de.quantummaid.eventmaid.processingContext.ProcessingContext;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.subscriber.SimpleTestSubscriber;
import de.quantummaid.eventmaid.shared.testMessages.TestMessage;
import de.quantummaid.eventmaid.shared.utils.FilterTestUtils;

import java.util.List;

import static de.quantummaid.eventmaid.channel.ChannelBuilder.aChannel;
import static de.quantummaid.eventmaid.channel.ChannelBuilder.aChannelWithDefaultAction;
import static de.quantummaid.eventmaid.channel.action.Call.prepareACall;
import static de.quantummaid.eventmaid.channel.action.Consume.consumeMessage;
import static de.quantummaid.eventmaid.channel.action.Jump.jumpTo;
import static de.quantummaid.eventmaid.channel.action.Return.aReturn;
import static de.quantummaid.eventmaid.channel.action.Subscription.subscription;
import static de.quantummaid.eventmaid.channel.givenWhenThen.FilterPosition.*;
import static de.quantummaid.eventmaid.channel.givenWhenThen.TestChannelErrorHandler.*;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironment.emptyTestEnvironment;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.*;
import static de.quantummaid.eventmaid.shared.properties.SharedTestProperties.*;
import static de.quantummaid.eventmaid.shared.subscriber.SimpleTestSubscriber.deliveryPreemptingSubscriber;
import static de.quantummaid.eventmaid.shared.utils.FilterTestUtils.addSeveralNoopFilter;

public final class ChannelSetupBuilder {
    private final TestEnvironment testEnvironment;
    private final ChannelBuilder<TestMessage> channelBuilder;
    private Channel<TestMessage> alreadyBuiltChannel;

    private ChannelSetupBuilder(final ChannelTestConfig channelTestConfig) {
        this.testEnvironment = emptyTestEnvironment();
        final Consume<TestMessage> noopConsume = consumeMessage(processingContext -> {
        });
        final ChannelType type = channelTestConfig.getType();
        final AsynchronousConfiguration asynchronousConfiguration = channelTestConfig.getAsynchronousConfiguration();
        this.channelBuilder = aChannel(TestMessage.class)
                .forType(type)
                .withAsynchronousConfiguration(asynchronousConfiguration)
                .withDefaultAction(noopConsume);
        final boolean testIsAsynchronous = channelTestConfig.isAsynchronous();
        this.testEnvironment.setProperty(IS_ASYNCHRONOUS, testIsAsynchronous);
    }

    private ChannelSetupBuilder(final TestEnvironment testEnvironment, final Channel<TestMessage> channel) {
        this.testEnvironment = testEnvironment;
        this.channelBuilder = null;
        this.alreadyBuiltChannel = channel;
    }

    public static ChannelSetupBuilder aConfiguredChannel(final ChannelTestConfig channelTestConfig) {
        return new ChannelSetupBuilder(channelTestConfig);
    }

    public static ChannelSetupBuilder threeChannelsConnectedWithJumps(final ChannelTestConfig channelTestConfig) {
        final TestEnvironment testEnvironment = emptyTestEnvironment();
        final Channel<TestMessage> thirdChannel = aChannelWithDefaultAction(consumeAsFinalResult(testEnvironment));
        final Jump<TestMessage> actionSecondChannel = jumpTo(thirdChannel);
        final Channel<TestMessage> secondChannel = aChannelWithDefaultAction(actionSecondChannel);
        final Jump<TestMessage> actionFirstChannel = jumpTo(secondChannel);
        final Channel<TestMessage> firstChannel = aChannel(TestMessage.class)
                .withDefaultAction(actionFirstChannel)
                .forType(channelTestConfig.getType())
                .withAsynchronousConfiguration(channelTestConfig.getAsynchronousConfiguration())
                .build();

        testEnvironment.addToListProperty(ChannelTestProperties.ALL_CHANNELS, firstChannel);
        testEnvironment.addToListProperty(ChannelTestProperties.ALL_CHANNELS, secondChannel);
        testEnvironment.addToListProperty(ChannelTestProperties.ALL_CHANNELS, thirdChannel);
        return new ChannelSetupBuilder(testEnvironment, firstChannel);
    }

    public static ChannelSetupBuilder aChannelCallingASecondThatReturnsBack(final ChannelTestConfig channelTestConfig) {
        final TestEnvironment testEnvironment = emptyTestEnvironment();
        final Channel<TestMessage> thirdChannel = aChannelWithDefaultAction(aReturn());
        final Jump<TestMessage> actionSecondChannel = jumpTo(thirdChannel);
        testEnvironment.setProperty(ChannelTestProperties.RETURNING_CHANNEL, thirdChannel);

        final Channel<TestMessage> secondChannel = aChannelWithDefaultAction(actionSecondChannel);
        testEnvironment.setProperty(ChannelTestProperties.CALL_TARGET_CHANNEL, secondChannel);

        final Channel<TestMessage> firstChannel = aChannel(TestMessage.class)
                .withDefaultAction(consumeAsFinalResult(testEnvironment))
                .forType(channelTestConfig.getType())
                .withAsynchronousConfiguration(channelTestConfig.getAsynchronousConfiguration())
                .build();
        return new ChannelSetupBuilder(testEnvironment, firstChannel);
    }

    public static ChannelSetupBuilder aChannelSetupWithNestedCalls(final ChannelTestConfig channelTestConfig) {
        final TestEnvironment testEnvironment = emptyTestEnvironment();
        final Channel<TestMessage> initialChannel = aChannel(TestMessage.class)
                .withDefaultAction(consumeAsFinalResult(testEnvironment))
                .forType(channelTestConfig.getType())
                .withAsynchronousConfiguration(channelTestConfig.getAsynchronousConfiguration())
                .build();
        final Channel<TestMessage> firstCallTargetChannel = aChannelWithDefaultAction(aReturn());
        ChannelTestActions.addFilterExecutingACall(initialChannel, firstCallTargetChannel);
        testEnvironment.addToListProperty(ChannelTestProperties.CALL_TARGET_CHANNEL, firstCallTargetChannel);

        final Channel<TestMessage> returnChannelAfterSecondCall = aChannelWithDefaultAction(aReturn());
        final Channel<TestMessage> secondCallTargetChannel = aChannelWithDefaultAction(jumpTo(returnChannelAfterSecondCall));
        testEnvironment.addToListProperty(ChannelTestProperties.CALL_TARGET_CHANNEL, secondCallTargetChannel);
        testEnvironment.setProperty(ChannelTestProperties.RETURNING_CHANNEL, returnChannelAfterSecondCall);

        ChannelTestActions.addFilterExecutingACall(firstCallTargetChannel, secondCallTargetChannel);
        return new ChannelSetupBuilder(testEnvironment, initialChannel);
    }

    private static Consume<TestMessage> consumeAsFinalResult(final TestEnvironment testEnvironment) {
        return consumeMessage(processingContext -> testEnvironment.setProperty(RESULT, processingContext));
    }

    public ChannelSetupBuilder withDefaultActionConsume() {
        channelBuilder.withDefaultAction(consumeAsFinalResult(testEnvironment));
        return this;
    }

    public ChannelSetupBuilder withNoopConsumeAsDefaultAction() {
        channelBuilder.withDefaultAction(Consume.consumePayload(testMessage -> {
            //doNothing
        }));
        return this;
    }

    public ChannelSetupBuilder withDefaultActionJumpToDifferentChannel() {
        final Channel<TestMessage> secondChannel = aChannelWithDefaultAction(consumeAsFinalResult(testEnvironment));
        channelBuilder.withDefaultAction(jumpTo(secondChannel));
        return this;
    }

    public ChannelSetupBuilder withDefaultActionReturn() {
        channelBuilder.withDefaultAction(aReturn());
        return this;
    }

    public ChannelSetupBuilder withDefaultActionCall() {
        channelBuilder.withDefaultAction(prepareACall(null));
        return this;
    }

    public ChannelSetupBuilder withSubscriptionAsAction() {
        channelBuilder.withDefaultAction(subscription());
        return this;
    }

    public ChannelSetupBuilder withOnPreemptiveSubscriberAndOneSubscriberThatShouldNeverBeCalled() {
        final Subscription<TestMessage> subscription = subscription();
        final SimpleTestSubscriber<TestMessage> subscriber = deliveryPreemptingSubscriber();
        subscription.addSubscriber(subscriber);
        final SimpleTestSubscriber<TestMessage> subscriberThatShouldNotBeCalled = SimpleTestSubscriber.testSubscriber();
        subscription.addSubscriber(subscriberThatShouldNotBeCalled);
        testEnvironment.setProperty(EXPECTED_RECEIVERS, subscriber);
        testEnvironment.setProperty(ERROR_SUBSCRIBER, subscriberThatShouldNotBeCalled);
        channelBuilder.withDefaultAction(subscription);
        return this;
    }

    public ChannelSetupBuilder withAnUnknownAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        channelBuilder.withDefaultAction(unknownAction);
        return this;
    }

    public ChannelSetupBuilder withAnExceptionInFinalAction() {
        channelBuilder.withDefaultAction(Consume.consumePayload(message -> {
            throw new TestException();
        }));
        return this;
    }

    public ChannelSetupBuilder withAPreFilterThatChangesTheAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChannel = channelBuilder.withDefaultAction(unknownAction)
                .build();
        ChannelTestActions.addActionChangingFilterToPipe(alreadyBuiltChannel, PRE, consumeAsFinalResult(testEnvironment));
        return this;
    }

    public ChannelSetupBuilder withAPreFilterThatBlocksMessages() {
        addFilterThatBlocksMessages(PRE);
        return this;
    }

    public ChannelSetupBuilder withAProcessFilterThatBlocksMessages() {
        addFilterThatBlocksMessages(PROCESS);
        return this;
    }

    public ChannelSetupBuilder withAPostFilterThatBlocksMessages() {
        addFilterThatBlocksMessages(POST);
        return this;
    }

    private void addFilterThatBlocksMessages(final FilterPosition filterPosition) {
        alreadyBuiltChannel = channelBuilder.withDefaultAction(consumeAsFinalResult(testEnvironment))
                .build();
        final ChannelTestActions channelTestActions = ChannelTestActions.channelTestActions(alreadyBuiltChannel);
        FilterTestUtils.addFilterThatBlocksMessages(channelTestActions, filterPosition);
    }

    public ChannelSetupBuilder withAPreFilterThatForgetsMessages() {
        addFilterThatForgetsMessages(PRE);
        return this;
    }

    public ChannelSetupBuilder withAProcessFilterThatForgetsMessages() {
        addFilterThatForgetsMessages(PROCESS);
        return this;
    }

    public ChannelSetupBuilder withAPostFilterThatForgetsMessages() {
        addFilterThatForgetsMessages(POST);
        return this;
    }

    private void addFilterThatForgetsMessages(final FilterPosition filterPosition) {
        alreadyBuiltChannel = channelBuilder.withDefaultAction(consumeAsFinalResult(testEnvironment))
                .build();
        final ChannelTestActions testActions = ChannelTestActions.channelTestActions(alreadyBuiltChannel);
        FilterTestUtils.addFilterThatForgetsMessages(testActions, filterPosition);
    }

    public ChannelSetupBuilder withAProcessFilterThatChangesTheAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChannel = channelBuilder.withDefaultAction(unknownAction)
                .build();
        ChannelTestActions.addActionChangingFilterToPipe(alreadyBuiltChannel, PROCESS, consumeAsFinalResult(testEnvironment));
        return this;
    }

    public ChannelSetupBuilder withAPostFilterThatChangesTheAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChannel = channelBuilder.withDefaultAction(unknownAction)
                .build();
        ChannelTestActions.addActionChangingFilterToPipe(alreadyBuiltChannel, POST, consumeAsFinalResult(testEnvironment));
        return this;
    }

    public ChannelSetupBuilder withSeveralPreFilter() {
        final int[] positions = new int[]{0, 0, 2, 1, 2, 4};
        severalFilterInPipe(positions, PRE);
        return this;
    }

    public ChannelSetupBuilder withSeveralProcessFilter() {
        final int[] positions = new int[]{0, 1, 1, 3, 0, 5};
        severalFilterInPipe(positions, PROCESS);
        return this;
    }

    public ChannelSetupBuilder withSeveralPostFilter() {
        final int[] positions = new int[]{0, 0, 2, 2, 1, 3};
        severalFilterInPipe(positions, POST);
        return this;
    }

    public ChannelSetupBuilder withAPreFilterAtAnInvalidPosition(final int position) {
        addAFilterAtPosition(position, PRE);
        return this;
    }

    public ChannelSetupBuilder withAProcessFilterAtAnInvalidPosition(final int position) {
        addAFilterAtPosition(position, PROCESS);
        return this;
    }

    public ChannelSetupBuilder withAPostFilterAtAnInvalidPosition(final int position) {
        addAFilterAtPosition(position, POST);
        return this;
    }

    private void addAFilterAtPosition(final int position, final FilterPosition filterPosition) {
        try {
            alreadyBuiltChannel = channelBuilder.withDefaultAction(consumeAsFinalResult(testEnvironment))
                    .build();
            final ChannelTestActions testActions = ChannelTestActions.channelTestActions(alreadyBuiltChannel);
            FilterTestUtils.addANoopFilterAtPosition(testActions, filterPosition, position);
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
    }

    public ChannelSetupBuilder withAnErrorThrowingFilter() {
        alreadyBuiltChannel = channelBuilder.withDefaultAction(consumeAsFinalResult(testEnvironment))
                .build();
        final ChannelTestActions testActions = ChannelTestActions.channelTestActions(alreadyBuiltChannel);
        FilterTestUtils.addFilterThatThrowsException(testActions, PROCESS);
        return this;
    }

    private void severalFilterInPipe(final int[] positions, final FilterPosition pipe) {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChannel = channelBuilder.withDefaultAction(unknownAction)
                .build();
        final ChannelTestActions testActions = ChannelTestActions.channelTestActions(alreadyBuiltChannel);
        final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = addSeveralNoopFilter(testActions, positions, pipe);
        testEnvironment.setProperty(EXPECTED_FILTER, expectedFilter);
        testEnvironment.setProperty(FILTER_POSITION, pipe);
    }

    public ChannelSetupBuilder withAnExceptionHandlerIgnoringExceptions() {
        channelBuilder.withChannelExceptionHandler(ignoringChannelExceptionHandler());
        return this;
    }

    public ChannelSetupBuilder withACustomErrorHandler() {
        channelBuilder.withChannelExceptionHandler(exceptionInResultStoringChannelExceptionHandler(testEnvironment));
        return this;
    }

    public ChannelSetupBuilder withAnExceptionHandlerRethrowingExceptions() {
        channelBuilder.withChannelExceptionHandler(errorRethrowingExceptionHandler(testEnvironment));
        return this;
    }

    public ChannelSetupBuilder withAnExceptionCatchingHandler() {
        channelBuilder.withChannelExceptionHandler(catchingChannelExceptionHandler(testEnvironment));
        return this;
    }

    public ChannelSetupBuilder withAnErrorHandlerDeclaringErrorsInDeliveryAsNotDeliveryAborting() {
        channelBuilder.withChannelExceptionHandler(testExceptionIgnoringChannelExceptionHandler(testEnvironment));
        return this;
    }

    public TestEnvironment build() {
        if (alreadyBuiltChannel != null) {
            testEnvironment.setProperty(SUT, alreadyBuiltChannel);
        } else {
            final Channel<TestMessage> channel = channelBuilder.build();
            testEnvironment.setProperty(SUT, channel);
        }
        return testEnvironment;
    }

    private static class UnknownAction implements Action<TestMessage> {
        private static UnknownAction unknownAction() {
            return new UnknownAction();
        }
    }

}
