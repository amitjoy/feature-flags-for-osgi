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

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

/**
 * A feature configuration is defined by its associated configuration PID and list
 * of associated features.
 * <p>
 * A feature is specified in OSGi configurations with a name format of
 * {@code osgi.feature.X} where X is the name of your feature. An OSGi configuration
 * is considered to be a feature specific configuration if it comprises features as
 * aforementioned.
 * </p>
 *
 * This interface is not intended to be implemented by consumers.
 *
 * @see Feature
 * @see FeatureManager
 *
 * @ThreadSafe
 */
@ProviderType
public class FeatureConfiguration {

    private final String pid;
    private final List<Feature> features;

    /**
     * Constructor
     *
     * @param pid The configuration PID of the configuration
     * @param features The list of associated features
     *
     * @throws NullPointerException if any of the specified arguments is {@code null}
     */
    public FeatureConfiguration(final String pid, final List<Feature> features) {
        requireNonNull(pid, "Configuration PID cannot be null");
        requireNonNull(features, "List of Features cannot be null");

        this.pid = pid;
        this.features = features;
    }

    /**
     * Returns the configuration PID of the configuration
     *
     * @return the configuration PID of the configuration (cannot be {@code null})
     */
    public String getPid() {
        return pid;
    }

    /**
     * Returns the associated features
     *
     * @return the associated features (cannot be {@code null})
     */
    public List<Feature> getFeatures() {
        return features;
    }

}
