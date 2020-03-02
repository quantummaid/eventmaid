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

package de.quantummaid.eventmaid.channel.action;

import lombok.RequiredArgsConstructor;

import static de.quantummaid.eventmaid.channel.action.ActionHandlerSet.emptyActionHandlerSet;
import static de.quantummaid.eventmaid.channel.action.CallActionHandler.callActionHandler;
import static de.quantummaid.eventmaid.channel.action.ConsumerActionHandler.consumerActionHandler;
import static de.quantummaid.eventmaid.channel.action.ReturnActionHandler.returnActionHandler;
import static de.quantummaid.eventmaid.channel.action.SubscriptionActionHandler.subscriptionActionHandler;
import static lombok.AccessLevel.PRIVATE;

/**
 * An {@code ActionHandlerSet}, that contains all built-in {@code Actions} and their {@code ActionHandlers}.
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class DefaultActionHandlerSet {

    /**
     * Creates an {@code ActionHandlerSet} with all built-in {@code Actions} and their {@code ActionHandlers}.
     *
     * @param <T> the type of messages of the {@code Channel}
     * @return an {@code ActionHandlerSet} with all default {@code Actions}
     */
    public static <T> ActionHandlerSet<T> defaultActionHandlerSet() {
        final ActionHandlerSet<T> actionHandlerSet = emptyActionHandlerSet();
        actionHandlerSet.registerActionHandler(Subscription.class, subscriptionActionHandler());
        actionHandlerSet.registerActionHandler(Consume.class, consumerActionHandler());
        actionHandlerSet.registerActionHandler(Jump.class, JumpActionHandler.jumpActionHandler());
        actionHandlerSet.registerActionHandler(Return.class, returnActionHandler());
        actionHandlerSet.registerActionHandler(Call.class, callActionHandler());
        return actionHandlerSet;
    }
}
