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

package de.quantummaid.eventmaid.qcec.domainbus.building;

import java.util.function.Predicate;

/**
 * Step defining, when the subscriber should stop answering the message and should unsubscribe.
 *
 * @param <T> the type of the message
 */
public interface TerminationAnswerBuilder<T> {

    /**
     * Defines the event, on which the the subscription should be terminated.
     *
     * @param eventClass the class of the event, that terminates the subscription
     * @return a builder for the next step in defining the answer to a message
     */
    AnswerStep2Builder<T> until(Class<?> eventClass);

    /**
     * Defines the event and a condition, when the subscription should be terminated.
     *
     * @param eventClass the class of of the terminating event
     * @param condition  the condition, that should hold
     * @param <R>        the type of the terminating event
     * @return a builder for the next step in defining the answer to a message
     */
    <R> AnswerStep2Builder<T> until(Class<R> eventClass, Predicate<R> condition);
}
