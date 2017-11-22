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

import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amitinside.featureflags.dto.FeatureDTO;

/**
 * The {@link FeatureManager} service is the application access point to the feature
 * flags functionality. It can be used to query the available features, configurations
 * that contain these features. It is also used to manage these instances pretty easily.
 * Therefore {@link FeatureManager} service allows introspection of the features and the
 * configuration instances available in runtime.
 *
 * <p>
 * This service differentiates between {@link FeatureDTO} and {@link ConfigurationDTO}.
 * A {@link FeatureDTO} instance is a representation of a feature whereas a
 * {@link ConfigurationDTO} instance is a representation of an OSGi Configuration in
 * which the features are specified.
 * <p>
 *
 * Access to this service requires the {@code ServicePermission[FeatureManager, GET]}
 * permission. It is intended that only administrative bundles should be granted this
 * permission to limit access to the potentially intrusive methods provided by this service.
 *
 * @noimplement This interface is not intended to be implemented by consumers.
 * @noextend This interface is not intended to be extended by consumers.
 *
 * @see FeatureDTO
 *
 * @ThreadSafe
 */
@ProviderType
public interface FeatureManager {

    /**
     * The prefix of the feature identifier pattern. This prefix should be used
     * with the feature id in OSGi Metatype XML Configuration to identify unique
     * features in an OSGi configuration.
     */
    String METATYPE_FEATURE_ID_PREFIX = "osgi.feature.";

    /**
     * Capability name for feature
     *
     * <p>
     * Used in {@code Provide-Capability} and {@code Require-Capability}
     * manifest headers with the {@code osgi.extender} namespace. For example:
     * </p>
     *
     * <pre>
     * Require-Capability: osgi.extender;
     *  filter:="(&amp;(osgi.extender=osgi.feature)(version&gt;=1.0)(!(version&gt;=2.0)))"
     * </pre>
     */
    String FEATURE_CAPABILITY_NAME = "osgi.feature";

    /**
     * Retrieve all (known) {@link FeatureDTO} instances registered in the runtime
     * <p>
     * {@link FeatureDTO}s are known if they are configured with OSGi configuration
     * </p>
     *
     * @return The known {@link FeatureDTO} instances
     * @throws NullPointerException if the specified argument is {@code null}
     * @throws IllegalArgumentException if the specified string argument is empty
     */
    Stream<FeatureDTO> getFeatures();

    /**
     * Returns all (known) {@link FeatureDTO} instances registered with the specified
     * feature ID
     * <p>
     * {@link FeatureDTO} instances are known if they are registered with OSGi configuration.
     * </p>
     *
     * @param featureID The feature ID
     * @return The known {@link FeatureDTO} instances
     * @throws NullPointerException if the specified argument is {@code null}
     * @throws IllegalArgumentException if the specified argument is empty
     */
    Stream<FeatureDTO> getFeatures(String featureID);

    /**
     * Updates the specified feature registered under the specified configuration PID
     *
     * @param configurationPID The configuration PID
     * @param featureID The feature ID
     * @param isEnabled the value for the enablement of the feature
     * @throws NullPointerException if any of the specified arguments is {@code null}
     * @throws IllegalArgumentException if any of the specified string arguments is empty
     */
    void updateFeature(String featureID, boolean isEnabled);
}