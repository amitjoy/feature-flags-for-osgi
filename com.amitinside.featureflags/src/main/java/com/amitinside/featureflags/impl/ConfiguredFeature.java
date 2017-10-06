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

import java.util.Map;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.amitinside.featureflags.Feature;
import com.amitinside.featureflags.util.ConfigHelper;

@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "com.amitinside.featureflags.feature")
public final class ConfiguredFeature implements Feature {

    private String name;
    private String description;
    private boolean enabled;

    @Activate
    private void activate(final Map<String, Object> properties) {
        final Map<Config, String> props = ConfigHelper.parseProperties(properties);
        name = props.get(Config.NAME);
        if (name == null) {
            final Object pid = properties.get(Constants.SERVICE_PID);
            if (pid == null) {
                name = getClass().getName() + "$" + System.identityHashCode(this);
            } else {
                name = pid.toString();
            }
        }
        description = props.get(Config.DESCRIPTION);
        if (description == null) {
            description = name;
        }
        enabled = Boolean.valueOf(props.get(Config.ENABLED));
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
}