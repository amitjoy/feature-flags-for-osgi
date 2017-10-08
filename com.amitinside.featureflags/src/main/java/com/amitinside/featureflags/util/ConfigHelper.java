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
package com.amitinside.featureflags.util;

import static java.util.Objects.requireNonNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import com.amitinside.featureflags.internal.Config;
import com.google.common.collect.Maps;

/**
 * Configuration Helper Class
 */
public final class ConfigHelper {

    /** Constructor */
    private ConfigHelper() {
        throw new IllegalAccessError("Non-Instantiable");
    }

    /**
     * Parse the specified properties to a more suitable and easy to use {@link EnumMap} instance
     *
     * @param properties the properties to parse
     * @return {@link EnumMap} instance
     * @throws NullPointerException if the argument is {@code null}
     */
    public static Map<Config, String> parseProperties(final Map<String, Object> properties) {
        requireNonNull(properties, "Properties cannot be null");
        final Map<Config, String> values = Maps.newEnumMap(Config.class);
        for (final Entry<String, Object> entry : properties.entrySet()) {
            final String propValue = String.valueOf(entry.getValue());
            final Config value = Config.getIfPresent(propValue).orElse(null);
            if (value != null) {
                values.put(value, propValue);
            }
        }
        return values;
    }

}
