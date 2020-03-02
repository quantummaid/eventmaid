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

package de.quantummaid.eventmaid.messageFunction;

import de.quantummaid.eventmaid.exceptions.AlreadyClosedException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static de.quantummaid.eventmaid.messageFunction.givenWhenThen.Given.given;
import static de.quantummaid.eventmaid.messageFunction.givenWhenThen.TestMessageFunctionActionBuilder.*;
import static de.quantummaid.eventmaid.messageFunction.givenWhenThen.TestMessageFunctionSetupBuilder.aMessageFunction;
import static de.quantummaid.eventmaid.messageFunction.givenWhenThen.TestMessageFunctionValidationBuilder.*;

public class MessageFunctionSpecs {

    @Test
    public void testMessageFunction_obtainsResponseForRequest() {
        given(aMessageFunction()
                .withTheRequestAnsweredByACorrelatedResponse())
                .when(aRequestIsSend())
                .then(expectTheResponseToBeReceived());
    }

    @Test
    public void testMessageFunction_canDifferentiateBetweenDifferentResponses() {
        given(aMessageFunction()
                .withTheRequestAnsweredByACorrelatedResponse())
                .when(severalRequestsAreSend())
                .then(expectCorrectResponseReceivedForEachRequest());
    }

    @Test
    public void testMessageFunction_fullFillsOnlyForCorrectResponse() {
        given(aMessageFunction()
                .acceptingTwoDifferentResponsesForTheTestRequest())
                .when(twoRequestsAreSendThatWithOneOfEachResponsesAnswered())
                .then(expectCorrectTheResponseToBeReceived());
    }

    @Test
    public void testMessageFunction_canGetErrorResponse() {
        given(aMessageFunction()
                .withTheRequestAnsweredByAErrorResponse())
                .when(aRequestIsSend())
                .then(expectTheFutureToHaveAccessToTheErrorResponse());
    }

    @Test
    public void testMessageFunction_canGetReceivedProcessingContext() {
        given(aMessageFunction()
                .withTheRequestAnsweredByANormalAndAErrorResponse())
                .when(aRequestIsSend())
                .then(expectTheProcessingContextToBeReceived());
    }

    @Test
    public void testMessageFunction_futureIsOnlyFulfilledOnce_forRedundantMessage() {
        given(aMessageFunction()
                .withFulfillingResponseSendTwice())
                .when(aFollowUpActionExecutingOnlyOnceIsAddedBeforeRequest())
                .then(expectTheFutureToBeFulFilledOnlyOnce());
    }

    @Test
    public void testMessageFunction_futureIsOnlyFulfilledOnce_forMessageAndException() {
        given(aMessageFunction()
                .withRequestAnsweredByResponseThenByException())
                .when(aFollowUpActionExecutingOnlyOnceIsAddedBeforeRequest())
                .then(expectTheFutureToBeFulFilledOnlyOnce());
    }

    @Test
    public void testMessageFunction_futureIsOnlyFulfilledOnce_forExceptionAndMessage() {
        given(aMessageFunction()
                .withRequestAnsweredByExceptionThenByMessage())
                .when(aFollowUpActionExecutingOnlyOnceIsAddedBeforeRequest())
                .then(expectTheFutureToBeFulFilledOnlyOnce());
    }

    @Test
    public void testMessageFunction_executesFollowUpWhenFuturesIsFulfilled() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(aFollowUpActionIsAddedBeforeSend())
                .then(expectTheFollowUpToBeExecuted());
    }

    @Test
    public void testMessageFunction_getsAccessToExceptionInFollowUp() {
        given(aMessageFunction()
                .definedWithResponseThrowingAnException())
                .when(aFollowUpActionForAnExceptionIsAdded())
                .then(expectAExceptionToBeReceivedInFollowUp());
    }

    @Test
    public void testMessageFunction_futuresFinishesWhenExceptionReceived() {
        given(aMessageFunction()
                .definedWithResponseThrowingAnException())
                .when(aRequestIsSendThatCausesAnException())
                .then(expectAFutureToBeFinishedWithException(ExecutionException.class));
    }

    @Test
    public void testMessageFunction_isFulFilledForNullResponse() {
        given(aMessageFunction()
                .withTheRequestAnsweredByANull())
                .when(aRequestIsSend())
                .then(expectNullReceived());
    }

    // timeout
    @Test
    public void testMessageFunction_getWaitsForTimeout() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(forTheResponseIsWaitedASpecificTime())
                .then(expectTheTimeoutToBeTriggeredAtTheCorrectTime());
    }

    //cancelling
    @Test
    public void testMessageFunction_canCancelAResponse() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(aRequestIsCancelled())
                .then(expectTheRequestToBeCancelledAndNoFollowUpActionToBeExecuted());
    }

    @Test
    public void testMessageFunction_canCancelAResponseSeveralTimes() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(aRequestIsCancelledSeveralTimes())
                .then(expectTheRequestToBeCancelledAndNoFollowUpActionToBeExecuted());
    }

    @Test
    public void testMessageFunction_cancellationAlwaysReturnsTheSameResult() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(aRequestIsCancelledSeveralTimes())
                .then(expectAllCancellationsToHaveReturnedTheSameResult());
    }

    @Test
    public void testMessageFunction_interruptsOtherWaitingThreads() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(aRequestsIsCancelledWhileOtherThreadsWait())
                .then(expectTheRequestToBeCancelledAndNoFollowUpActionToBeExecuted());
    }

    @Test
    public void testMessageFunction_throwsExceptionWhenResultOfACancelledResponseIsRequested() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(theResultOfACancelledRequestIsTaken())
                .then(expectAExceptionToBeThrownOfType(CancellationException.class));
    }

    @Test
    public void testMessageFunction_cancellingAFulfilled() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(theFutureIsFulfilledAndThenCancelled())
                .then(expectTheCancellationToFailed());
    }

    @Test
    public void testMessageFunction_aResponseAfterACancellationDoesNotExecuteFollowUpAction() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(aResponseToACancelledRequestDoesNotExecuteFollowUpAction())
                .then(expectTheRequestToBeCancelledAndNoFollowUpActionToBeExecuted());
    }

    @Test
    public void testMessageFunction_addingAFollowUpActionToACancelledFutureFails() {
        given(aMessageFunction()
                .withTheRequestAnsweredByACorrelatedResponse())
                .when(aFollowUpActionIsAddedToACancelledFuture())
                .then(expectAExceptionToBeThrownOfType(CancellationException.class));
    }

    //Closing
    @Test
    public void testMessageFunction_usingAClosedMessageFunctionFails() {
        given(aMessageFunction()
                .withTheRequestAnsweredByACorrelatedResponse())
                .when(theMessageFunctionIsClosed()
                        .andThen(aRequestIsSend()))
                .then(expectAExceptionToBeThrownOfType(AlreadyClosedException.class));
    }

    //errors
    @Test
    public void testMessageFunction_errorOfMessageBusSendIsNotPropagatedToCallerOfMessageFunction() {
        given(aMessageFunction()
                .throwingAnExceptionDuringSend())
                .when(aFollowUpExpectingExceptionIsAdded())
                .then(expectTheExceptionToBeSetOnlyDuringByFuture());
    }


    //cleaning up
    @Test
    public void testMessageFunction_unregistersAllSubscriber_whenFulfilled() {
        given(aMessageFunction()
                .withTheRequestAnsweredByACorrelatedResponse())
                .when(aRequestIsSend())
                .then(expectNoUnnecssarySubscribersOnTheMessageBus());
    }

    @Test
    public void testMessageFunction_unregistersAllSubscriber_whenExceptionIsReceived() {
        given(aMessageFunction()
                .definedWithResponseThrowingAnException())
                .when(aRequestIsSend())
                .then(expectNoUnnecssarySubscribersOnTheMessageBus());
    }

    @Test
    public void testMessageFunction_unregistersAllSubscriber_whenCancelled() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(aRequestIsCancelled())
                .then(expectNoUnnecssarySubscribersOnTheMessageBus());
    }
}
