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

package de.quantummaid.eventmaid.qcec.querying.config;

import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.qcec.queryresolving.Query;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class TestQueryResolver {
    private final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();

    public abstract <R> Optional<R> executeQuery(Query<R> query);

    public abstract <R> R executeRequiredQuery(Query<R> query);

    public abstract <T extends Query<?>> SubscriptionId subscribing(Class<T> queryClass, Consumer<T> consumer);

    public abstract <T extends Query<?>> TestQueryResolver withASubscriber(Class<T> queryClass, Consumer<T> consumer);

    public abstract void unsubscribe(SubscriptionId subscriptionId);

    public TestEnvironment getEnvironment() {
        return testEnvironment;
    }
}
