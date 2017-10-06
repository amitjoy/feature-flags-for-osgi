package com.amitinside.featureflags.util;

import static java.util.Objects.requireNonNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import com.amitinside.featureflags.impl.Config;
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
            switch (entry.getKey()) {
                case "name":
                    values.put(Config.NAME, String.valueOf(entry.getValue()));
                    break;
                case "description":
                    values.put(Config.DESCRIPTION, String.valueOf(entry.getValue()));
                    break;
                case "enabled":
                    values.put(Config.ENABLED, String.valueOf(entry.getValue()));
                    break;
            }
        }
        return values;
    }

}
