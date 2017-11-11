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

import org.osgi.annotation.versioning.ProviderType;

/**
 * A feature is defined by its name, description and flag denoting whether
 * the feature is enabled.
 * <p>
 * A feature is specified in OSGi configurations with a name format of
 * {@code osgi.feature.X} where X is the name of your feature.
 * </p>
 *
 * This interface is not intended to be implemented by consumers.
 *
 * @see FeatureConfiguration
 * @see FeatureManager
 *
 * @ThreadSafe
 */
@ProviderType
public class Feature {

    /** The prefix of the feature name pattern */
    public static final String FEATURE_NAME_PREFIX = "osgi.feature";

    private final String name;
    private final String description;
    private final boolean isEnabled;

    /**
     * Constructor
     *
     * @param name The name of the feature
     * @param description The description of the feature
     * @param isEnabled The flag denoting whether the feature is enabled
     *
     * @throws NullPointerException if any of the specified arguments is {@code null}
     */
    public Feature(final String name, final String description, final boolean isEnabled) {
        requireNonNull(name, "Feature name cannot be null");
        requireNonNull(description, "Feature description cannot be null");

        this.name = name;
        this.description = description;
        this.isEnabled = isEnabled;
    }

    /**
     * Returns the name of the feature
     *
     * @return the name of the feature (cannot be {@code null})
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the feature
     *
     * @return the description of the feature (cannot be {@code null})
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the flag of the feature that denotes whether the feature
     * is enabled
     *
     * @return the flag of the feature
     */
    public boolean isEnabled() {
        return isEnabled;
    }

}
