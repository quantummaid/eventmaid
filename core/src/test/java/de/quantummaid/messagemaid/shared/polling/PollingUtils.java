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

package de.quantummaid.messagemaid.shared.polling;

import de.quantummaid.messagemaid.shared.validations.SharedTestValidations;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PollingUtils {
    public static void pollUntil(final BooleanSupplier condition) {
        final TimeoutPoller poller = TimeoutPoller.timeoutPoller();
        poller.poll(condition);
    }

    public static void pollUntilEquals(final Supplier<Object> actualSupplier, final Object expected) {
        final TimeoutPoller poller = TimeoutPoller.timeoutPoller();
        final String exceptionMessage = "Actual: " + actualSupplier.get() + ", Expected: " + expected;
        poller.poll(() -> {
            final Object actual = actualSupplier.get();
            return SharedTestValidations.testEquals(actual, expected);
        }, exceptionMessage);
    }

    public static void pollUntilListHasSize(final Supplier<List<?>> listSupplier, final Object expected) {
        pollUntilEquals(() -> listSupplier.get().size(), expected);
    }

    public static void pollUntilListHasSize(final List<?> list, final Object expected) {
        pollUntilEquals(list::size, expected);
    }
}
