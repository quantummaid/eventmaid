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

package de.quantummaid.eventmaid.qcec.shared.testQueries;

import de.quantummaid.eventmaid.qcec.queryresolving.Query;

public final class TestQuery implements Query<Integer> {
    private final boolean returnsResult;
    private boolean isFinished;
    private int result;

    private TestQuery(final boolean returnsResult) {
        this.returnsResult = returnsResult;
    }

    public static TestQuery aTestQuery() {
        return new TestQuery(true);
    }

    public static TestQuery aTestQueryWithoutResult() {
        return new TestQuery(false);
    }

    @Override
    public Integer result() {
        if (returnsResult) {
            return result;
        } else {
            return null;
        }
    }

    @Override
    public boolean finished() {
        return isFinished;
    }

    public void finishQuery() {
        this.isFinished = true;
    }

    public void setResult(final int result) {
        this.result = result;
    }

    public void addPartialResult(final int result) {
        this.result += result;
    }
}
