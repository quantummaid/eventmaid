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

public interface SerializedMessageBusSpecs {

    //send and receive maps
    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapData(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withAMapSubscriber())
                .when(SerializedMessageBusActionBuilder.aMapDataIsSend())
                .then(SerializedMessageBusValidationBuilder.expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapDataForACorrelationId(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withAMapSubscriberForACorrelationId())
                .when(SerializedMessageBusActionBuilder.aMapDataIsSendForTheGivenCorrelationId())
                .then(SerializedMessageBusValidationBuilder.expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapDataAndErrorData(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withAMapSubscriber())
                .when(SerializedMessageBusActionBuilder.aMapDataWithErrorDataIsSend())
                .then(SerializedMessageBusValidationBuilder.expectTheDataAndTheErrorToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveMapDataAndErrorDataForACorrelationId(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withAMapSubscriberForACorrelationId())
                .when(SerializedMessageBusActionBuilder.aMapDataWithErrorDataIsSendForTheGivenCorrelationId())
                .then(SerializedMessageBusValidationBuilder.expectTheDataAndTheErrorToBeReceived());
    }

    //send and receive objects
    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjects(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withADeserializedSubscriber())
                .when(SerializedMessageBusActionBuilder.anObjectIsSend())
                .then(SerializedMessageBusValidationBuilder.expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjectsForACorrelationId(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withADeserializedSubscriberForACorrelationId())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendForACorrelationId())
                .then(SerializedMessageBusValidationBuilder.expectTheCorrectDataToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjectAndErrorObject(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withADeserializedSubscriber())
                .when(SerializedMessageBusActionBuilder.anObjectDataWithErrorDataIsSend())
                .then(SerializedMessageBusValidationBuilder.expectTheDataAndTheErrorToBeReceived());
    }

    @Test
    default void testSerializedMessageBus_canSendAndReceiveObjectAndErrorObjectForACorrelationId(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withADeserializedSubscriberForACorrelationId())
                .when(SerializedMessageBusActionBuilder.anObjectDataWithErrorDataIsSendForAGivenCorrelationId())
                .then(SerializedMessageBusValidationBuilder.expectTheDataAndTheErrorToBeReceived());
    }

    //invokeAndWait map
    @Test
    default void testSerializedMessageBus_canWaitForMapResult(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.aMapIsSendAndTheResultIsWaited())
                .then(SerializedMessageBusValidationBuilder.expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForMapResultWithTimeout(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForErrorResponse(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withASubscriberSendingDataBackAsErrorResponse())
                .when(SerializedMessageBusActionBuilder.aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectTheSendDataToBeReturnedAsErrorData());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitIsStoppedByTimeOutWhenNoResultIsReceived(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config))
                .when(SerializedMessageBusActionBuilder.aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectTheTimeoutToBeOccurred());
    }

    //invokeAndWait objects
    @Test
    default void testSerializedMessageBus_canWaitForObjectResult(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheResultIsWaited())
                .then(SerializedMessageBusValidationBuilder.expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForObjectResultWithTimeout(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheResultIsWaitedWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectToHaveWaitedUntilTheCorrectResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForObjectErrorResponse(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withASubscriberSendingDataBackAsErrorResponse())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheResultIsWaited())
                .then(SerializedMessageBusValidationBuilder.expectTheSendDataToBeReturnedAsErrorData());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitDeserializedIsStoppedByTimeOutWhenNoResultIsReceived(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config))
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheResultIsWaitedWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectTheTimeoutToBeOccurred());
    }

    //invokeAndWait serializedOnly
    @Test
    default void testSerializedMessageBus_canWaitForSerializedOnlyVersion(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheNotSerializedResultIsWaited())
                .then(SerializedMessageBusValidationBuilder.expectToHaveWaitedUntilTheNotSerializedResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForSerializedOnlyVersionResultWithTimeout(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(SerializedMessageBusValidationBuilder.expectToHaveWaitedUntilTheNotSerializedResponseWasReceived());
    }

    @Test
    default void testSerializedMessageBus_canWaitForErrorResponseInSerializedOnlyVersion(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withASubscriberSendingDataBackAsErrorResponse())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(SerializedMessageBusValidationBuilder.expectTheSendDataToBeReturnedAsNotSerializedErrorData());
    }

    @Test
    default void testSerializedMessageBus_serializedOnyVersionIsStoppedByTimeOutWhenNoResultIsReceived(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config))
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(SerializedMessageBusValidationBuilder.expectTheTimeoutToBeOccurred());
    }

    //unsubscribe
    @Test
    default void testSerializedMessageBus_canUnsubscribe(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withAMapSubscriber()
                .withADeserializedSubscriber())
                .when(SerializedMessageBusActionBuilder.theSubscriberUnsubscribe())
                .then(SerializedMessageBusValidationBuilder.expectNoRemainingSubscriber());
    }

    //errors: invokeAndWait
    @Test
    default void testSerializedMessageBus_invokeAndWaitBubblesUnderlyingExceptionUp(final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withASubscriberThrowingError())
                .when(SerializedMessageBusActionBuilder.aMapIsSendAndTheResultIsWaited())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionWithTheCorrectCause());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitSerializedOnlyBubblesUnderlyingExceptionUp(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withASubscriberThrowingError())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheResultIsWaited())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionWithTheCorrectCause());
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitDeserializedBubblesUnderlyingExceptionUp(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withASubscriberThrowingError())
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheNotSerializedResultIsWaited())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionWithTheCorrectCause());
    }

    //errors: missing serialization mapping
    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingSerializationMapping_forInvokeAndWaitDeserialized(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config))
                .when(SerializedMessageBusActionBuilder.anObjectWithoutKnownSerializationIsSend())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    @Test
    default void testSerializedMessageBus_exceptionIsThrownForMissingSerializationMapping_forInvokeAndWaitDeserializedWithTimeout(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config))
                .when(SerializedMessageBusActionBuilder.anObjectWithoutKnownSerializationIsSendWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingSerializationMapping_forInvokeAndSerializeOnly(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config))
                .when(SerializedMessageBusActionBuilder.anObjectWithoutKnownSerializationIsSendForInvokeAndSerializeOnly())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingSerializationMapping_forInvokeAndSerializeOnlyWithTimeout(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config))
                .when(SerializedMessageBusActionBuilder.aObjectWithoutKnownSerializationIsSendForInvokeAndSerializeOnlyWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionFor(TestMissingSerializationException.class));
    }

    //errors: missing deserialization mapping
    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingDeserializationMapping_forInvokeAndWaitDeserialized(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.anObjectWithoutKnownReturnValueDeserializationIsSend())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionFor(TestMissingDeserializationException.class));
    }

    @Test
    default void testSerializedMessageBus_anExceptionIsThrownForMissingDeserializationMapping_forInvokeAndSerializeOnly(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config)
                .withASubscriberSendingCorrelatedResponse())
                .when(SerializedMessageBusActionBuilder.anObjectWithoutKnownReturnValueDeserializationIsSendWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectAnExecutionExceptionFor(TestMissingDeserializationException.class));
    }

    //errors: timeout when no response
    @Test
    default void testSerializedMessageBus_invokeAndWaitThrowsTimeoutExceptionWhenNoResponseIsReceived(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config))
                .when(SerializedMessageBusActionBuilder.aMapIsSendAndTheResultIsWaitedWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectTheException(TimeoutException.class));
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitSerializedOnlyThrowsTimeoutExceptionWhenNoResponseIsReceived(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config))
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut())
                .then(SerializedMessageBusValidationBuilder.expectTheException(TimeoutException.class));
    }

    @Test
    default void testSerializedMessageBus_invokeAndWaitDeserializedOnlyThrowsTimeoutExceptionWhenNoResponseIsReceived(
            final SerializedMessageBusTestConfig config) {
        Given.given(SerializedMessageBusSetupBuilder.aSerializedMessageBus(config))
                .when(SerializedMessageBusActionBuilder.anObjectIsSendAndTheResultIsWaitedWithTimeout())
                .then(SerializedMessageBusValidationBuilder.expectTheException(TimeoutException.class));
    }

}
