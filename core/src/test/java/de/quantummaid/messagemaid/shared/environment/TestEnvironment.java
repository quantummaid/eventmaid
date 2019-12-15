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

package de.quantummaid.messagemaid.shared.environment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestEnvironment {
    private final Map<String, Object> definedPropertiesMap = new ConcurrentHashMap<>();

    public static TestEnvironment emptyTestEnvironment() {
        return new TestEnvironment();
    }

    public void setProperty(final TestEnvironmentProperty property, final Object o) {
        setProperty(property.name(), o);
    }

    public void setProperty(final String property, final Object o) {
        definedPropertiesMap.put(property, o);
    }

    public void setPropertyIfNotSet(final TestEnvironmentProperty property, final Object o) {
        setPropertyIfNotSet(property.name(), o);
    }

    public void setPropertyIfNotSet(final String property, final Object o) {
        if (has(property)) {
            throw new IllegalArgumentException("Property " + property + " already set.");
        }
        setProperty(property, o);
    }

    public <T> T getPropertyOrSetDefault(final TestEnvironmentProperty property, final T defaultValue) {
        return getPropertyOrSetDefault(property.name(), defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPropertyOrSetDefault(final String property, final T defaultValue) {
        if (!has(property)) {
            setProperty(property, defaultValue);
        }
        final Class<?> aClass = defaultValue.getClass();
        return (T) getPropertyAsType(property, aClass);
    }

    public synchronized void addToListProperty(final String property, final Object... os) {
        for (Object o : os) {
            addToListProperty(property, o);
        }
    }

    public synchronized void addToListProperty(final TestEnvironmentProperty property, final Object... os) {
        for (Object o : os) {
            addToListProperty(property, o);
        }
    }

    public synchronized void addToListProperty(final TestEnvironmentProperty property, final Object o) {
        addToListProperty(property.name(), o);
    }

    public synchronized void addToListProperty(final String property, final Object o) {
        @SuppressWarnings("unchecked")
        final List<Object> list = (List<Object>) definedPropertiesMap.getOrDefault(property, new LinkedList<>());
        list.add(o);
        definedPropertiesMap.put(property, list);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPropertyAsType(final TestEnvironmentProperty property, final Class<T> tClass) {
        return (T) getProperty(property);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPropertyAsType(final String property, final Class<T> tClass) {
        return (T) getProperty(property);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getPropertyAsListOfType(final String property, final Class<T> tClass) {
        return (List<T>) getProperty(property);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getPropertyAsListOfType(final TestEnvironmentProperty property, final Class<T> tClass) {
        return (List<T>) getProperty(property);
    }

    public Object getProperty(final TestEnvironmentProperty property) {
        return getProperty(property.name());
    }

    public Object getProperty(final String property) {
        final Object object = definedPropertiesMap.get(property);
        if (object != null) {
            return object;
        } else {
            throw new RuntimeException("Property " + property + " not set.");
        }
    }

    public boolean has(final TestEnvironmentProperty property) {
        return has(property.name());
    }

    public boolean has(final String property) {
        return definedPropertiesMap.containsKey(property);
    }
}
