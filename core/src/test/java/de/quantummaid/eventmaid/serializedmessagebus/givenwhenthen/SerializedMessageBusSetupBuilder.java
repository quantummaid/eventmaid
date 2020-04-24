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

package de.quantummaid.eventmaid.serializedmessagebus.givenwhenthen;

import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.internal.collections.filtermap.FilterMapBuilder;
import de.quantummaid.eventmaid.internal.collections.predicatemap.PredicateMapBuilder;
import de.quantummaid.eventmaid.mapping.Demapifier;
import de.quantummaid.eventmaid.mapping.Deserializer;
import de.quantummaid.eventmaid.mapping.Mapifier;
import de.quantummaid.eventmaid.mapping.Serializer;
import de.quantummaid.eventmaid.messagebus.MessageBus;
import de.quantummaid.eventmaid.processingcontext.EventType;
import de.quantummaid.eventmaid.serializedmessagebus.SerializedMessageBus;
import de.quantummaid.eventmaid.shared.environment.TestEnvironment;
import de.quantummaid.eventmaid.shared.exceptions.TestException;
import de.quantummaid.eventmaid.shared.givenwhenthen.SetupAction;
import de.quantummaid.eventmaid.shared.subscriber.SimpleTestSubscriber;
import de.quantummaid.eventmaid.shared.testmessages.ErrorTestMessage;
import de.quantummaid.eventmaid.shared.testmessages.TestMessageOfInterest;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import de.quantummaid.eventmaid.usecases.payloadanderrorpayload.PayloadAndErrorPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static de.quantummaid.eventmaid.internal.collections.filtermap.FilterMapBuilder.filterMapBuilder;
import static de.quantummaid.eventmaid.internal.collections.predicatemap.PredicateMapBuilder.predicateMapBuilder;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static de.quantummaid.eventmaid.shared.environment.TestEnvironmentProperty.*;
import static de.quantummaid.eventmaid.shared.eventtype.TestEventType.testEventType;
import static de.quantummaid.eventmaid.shared.properties.SharedTestProperties.*;
import static de.quantummaid.eventmaid.shared.subscriber.SimpleTestSubscriber.testSubscriber;
import static de.quantummaid.eventmaid.subscribing.ConsumerSubscriber.consumerSubscriber;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SerializedMessageBusSetupBuilder {
    public static final String PAYLOAD_SERIALIZATION_KEY = "content";
    public static final String ERROR_PAYLOAD_SERIALIZATION_KEY = "error";
    private final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();
    private final List<SetupAction<SerializedMessageBus>> setupActions = new LinkedList<>();
    private final SerializedMessageBusTestConfig testConfig;

    public static SerializedMessageBusSetupBuilder aSerializedMessageBus(final SerializedMessageBusTestConfig testConfig) {
        return new SerializedMessageBusSetupBuilder(testConfig);
    }

    public SerializedMessageBusSetupBuilder withAMapSubscriber() {
        setupActions.add(this::addMapSubscriber);
        return this;
    }

    private void addMapSubscriber(final SerializedMessageBus serializedMessageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final SimpleTestSubscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber =
                testSubscriber();
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);
        final SubscriptionId subscriptionId = serializedMessageBus.subscribe(eventType, subscriber);
        testEnvironment.addToListProperty(USED_SUBSCRIPTION_ID, subscriptionId);
    }

    public SerializedMessageBusSetupBuilder withAMapSubscriberForACorrelationId() {
        setupActions.add((serializedMessageBus, testEnvironment) -> {
            final CorrelationId newUniqueCorrelationId = CorrelationId.newUniqueCorrelationId();
            final CorrelationId correlationId = getOrSetCorrelationId(testEnvironment, newUniqueCorrelationId);
            final SimpleTestSubscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber =
                    testSubscriber();
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
            testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);
            final SubscriptionId subscriptionId = serializedMessageBus.subscribe(correlationId, subscriber);
            testEnvironment.addToListProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public SerializedMessageBusSetupBuilder withADeserializedSubscriber() {
        setupActions.add((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final SimpleTestSubscriber<PayloadAndErrorPayload<TestMessageOfInterest, ErrorTestMessage>> subscriber =
                    testSubscriber();
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
            testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);
            final SubscriptionId subscriptionId = serializedMessageBus
                    .subscribeDeserialized(eventType, subscriber, TestMessageOfInterest.class, ErrorTestMessage.class);
            testEnvironment.addToListProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public SerializedMessageBusSetupBuilder withADeserializedSubscriberForACorrelationId() {
        setupActions.add((serializedMessageBus, testEnvironment) -> {
            final CorrelationId newUniqueCorrelationId = CorrelationId.newUniqueCorrelationId();
            final CorrelationId correlationId = getOrSetCorrelationId(testEnvironment, newUniqueCorrelationId);
            final SimpleTestSubscriber<PayloadAndErrorPayload<TestMessageOfInterest, ErrorTestMessage>> subscriber =
                    testSubscriber();
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
            testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);
            final SubscriptionId subscriptionId = serializedMessageBus
                    .subscribeDeserialized(correlationId, subscriber, TestMessageOfInterest.class, ErrorTestMessage.class);
            testEnvironment.addToListProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public SerializedMessageBusSetupBuilder withASubscriberSendingCorrelatedResponse() {
        setupActions.add((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final SubscriptionId subscriptionId = serializedMessageBus.subscribeRaw(eventType,
                    consumerSubscriber(processingContext -> {
                        final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
                        final Map<String, Object> payload = processingContext.getPayload();
                        serializedMessageBus.send(SerializedMessageBusTestProperties.EVENT_TYPE_WITH_NO_SUBSCRIBERS, payload, correlationId);
                    }));
            testEnvironment.addToListProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public SerializedMessageBusSetupBuilder withASubscriberSendingDataBackAsErrorResponse() {
        setupActions.add((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final SubscriptionId subscriptionId = serializedMessageBus.subscribeRaw(eventType,
                    consumerSubscriber(processingContext -> {
                        final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
                        final Map<String, Object> payload = processingContext.getPayload();
                        serializedMessageBus.send(SerializedMessageBusTestProperties.EVENT_TYPE_WITH_NO_SUBSCRIBERS, null, payload, correlationId);
                    }));
            testEnvironment.addToListProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public SerializedMessageBusSetupBuilder withASubscriberThrowingError() {
        setupActions.add((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final SubscriptionId subscriptionId = serializedMessageBus.subscribe(eventType, consumerSubscriber(ignored -> {
                throw new TestException();
            }));
            testEnvironment.addToListProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public SerializedMessageBusSetup build() {
        final MessageBus messageBus = testConfig.getMessageBus();
        final Deserializer deserializer = getDeserializer();
        final Serializer serializer = getSerializer();
        final SerializedMessageBus serializedMessageBus = SerializedMessageBus
                .aSerializedMessageBus(messageBus, serializer, deserializer);
        setupActions.forEach(a -> a.execute(serializedMessageBus, testEnvironment));
        testEnvironment.setPropertyIfNotSet(SUT, serializedMessageBus);
        testEnvironment.setPropertyIfNotSet(MOCK, messageBus);
        return new SerializedMessageBusSetup(serializedMessageBus, testEnvironment);
    }

    private CorrelationId getOrSetCorrelationId(final TestEnvironment testEnvironment,
                                                final CorrelationId newUniqueCorrelationId) {
        return testEnvironment.getPropertyOrSetDefault(EXPECTED_CORRELATION_ID, newUniqueCorrelationId);
    }

    @SuppressWarnings("unchecked")
    private Deserializer getDeserializer() {
        final FilterMapBuilder<Class<?>, Object, Demapifier<?>> deserializingFilterMapBuilder = filterMapBuilder();
        deserializingFilterMapBuilder
                .put((o, o2) -> o.equals(TestMessageOfInterest.class), (Demapifier) (targetType, map) -> {
                    final String content = (String) ((Map<Object, Object>) map).get(PAYLOAD_SERIALIZATION_KEY);
                    return TestMessageOfInterest.messageOfInterest(content);
                })
                .put((o, o2) -> o.equals(ErrorTestMessage.class), (Demapifier) (targetType, map) -> {
                    final String content = (String) ((Map<Object, Object>) map).get(ERROR_PAYLOAD_SERIALIZATION_KEY);
                    return ErrorTestMessage.errorTestMessage(content);
                })
                .setDefaultValue((targetType, map) -> {
                    throw new TestMissingDeserializationException("No deserialization known for " + targetType);
                });

        return Deserializer.deserializer(deserializingFilterMapBuilder.build());
    }

    private Serializer getSerializer() {
        final PredicateMapBuilder<Object, Mapifier<Object>> serializingMapBuilder = predicateMapBuilder();
        serializingMapBuilder
                .put(o -> o.getClass().equals(TestMessageOfInterest.class), object -> {
                    final TestMessageOfInterest message = (TestMessageOfInterest) object;
                    final HashMap<String, Object> map = new HashMap<>();
                    map.put(PAYLOAD_SERIALIZATION_KEY, message.getContent());
                    return map;
                })
                .put(o -> o.getClass().equals(ErrorTestMessage.class), object -> {
                    final ErrorTestMessage message = (ErrorTestMessage) object;
                    final HashMap<String, Object> map = new HashMap<>();
                    map.put(ERROR_PAYLOAD_SERIALIZATION_KEY, message.getContent());
                    return map;
                })
                .setDefaultValue(o -> {
                    throw new TestMissingSerializationException("No serialization known for " + o.getClass());
                });
        return Serializer.serializer(serializingMapBuilder.build());
    }

    @RequiredArgsConstructor(access = PRIVATE)
    static final class SerializedMessageBusSetup {
        @Getter
        private final SerializedMessageBus serializedMessageBus;
        @Getter
        private final TestEnvironment testEnvironment;

    }
}
