/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.internal;

import static com.amitinside.featureflags.Constants.FEATURE_GROUP_FACTORY_PID;
import static com.amitinside.featureflags.internal.Config.*;
import static com.google.common.base.Preconditions.checkArgument;
import static org.osgi.service.component.annotations.ConfigurationPolicy.REQUIRE;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import com.amitinside.featureflags.feature.group.FeatureGroup;
import com.amitinside.featureflags.util.ConfigHelper;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

@Component(name = "ConfiguredFeatureGroup", immediate = true, configurationPolicy = REQUIRE, configurationPid = FEATURE_GROUP_FACTORY_PID)
public final class ConfiguredFeatureGroup implements FeatureGroup {

    private String name;
    private String description;
    private String strategy;
    private volatile boolean isEnabled;

    private final Lock lock = new ReentrantLock(true);

    @Activate
    protected void activate(final Map<String, Object> properties) {
        extractProperties(properties);
    }

    @Modified
    protected void updated(final Map<String, Object> properties) {
        extractProperties(properties);
    }

    private void extractProperties(final Map<String, Object> properties) {
        lock.lock();
        try {
            final Map<Config, Object> props = ConfigHelper.parseProperties(properties);

            name = (String) props.get(NAME);
            checkArgument(!Strings.isNullOrEmpty(name), "Feature Group name cannot be null or empty");

            //@formatter:off
            description = Optional.ofNullable(props.get(DESCRIPTION)).map(String.class::cast)
                                                                     .orElse(name);
            strategy = Optional.ofNullable(props.get(STRATEGY)).map(String.class::cast)
                                                               .filter(s -> !s.isEmpty())
                                                               .orElse(null);
            isEnabled = Optional.ofNullable(props.get(ENABLED)).map(Boolean.class::cast)
                                                               .orElse(false);
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

    @Override
    public Optional<String> getStrategy() {
        return Optional.ofNullable(strategy);
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public String toString() {
        //@formatter:off
        return Objects.toStringHelper(this)
                        .add("Name", name)
                        .add("Description", description)
                        .add("Strategy", strategy)
                        .add("Enabled", isEnabled).toString();
        //@formatter:on
    }

}