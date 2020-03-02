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

package de.quantummaid.eventmaid.messageFunction.followup;

import de.quantummaid.eventmaid.messageFunction.ResponseFuture;

/**
 * A {@code FollowUpAction} can be added to a {@link ResponseFuture} to be executed, once the future has be fulfilled.
 *
 * @see <a href="https://github.com/quantummaid/eventmaid#responsefuture">EventMaid Documentation</a>
 */
public interface FollowUpAction {

    /**
     * Method to be called, when the {@code FollowUpAction} is executed.
     *
     * @param response      the received response, or {@code null} of an exception was thrown
     * @param errorResponse the received error response, or {@code null} if an exception was thrown
     * @param exception     the thrown exception or {@code null} otherwise
     */
    void apply(Object response, Object errorResponse, Exception exception);
}
