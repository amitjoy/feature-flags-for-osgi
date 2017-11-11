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

import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The {@link FeatureManager} service is the application access point to the feature
 * flags functionality. It can be used to query the available features, configurations
 * that contain these features. It is also used to manage these instances easily.
 *
 * @noimplement This interface is not intended to be implemented by consumers.
 * @noextend This interface is not intended to be extended by consumers.
 *
 * @see Feature
 * @see FeatureConfiguration
 *
 * @ThreadSafe
 */
@ProviderType
public interface FeatureManager {

    /**
     * Retrieve all (known) {@link FeatureConfiguration} instances
     * <p>
     * {@link FeatureConfiguration}s are known if they comprise features
     * </p>
     *
     * @return The known {@link FeatureConfiguration} instances
     */
    Stream<FeatureConfiguration> getConfigurations();

    /**
     * Retrieve all (known) {@link Feature} instances registered under the provided
     * configuration PID
     * <p>
     * {@link Feature}s are known if they are configured with OSGi configuration
     * under the specified configuration PID
     * </p>
     *
     * @param configurationPID The configuration PID
     * @return The known {@link Feature} instances
     * @throws NullPointerException if the specified argument is {@code null}
     * @throws IllegalArgumentException if the specified argument is empty
     */
    Stream<Feature> getFeatures(String configurationPID);

    /**
     * Returns the known {@link FeatureConfiguration} instance
     * <p>
     * {@link FeatureConfiguration} instances are known if they comprise features
     * </p>
     *
     * @param name The configuration PID
     * @return The {@link FeatureConfiguration} wrapped in {@link Optional} or empty {@link Optional}
     *         instance if not known or the name is an empty string or {@code null}
     * @throws NullPointerException if the specified argument is {@code null}
     * @throws IllegalArgumentException if the specified argument is empty
     */
    Optional<FeatureConfiguration> getConfiguration(String configurationPID);

    /**
     * Returns the feature registered under the specified configuration PID with the given name
     * <p>
     * {@link Feature} instances are known if they are registered with OSGi configuration.
     * </p>
     *
     * @param configurationPID The configuration PID
     * @param featureName The name of the feature
     * @return The {@link Feature} wrapped in {@link Optional} or empty {@link Optional}
     *         instance if not known or the name is an empty string or {@code null}
     * @throws NullPointerException if any of the specified arguments is {@code null}
     * @throws IllegalArgumentException if any of the specified arguments is empty
     */
    Optional<Feature> getFeature(String configurationPID, String featureName);

    /**
     * Updates the specified feature registered under the specified configuration PID
     *
     * @param configurationPID The configuration PID
     * @param featureName The name of the feature
     * @param isEnabled the value for the enablement of the feature
     * @return {@code true} if the feature is known and updated by this operation.
     *         Specifically {@code false} is also returned if the feature is not
     *         known or the operation failed to update the feature
     * @throws NullPointerException if any of the specified arguments is {@code null}
     * @throws IllegalArgumentException if any of the specified arguments is empty
     */
    boolean updateFeature(String configurationPID, String featureName, boolean isEnabled);
}