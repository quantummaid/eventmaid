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

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code ActionHandlerSet} defines the mapping of {@code Actions} to their respective {@code ActionHandler}.
 *
 * <p>Each {@code Channel} has an {@code ActionHandlerSet} set during creation. Only those {@code Actions} can be used as
 * final {@code Action} of the {@code Channel}, that have a matching {@code ActionHandler} registered in the set. </p>
 *
 * @param <T> the type of messages of the {@code Channel}
 * @see <a href="https://github.com/quantummaid/eventmaid#custom-actions">EventMaid Documentation</a>
 */
public final class ActionHandlerSet<T> {

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends Action>, ActionHandler<? extends Action<T>, T>> actionHandlerMap;

    @SuppressWarnings("rawtypes")
    private ActionHandlerSet(final Map<Class<? extends Action>, ActionHandler<? extends Action<T>, T>> actionHandlerMap) {
        this.actionHandlerMap = actionHandlerMap;
    }

    /**
     * Creates a new, empty {@code ActionHandlerSet}.
     *
     * @param <T> the type of messages of the {@code Channel}
     * @return a new, empty {@code ActionHandlerSet}
     */
    @SuppressWarnings("rawtypes")
    public static <T> ActionHandlerSet<T> emptyActionHandlerSet() {
        final Map<Class<? extends Action>, ActionHandler<? extends Action<T>, T>> map = new HashMap<>();
        return new ActionHandlerSet<>(map);
    }

    /**
     * Returns the {@code ActionHandler} registered for the given action.
     *
     * @param action the action for which the handler is queried
     * @return the {@code ActionHandler} if one is present
     * @throws NoHandlerForUnknownActionException if no {@code ActionHandler} for the {@code Action} exists
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ActionHandler<Action<T>, T> getActionHandlerFor(final Action<T> action) {
        final Class<? extends Action> actionClass = action.getClass();
        final ActionHandler<?, T> actionHandler = actionHandlerMap.get(actionClass);
        if (actionHandler != null) {
            return (ActionHandler<Action<T>, T>) actionHandler;
        } else {
            throw new NoHandlerForUnknownActionException(action);
        }
    }

    /**
     * Method, that can be used to add an {@code Action} and its {@code ActionHandler} dynamically to the set. Can be used
     * to overwrite existing mappings.
     *
     * @param actionClass the {@code Action} for which the handler should be added
     * @param actionHandler the {@code ActionHandler}
     */
    @SuppressWarnings("rawtypes")
    public void registerActionHandler(final Class<? extends Action> actionClass,
                                      final ActionHandler<? extends Action<T>, T> actionHandler) {
        actionHandlerMap.put(actionClass, actionHandler);
    }
}
