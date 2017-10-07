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

import static com.amitinside.featureflags.ActivationStrategy.DEFAULT_STRATEGY;
import static com.amitinside.featureflags.impl.Config.*;
import static com.google.common.base.Preconditions.checkArgument;
import static org.osgi.service.component.annotations.ConfigurationPolicy.REQUIRE;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.amitinside.featureflags.Feature;
import com.amitinside.featureflags.util.ConfigHelper;
import com.google.common.base.Strings;

@Component(configurationPolicy = REQUIRE, configurationPid = "com.amitinside.featureflags.feature")
public final class ConfiguredFeature implements Feature {

    private String name;
    private String description;
    private String strategyId;
    private boolean enabled;

    @Activate
    private void activate(final Map<String, Object> properties) {
        final Map<Config, String> props = ConfigHelper.parseProperties(properties);
        name = props.get(NAME);
        checkArgument(!Strings.isNullOrEmpty(name), "Name cannot be null or empty");
        description = Strings.emptyToNull(props.get(DESCRIPTION));
        if (description == null) {
            description = name;
        }
        strategyId = Strings.emptyToNull(props.get(STRATEGY));
        if (strategyId == null) {
            strategyId = DEFAULT_STRATEGY;
        }
        enabled = Boolean.valueOf(props.get(ENABLED));
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String strategyId() {
        return strategyId;
    }

}