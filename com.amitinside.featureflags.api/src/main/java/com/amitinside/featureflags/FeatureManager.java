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

import com.amitinside.featureflags.dto.ConfigurationDTO;
import com.amitinside.featureflags.dto.FeatureDTO;

/**
 * The {@link FeatureManager} service is the application access point to the feature
 * flags functionality. It can be used to query the available features, configurations
 * that contain these features. It is also used to manage these instances easily.
 *
 * @noimplement This interface is not intended to be implemented by consumers.
 * @noextend This interface is not intended to be extended by consumers.
 *
 * @see FeatureDTO
 * @see ConfigurationDTO
 *
 * @ThreadSafe
 */
@ProviderType
public interface FeatureManager {

    /**
     * Retrieve all (known) {@link ConfigurationDTO} instances
     * <p>
     * {@link ConfigurationDTO}s are known if they comprise features
     * </p>
     *
     * @return The known {@link ConfigurationDTO} instances
     */
    Stream<ConfigurationDTO> getConfigurations();

    /**
     * Retrieve all (known) {@link FeatureDTO} instances registered under the provided
     * configuration PID
     * <p>
     * {@link FeatureDTO}s are known if they are configured with OSGi configuration
     * under the specified configuration PID
     * </p>
     *
     * @param configurationPID The configuration PID
     * @return The known {@link FeatureDTO} instances
     * @throws NullPointerException if the specified argument is {@code null}
     */
    Stream<FeatureDTO> getFeatures(String configurationPID);

    /**
     * Returns the feature registered under the specified configuration PID with the given name
     * <p>
     * Features are known if they are registered with OSGi configuration.
     * </p>
     *
     * @param configurationPID The configuration PID
     * @param featureName The name of the feature
     * @return The {@link FeatureDTO} wrapped in {@link Optional} or empty {@link Optional}
     *         instance if not known or the name is an empty string or {@code null}
     * @throws NullPointerException if any of the specified arguments is {@code null}
     */
    Optional<FeatureDTO> getFeature(String configurationPID, String featureName);

    /**
     * Returns the known {@link ConfigurationDTO} instance
     * <p>
     * {@link ConfigurationDTO} instances are known if they comprise feature configurations
     * </p>
     *
     * @param name The name of the configuration
     * @return The {@link ConfigurationDTO} wrapped in {@link Optional} or empty {@link Optional}
     *         instance if not known or the name is an empty string or {@code null}
     * @throws NullPointerException if the specified argument is {@code null}
     */
    Optional<ConfigurationDTO> getConfiguration(String configurationPID);

    /**
     * Updates the specified feature registered under the specified configuration PID
     *
     * @param configurationPID The configuration PID
     * @param featureName The name of the feature.
     * @param valueToSet the value for the enablement of the feature
     * @return {@code true} if the named feature is known and updated by this operation.
     *         Specifically {@code false} is also returned if the named feature
     *         is not known or the operation failed to update the feature
     * @throws NullPointerException if any of the specified arguments is {@code null}
     */
    boolean updateFeature(String configurationPID, String featureName, boolean valueToSet);
}