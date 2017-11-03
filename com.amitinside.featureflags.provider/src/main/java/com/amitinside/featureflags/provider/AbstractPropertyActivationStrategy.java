/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.provider;

import static com.amitinside.featureflags.Config.*;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.amitinside.featureflags.Config;
import com.amitinside.featureflags.strategy.ActivationStrategy;
import com.amitinside.featureflags.util.ConfigHelper;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

public abstract class AbstractPropertyActivationStrategy implements ActivationStrategy {

    private String name;
    private String description;
    private String propertyKey;
    private String propertyValue;

    private final Lock lock = new ReentrantLock(true);

    protected void activate(final Map<String, Object> properties) {
        extractProperties(properties);
    }

    protected void updated(final Map<String, Object> properties) {
        extractProperties(properties);
    }

    private void extractProperties(final Map<String, Object> properties) {
        lock.lock();
        try {
            final Map<Config, Object> props = ConfigHelper.parseProperties(properties);

            name = (String) props.get(NAME);
            checkArgument(!Strings.isNullOrEmpty(name), "Strategy name cannot be null or empty");

            //@formatter:off
            description = Optional.ofNullable(props.get(DESCRIPTION)).map(String.class::cast)
                                                                     .orElse(name);
            propertyKey = Optional.ofNullable(props.get(PROPERTY_KEY)).map(String.class::cast)
                                                               .filter(s -> !s.isEmpty())
                                                               .orElse(null);
            propertyValue = Optional.ofNullable(props.get(PROPERTY_VALUE)).map(String.class::cast)
                                                               .filter(s -> !s.isEmpty())
                                                               .orElse(null);
            //@formatter:on
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of(description);
    }

    public Optional<String> getKey() {
        return Optional.ofNullable(propertyKey);
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(propertyValue);
    }

    @Override
    public String toString() {
        //@formatter:off
        return Objects.toStringHelper(this)
                        .add("Name", name)
                        .add("Description", description)
                        .add("Property Key", propertyKey)
                        .add("Property Value", propertyValue)
                        .toString();
        //@formatter:on
    }

}