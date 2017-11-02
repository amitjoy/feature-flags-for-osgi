/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

/**
 * This represents configuration elements to be used for instance properties. Not every instance will
 * require all these properties. This enum aggregates all the predefined service properties to be used
 * to represent the primary service instances in Feature Flags.
 */
public enum Config {

    NAME("name"),
    DESCRIPTION("description"),
    ENABLED("enabled"),
    STRATEGY("strategy"),
    GROUPS("groups"),
    PROPERTY_KEY("property_key"),
    PROPERTY_VALUE("property_value");

    final String value;

    /** Constructor */
    private Config(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /**
     * Returns an optional enum constant for the given type, using {@link Enum#valueOf}. If the
     * constant does not exist, {@link Optional#empty()} is returned.
     *
     * @param value the value to check for existence in {@link Config} enumeration
     * @return {@link Config} instance wrapped in {@link Optional} or empty {@link Optional} if not present
     * @throws NullPointerException if the argument is {@code null}
     */
    public static Optional<Config> getIfPresent(final String value) {
        requireNonNull(value, "Value cannot be null");
        try {
            return Optional.of(Enum.valueOf(Config.class, value.toUpperCase()));
        } catch (final IllegalArgumentException iae) {
            return Optional.empty();
        }
    }

}
