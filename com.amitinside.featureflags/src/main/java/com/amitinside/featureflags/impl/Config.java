package com.amitinside.featureflags.impl;

import com.amitinside.featureflags.Feature;

/**
 * Represents configuration elements of a {@link Feature} service
 */
public enum Config {

    NAME("name"),
    DESCRIPTION("description"),
    ENABLED("enabled");

    final String value;

    private Config(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
