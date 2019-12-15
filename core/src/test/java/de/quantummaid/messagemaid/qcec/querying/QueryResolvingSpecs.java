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

package de.quantummaid.messagemaid.qcec.querying;

import de.quantummaid.messagemaid.qcec.querying.config.TestQueryResolver;
import de.quantummaid.messagemaid.qcec.shared.testQueries.SpecificQuery;
import de.quantummaid.messagemaid.qcec.shared.testQueries.TestQuery;
import de.quantummaid.messagemaid.qcec.querying.givenWhenThen.Given;
import de.quantummaid.messagemaid.qcec.querying.givenWhenThen.QueryActionBuilder;
import de.quantummaid.messagemaid.qcec.querying.givenWhenThen.QueryValidationBuilder;
import org.junit.jupiter.api.Test;

public interface QueryResolvingSpecs {

    @Test
    default void testQueryResolver_whenResolvingAQuery_aValidResultIsReturned(final TestQueryResolver resolver) {
        Given.given(resolver)
                .when(QueryActionBuilder.aQueryIsExecuted())
                .expect(QueryValidationBuilder.theCorrectResult());
    }

    @Test
    default void testQueryResolver_whenResolvingAQueryWithARequiredResult_aValidResultIsReturned(
            final TestQueryResolver resolver) {
        Given.given(resolver)
                .when(QueryActionBuilder.aQueryIsExecutedThatRequiresAResult())
                .expect(QueryValidationBuilder.theCorrectResult());
    }

    @Test
    default void testQueryResolver_whenResolvingAQueryWithPartialResults_aValidResultIsReturned(
            final TestQueryResolver resolver) {
        Given.given(resolver)
                .when(QueryActionBuilder.aQueryIsExecutedThatCollectsPartialResults())
                .expect(QueryValidationBuilder.theCorrectResult());
    }

    @Test
    default void testQueryResolver_whenResolvingAQueryWithARequiredResult_throwsExceptionWhenNoResultsIsObtained(
            final TestQueryResolver resolver) {
        Given.given(resolver)
                .when(QueryActionBuilder.aQueryIsExecutedThatRequiresAResultButDoesntProvideOne())
                .expect(QueryValidationBuilder.aExceptionWithMessageMatchingRegex("^Expected a query result for query .+$"));
    }

    @Test
    default void testQueryResolver_unsubscribe(final TestQueryResolver resolver) {
        Given.given(resolver)
                .when(QueryActionBuilder.anRecipientIsUnsubscribedBeforeAQueryIsExecuted())
                .expect(QueryValidationBuilder.theCorrectResult());
    }

    @Test
    default void testQueryResolver_queryCanBeStoppedEarly(final TestQueryResolver resolver) {
        final int expectedResult = 5;
        final int invalidResponse = 1000;
        Given.given(resolver
                .withASubscriber(TestQuery.class, q -> {
                    q.setResult(expectedResult);
                    q.finishQuery();
                })
                .withASubscriber(TestQuery.class, q -> q.setResult(invalidResponse)))
                .when(QueryActionBuilder.theQueryIsExecuted(TestQuery.aTestQuery()))
                .expect(QueryValidationBuilder.theResult(expectedResult));
    }

    @Test
    default void testQueryResolver_returnsNoResultWhenExceptionIsThrown(final TestQueryResolver resolver) {
        Given.given(resolver)
                .when(QueryActionBuilder.aQueryIsExecutedThatThrowsAnException())
                .expect(QueryValidationBuilder.theThrownException());
    }

    @Test
    default void testQueryResolver_throwsExceptionWhenExceptionIsThrownButAResultIsExpected(final TestQueryResolver resolver) {
        Given.given(resolver)
                .when(QueryActionBuilder.aQueryIsExecutedThatExpectsAResultButDidThrowAnException())
                .expect(QueryValidationBuilder.aExceptionForNoResultButOneWasRequired());
    }

    @Test
    default void testQueryResolver_allowsDifferentRegisteredQueries(final TestQueryResolver resolver) {
        final int expectedResult = 5;
        Given.given(resolver
                .withASubscriber(TestQuery.class, q -> {
                    q.setResult(expectedResult);
                    q.finishQuery();
                })
                .withASubscriber(SpecificQuery.class, specificQuery -> {
                }))
                .when(QueryActionBuilder.theQueryIsExecuted(TestQuery.aTestQuery()))
                .expect(QueryValidationBuilder.theResult(expectedResult));
    }
}
