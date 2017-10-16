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

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import com.amitinside.featureflags.feature.Feature;

/**
 * Represents configuration elements of a {@link Feature} service
 */
public enum Config {

    NAME("name"),
    DESCRIPTION("description"),
    ENABLED("enabled"),
    STRATEGY("strategy"),
    GROUP("group");

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
     *
     * @throws NullPointerException if the argument is {@code null}
     * @return {@link Config} instance wrapped in {@link Optional} or empty {@link Optional} if not present
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
