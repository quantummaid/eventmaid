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

package de.quantummaid.eventmaid.qcec.documentBus;

import de.quantummaid.eventmaid.qcec.documentBus.givenWhenThen.Given;
import de.quantummaid.eventmaid.qcec.documentBus.givenWhenThen.TestDocumentBusBuilder;
import org.junit.jupiter.api.Test;

import static de.quantummaid.eventmaid.qcec.documentBus.givenWhenThen.DocumentBusActionBuilder.*;
import static de.quantummaid.eventmaid.qcec.documentBus.givenWhenThen.DocumentBusValidationBuilder.*;

//Most of the Query/Constraint/Event specific stuff is tested in the respective Specs
public class DocumentBusSpecs {

    //queries
    @Test
    public void testDocumentBus_canUnsubscribeQueriesAfterASpecificEventIsReceived() {
        Given.given(TestDocumentBusBuilder.aDocumentBus()
                .withSeveralQuerySubscriberThatUnsubscribeAfterASpecificEventWasReceived())
                .when(aQueryTheEventAndASecondQueryAreSend())
                .then(expectNoQueryAfterTheEventToHaveAResult());
    }

    @Test
    public void testDocumentBus_subscriberCanFilterForQueries() {
        Given.given(TestDocumentBusBuilder.aDocumentBus()
                .withSeveralSubscriberThatOnlyTakeSpecificQueries())
                .when(oneQueryOfInterestAndSeveralOtherAreSend())
                .then(expectOnlyQueriesOfInterestToBeReceived());
    }

    //constraints
    @Test
    public void testDocumentBus_canUnsubscribeConstraintsAfterASpecificEventIsReceived() {
        Given.given(TestDocumentBusBuilder.aDocumentBus()
                .withSeveralConstraintSubscriberThatUnsubscribeAfterASpecificEventWasReceived())
                .when(aConstraintTheEventAndASecondConstraintAreSend())
                .then(expectOnlyTheFirstConstraintToBeReceived());
    }

    @Test
    public void testDocumentBus_subscriberCanFilterForConstraints() {
        Given.given(TestDocumentBusBuilder.aDocumentBus()
                .withSeveralSubscriberThatOnlyTakeSpecificConstraints())
                .when(oneConstraintOfInterestAndSeveralOtherAreSend())
                .then(expectOnlyConstraintsOfInterestToBeReceived());
    }

    //events
    @Test
    public void testDocumentBus_canUnsubscribeEventsAfterASpecificEventIsReceived() {
        Given.given(TestDocumentBusBuilder.aDocumentBus()
                .withSeveralEventSubscriberThatUnsubscribeAfterASpecificEventWasReceived())
                .when(anEventThenTheUnsubscribeEventAndAThirdEventAreSend())
                .then(expectOnlyTheFirstEventToBeReceived());
    }

    @Test
    public void testDocumentBus_subscriberCanFilterForEvents() {
        Given.given(TestDocumentBusBuilder.aDocumentBus()
                .withSeveralSubscriberThatOnlyTakeSpecificEvents())
                .when(oneEventOfInterestAndSeveralOtherAreSend())
                .then(expectOnlyEventsOfInterestToBeReceived());
    }

    @Test
    public void testDocumentBus_theConsumerIsCalledForTheUnsubscribingEventBeforeItIsRemoved() {
        Given.given(TestDocumentBusBuilder.aDocumentBus()
                .withASubscriberForTheUnscubscribeEvent())
                .when(anEventIsSend())
                .then(expectTheConsumerToBeStillExecuted());
    }
}
