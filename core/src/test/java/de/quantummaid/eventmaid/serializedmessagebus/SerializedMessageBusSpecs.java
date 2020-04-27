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

package de.quantummaid.eventmaid.serializedmessagebus;

import de.quantummaid.eventmaid.serializedmessagebus.givenwhenthen.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

import static de.quantummaid.eventmaid.serializedmessagebus.givenwhenthen.Given.given;
import static de.quantummaid.eventmaid.serializedmessagebus.givenwhenthen.SerializedMessageBusActionBuilder.*;
import static de.quantummaid.eventmaid.serializedmessagebus.givenwhenthen.SerializedMessageBusSetupBuilder.aSerializedMessageBus;
import static de.quantummaid.eventmaid.serializedmessagebus.givenwhenthen.SerializedMessageBusValidationBuilder.*;

public interface SerializedMessageBusSpecs {

    //send and receive maps
    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapData(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withAMapSubscriber())
                .when(aMapDataIsSend())
                .then(expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapDataForACorrelationId(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withAMapSubscriberForACorrelationId())
                .when(aMapDataIsSendForTheGivenCorrelationId())
                .then(expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapDataAndErrorData(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withAMapSubscriber())
                .when(aMapDataWithErrorDataIsSend())
                .then(expectTheDataAndTheErrorToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapDataAndErrorDataForACorrelationId(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withAMapSubscriberForACorrelationId())
                .when(aMapDataWithErrorDataIsSendForTheGivenCorrelationId())
                .then(expectTheDataAndTheErrorToBeReceived());
    }

    //send and receive objects
    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjects(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withADeserializedSubscriber())
                .when(anObjectIsSend())
                .then(expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjectsForACorrelationId(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withADeserializedSubscriberForACorrelationId())
                .when(anObjectIsSendForACorrelationId())
                .then(expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjectAndErrorObject(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withADeserializedSubscriber())
                .when(anObjectDataWithErrorDataIsSend())
                .then(expectTheDataAndTheErrorToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjectAndErrorObjectForACorrelationId(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withADeserializedSubscriberForACorrelationId())
                .when(anObjectDataWithErrorDataIsSendForAGivenCorrelationId())
                .then(expectTheDataAndTheErrorToBeReceived());
    }

    //invokeAndWait map
    @Test
    default void testSerializedMessageBus_canWaitForMapResult(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(aMapIsSendAndTheResultIsWaited())
                .then(expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForMapResultWithTimeout(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForErrorResponse(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberSendingDataBackAsErrorResponse())
                .when(aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(expectTheSendDataToBeReturnedAsErrorData());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitIsStoppedByTimeOutWhenNoResultIsReceived(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config))
                .when(aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(expectTheTimeoutToBeOccurred());
    }

    //invokeAndWait objects
    @Test
    default void testSerializedMessageBus_canWaitForObjectResult(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(anObjectIsSendAndTheResultIsWaited())
                .then(expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForObjectResultWithTimeout(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(anObjectIsSendAndTheResultIsWaitedWithTimeout())
                .then(expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForObjectErrorResponse(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberSendingDataBackAsErrorResponse())
                .when(anObjectIsSendAndTheResultIsWaited())
                .then(expectTheSendDataToBeReturnedAsErrorData());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitDeserializedIsStoppedByTimeOutWhenNoResultIsReceived(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config))
                .when(anObjectIsSendAndTheResultIsWaitedWithTimeout())
                .then(expectTheTimeoutToBeOccurred());
    }

    //invokeAndWait serializedOnly
    @Test
    default void testSerializedMessageBus_canWaitForSerializedOnlyVersion(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(anObjectIsSendAndTheNotSerializedResultIsWaited())
                .then(expectToHaveWaitedUntilTheNotSerializedResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForSerializedOnlyVersionResultWithTimeout(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(expectToHaveWaitedUntilTheNotSerializedResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForErrorResponseInSerializedOnlyVersion(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberSendingDataBackAsErrorResponse())
                .when(anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(expectTheSendDataToBeReturnedAsNotSerializedErrorData());
    }

    @Test
    default void testSerializedMessageBus_serializedOnyVersionIsStoppedByTimeOutWhenNoResultIsReceived(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config))
                .when(anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(expectTheTimeoutToBeOccurred());
    }

    //unsubscribe
    @Test
    default void testSerializedMessageBus_canUnsubscribe(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withAMapSubscriber()
                .withADeserializedSubscriber())
                .when(theSubscriberUnsubscribe())
                .then(expectNoRemainingSubscriber());
    }

    //errors: invokeAndWait
    @Test
    default void testSerializedMessageBus_invokeAndWaitBubblesUnderlyingExceptionUp(final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberThrowingError())
                .when(aMapIsSendAndTheResultIsWaited())
                .then(expectAnExecutionExceptionWithTheCorrectCause());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitSerializedOnlyBubblesUnderlyingExceptionUp(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberThrowingError())
                .when(anObjectIsSendAndTheResultIsWaited())
                .then(expectAnExecutionExceptionWithTheCorrectCause());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitDeserializedBubblesUnderlyingExceptionUp(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberThrowingError())
                .when(anObjectIsSendAndTheNotSerializedResultIsWaited())
                .then(expectAnExecutionExceptionWithTheCorrectCause());
    }

    //errors: missing serialization mapping
    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingSerializationMapping_forInvokeAndWaitDeserialized(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config))
                .when(anObjectWithoutKnownSerializationIsSend())
                .then(expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    @Test
    default void testSerializedMessageBus_exceptionIsThrownForMissingSerializationMapping_forInvokeAndWaitDeserializedWithTimeout(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config))
                .when(anObjectWithoutKnownSerializationIsSendWithTimeout())
                .then(expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingSerializationMapping_forInvokeAndSerializeOnly(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config))
                .when(anObjectWithoutKnownSerializationIsSendForInvokeAndSerializeOnly())
                .then(expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingSerializationMapping_forInvokeAndSerializeOnlyWithTimeout(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config))
                .when(aObjectWithoutKnownSerializationIsSendForInvokeAndSerializeOnlyWithTimeout())
                .then(expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    //errors: missing deserialization mapping
    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingDeserializationMapping_forInvokeAndWaitDeserialized(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(anObjectWithoutKnownReturnValueDeserializationIsSend())
                .then(expectAnExecutionExceptionFor(TestMissingDeserializationException.class));
    }

    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingDeserializationMapping_forInvokeAndSerializeOnly(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(anObjectWithoutKnownReturnValueDeserializationIsSendWithTimeout())
                .then(expectAnExecutionExceptionFor(TestMissingDeserializationException.class));
    }

    @Test
    default void testSerializedMessageBus_failsIfTheDeserializationClassForAnErrorResponseIsMissing(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config)
                .withASubscriberSendingDataBackAsErrorResponse())
                .when(anObjectWithoutSpecifyingClassOfResponseErrorClassIsSendWithTimeout())
                .then(expectAnExecutionExceptionFor(MissingErrorPayloadClassForDeserializationException.class));
    }

    //errors: timeout when no response
    @Test
    default void testSerializedMessageBus_invokeAndWaitThrowsTimeoutExceptionWhenNoResponseIsReceived(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config))
                .when(aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(expectTheException(TimeoutException.class));
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitSerializedOnlyThrowsTimeoutExceptionWhenNoResponseIsReceived(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config))
                .when(anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(expectTheException(TimeoutException.class));
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitDeserializedOnlyThrowsTimeoutExceptionWhenNoResponseIsReceived(
            final SerializedMessageBusTestConfig config) {
        given(aSerializedMessageBus(config))
                .when(anObjectIsSendAndTheResultIsWaitedWithTimeout())
                .then(expectTheException(TimeoutException.class));
    }

}
