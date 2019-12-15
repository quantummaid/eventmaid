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

package de.quantummaid.messagemaid.qcec.queryresolving;

/**
 * The {@code Queries} interface to be used with the {@code QueryResolver}
 *
 * @param <R> the type of the result
 * @see <a href="https://github.com/quantummaid/messagemaid#queries">MessageMaid Documentation</a>
 */
public interface Query<R> {
    /**
     * Returns the result once the {@code Query} has finished or nur further {@code Subscribers} exist.
     *
     * @return the result
     */
    R result();

    /**
     * Method to preemptive stop the delivery of a {@code Query} to its {@code Subscribers}.
     *
     * @return {@code true} if stop delivery or {@code false} otherwise
     */
    default boolean finished() {
        return false;
    }
}
