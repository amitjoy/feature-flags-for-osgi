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

import static com.amitinside.featureflags.Constants.STRATEGY_SYSTEM_PROPERTY_PID;
import static org.osgi.service.component.annotations.ConfigurationPolicy.REQUIRE;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import com.amitinside.featureflags.Strategizable;

/**
 * This strategy is responsible for checking configured property key and value in the
 * system configured properties.
 */
@Component(name = "ConfiguredSystemPropertyStrategy", immediate = true, configurationPolicy = REQUIRE, configurationPid = STRATEGY_SYSTEM_PROPERTY_PID)
public final class SystemPropertyActivationStrategy extends AbstractPropertyActivationStrategy {

    @Override
    @Activate
    protected void activate(final Map<String, Object> properties) {
        super.activate(properties);
    }

    @Override
    @Modified
    protected void updated(final Map<String, Object> properties) {
        super.updated(properties);
    }

    @Override
    public boolean isEnabled(final Strategizable strategizable, final Map<String, Object> properties) {
        final String key = getKey().orElse(null);
        final String value = getValue().orElse(null);

        if (key == null || value == null) {
            return false;
        }
        final String entry = System.getProperty(key);
        return entry == null ? false : true;
    }

}