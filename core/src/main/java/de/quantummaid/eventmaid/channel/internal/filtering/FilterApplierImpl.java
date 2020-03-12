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

package de.quantummaid.eventmaid.channel.internal.filtering;

import de.quantummaid.eventmaid.filtering.Filter;
import de.quantummaid.eventmaid.filtering.FilterActions;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class FilterApplierImpl<T> implements FilterApplier<T> {

    @Override
    public void applyAll(final T message,
                         final List<Filter<T>> filters,
                         final PostFilterActions<T> postFilterActions) {
        if (filters.isEmpty()) {
            postFilterActions.onAllPassed(message);
            return;
        }

        final CurrentFilterActions filterActions = new CurrentFilterActions(filters, postFilterActions);
        final Filter<T> firstFilter = filters.get(0);
        firstFilter.apply(message, filterActions);
        if (filterActions.messageWasForgotten()) {
            postFilterActions.onForgotten(message);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private final class CurrentFilterActions implements FilterActions<T> {
        private final List<Filter<T>> filters;
        private final PostFilterActions<T> postFilterActions;
        private int currentFilterIndex;
        private boolean messageNotForgotten;

        @Override
        public void block(final T message) {
            messageNotForgotten = true;
            postFilterActions.onBlock(message);
        }

        @Override
        public void pass(final T message) {
            if (++currentFilterIndex < filters.size()) {
                final Filter<T> nextFilter = filters.get(currentFilterIndex);
                nextFilter.apply(message, this);
            } else {
                messageNotForgotten = true;
                postFilterActions.onAllPassed(message);
            }
        }

        public boolean messageWasForgotten() {
            return !messageNotForgotten;
        }
    }
}
