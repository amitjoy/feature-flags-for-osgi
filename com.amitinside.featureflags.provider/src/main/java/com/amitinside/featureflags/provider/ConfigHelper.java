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

import static java.util.Objects.requireNonNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

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
     * Parse the specified properties to a more suitable and easy to use {@link EnumMap} instance.
     *
     * @param properties the properties to parse
     * @return Returns an {@link EnumMap} instance or empty (not {@code null}) if specified properties
     *         cannot be parsed.
     * @throws NullPointerException if the argument is {@code null}
     */
    public static Map<Config, Object> parseProperties(final Map<String, Object> properties) {
        requireNonNull(properties, "Properties cannot be null");
        final Map<Config, Object> values = Maps.newEnumMap(Config.class);
        for (final Entry<String, Object> entry : properties.entrySet()) {
            final String propKey = entry.getKey();
            final Object propValue = entry.getValue();
            Config.getIfPresent(propKey).ifPresent(x -> values.put(x, propValue));
        }
        return values;
    }

}
