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

package de.quantummaid.messagemaid.qcec.domainBus;

import de.quantummaid.messagemaid.messageBus.MessageBus;
import de.quantummaid.messagemaid.qcec.constraintEnforcing.ConstraintEnforcer;
import de.quantummaid.messagemaid.qcec.eventBus.EventBus;
import de.quantummaid.messagemaid.qcec.queryresolving.QueryResolver;
import de.quantummaid.messagemaid.internal.enforcing.NotNullEnforcer;
import de.quantummaid.messagemaid.qcec.constraintEnforcing.ConstraintEnforcerFactory;
import de.quantummaid.messagemaid.qcec.queryresolving.QueryResolverFactory;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.messagemaid.messageBus.MessageBusBuilder.aMessageBus;
import static de.quantummaid.messagemaid.messageBus.MessageBusType.SYNCHRONOUS;
import static de.quantummaid.messagemaid.qcec.eventBus.EventBusFactory.aEventBus;
import static lombok.AccessLevel.PRIVATE;

/**
 * Builder class to create a new {@code DocumentBus}.
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class DocumentBusBuilder {
    private QueryResolver queryResolver;
    private ConstraintEnforcer constraintEnforcer;
    private EventBus eventBus;

    /**
     * Creates a new {@code DocumentBus} based on a synchronous {@code MessageBus}.
     *
     * @return a new {@code DocumentBus}
     */
    public static DocumentBus aDefaultDocumentBus() {
        final MessageBus queryMessageBus = aMessageBus()
                .forType(SYNCHRONOUS)
                .build();
        final MessageBus constraintMessageBus = aMessageBus()
                .forType(SYNCHRONOUS)
                .build();
        final MessageBus eventMessageBus = aMessageBus()
                .forType(SYNCHRONOUS)
                .build();
        final QueryResolver queryResolver = QueryResolverFactory.aQueryResolver(queryMessageBus);
        final ConstraintEnforcer constraintEnforcer = ConstraintEnforcerFactory.aConstraintEnforcer(constraintMessageBus);
        final EventBus eventBus = aEventBus(eventMessageBus);
        return aDocumentBus()
                .using(queryResolver)
                .using(constraintEnforcer)
                .using(eventBus)
                .build();
    }

    /**
     * Factory method to create a new {@code DocumentBusBuilder}.
     *
     * @return newly created {@code DocumentBusBuilder}
     */
    public static DocumentBusBuilder aDocumentBus() {
        return new DocumentBusBuilder();
    }

    /**
     * Sets the {@code QueryResolver} to be used for the {@code DocumentBus}.
     *
     * @param queryResolver the {@code QueryResolver} to be used
     * @return the same {@code DocumentBusBuilder} instance the method was called one
     */
    public DocumentBusBuilder using(final QueryResolver queryResolver) {
        this.queryResolver = queryResolver;
        return this;
    }

    /**
     * Sets the {@code ConstraintEnforcer} to be used for the {@code DocumentBus}.
     *
     * @param constraintEnforcer the {@code ConstraintEnforcer} to be used
     * @return the same {@code DocumentBusBuilder} instance the method was called one
     */
    public DocumentBusBuilder using(final ConstraintEnforcer constraintEnforcer) {
        this.constraintEnforcer = constraintEnforcer;
        return this;
    }

    /**
     * Sets the {@code EventBus} to be used for the {@code DocumentBus}.
     *
     * @param eventBus the {@code EventBus} to be used
     * @return the same {@code DocumentBusBuilder} instance the method was called one
     */
    public DocumentBusBuilder using(final EventBus eventBus) {
        this.eventBus = eventBus;
        return this;
    }

    /**
     * Creates the configured {@code DocumentBus}.
     *
     * @return newly created {@code DocumentBus}
     */
    public DocumentBusImpl build() {
        NotNullEnforcer.ensureNotNull(queryResolver, "query resolver");
        NotNullEnforcer.ensureNotNull(constraintEnforcer, "constraint enforcer");
        NotNullEnforcer.ensureNotNull(eventBus, "event bus");
        return new DocumentBusImpl(queryResolver, constraintEnforcer, eventBus);
    }
}
