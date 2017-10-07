/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Amit Kumar Mondal
 *
 *******************************************************************************/
package com.amitinside.featureflags.impl;

import static com.amitinside.featureflags.Constants.PID;
import static com.amitinside.featureflags.impl.Config.*;
import static com.google.common.base.Preconditions.checkArgument;
import static org.osgi.service.component.annotations.ConfigurationPolicy.REQUIRE;

import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.util.ConfigHelper;
import com.google.common.base.Strings;

@Component(name = "ConfiguredFeature", configurationPolicy = REQUIRE, configurationPid = PID)
public final class ConfiguredFeature implements Feature {

    private String name;
    private String description;
    private String strategy;
    private boolean enabled;

    @Activate
    private void activate(final Map<String, Object> properties) {
        final Map<Config, String> props = ConfigHelper.parseProperties(properties);
        name = props.get(NAME);
        checkArgument(!Strings.isNullOrEmpty(name), "Feature name cannot be null or empty");
        description = Optional.ofNullable(props.get(DESCRIPTION)).orElse(name);
        strategy = Optional.ofNullable(props.get(STRATEGY)).filter(s -> !s.isEmpty()).orElse(null);
        enabled = Optional.ofNullable(props.get(ENABLED)).map(Boolean::valueOf).orElse(false);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Optional<String> description() {
        return Optional.of(description);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Optional<String> strategy() {
        return Optional.ofNullable(strategy);
    }

}