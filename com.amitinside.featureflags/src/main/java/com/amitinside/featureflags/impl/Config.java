package com.amitinside.featureflags.impl;

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
    STRATEGY("strategy");

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
     * @throws NullPointerException if the argument is {@code null}
     */
    public static Optional<Config> getIfPresent(final String value) {
        requireNonNull(value, "Value cannot be null");
        try {
            return Optional.of(Enum.valueOf(Config.class, value));
        } catch (final IllegalArgumentException iae) {
            return Optional.empty();
        }
    }

}
