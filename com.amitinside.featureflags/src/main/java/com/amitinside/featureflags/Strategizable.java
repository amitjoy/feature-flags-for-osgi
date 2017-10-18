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

import org.osgi.annotation.versioning.ProviderType;

import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.feature.group.FeatureGroup;
import com.amitinside.featureflags.strategy.ActivationStrategy;

/**
 * A class implements the {@code Strategizable} interface to
 * indicate that the instances of the class can be used to
 * provide strategic information for its enablement. Strategic
 * information can be provided by implementing {@link ActivationStrategy}
 * interface.
 *
 * @noimplement This interface is not intended to be implemented by feature providers.
 * @noextend This interface is not intended to be extended by feature providers.
 *
 * @see Feature
 * @see FeatureGroup
 * @see ActivationStrategy
 *
 * @ThreadSafe
 */
@ProviderType
public interface Strategizable {

    /**
     * The name of the strategizable.
     *
     * @return The name of this strategizable which must not be {@code null} or an
     *         empty string.
     */
    String getName();

    /**
     * The description of the strategizable.
     *
     * @return The optional description of this feature wrapped in {@link Optional}
     *         or empty {@link Optional} instance
     */
    Optional<String> getDescription();

    /**
     * The associated strategy identifier that will be used to check
     * whether this strategizable will be enabled or not.
     *
     * @return The strategy identifier of this strategizable wrapped in {@link Optional}
     *         or empty {@link Optional} instance
     */
    Optional<String> getStrategy();

    /**
     * Checks whether the strategizable is enabled in its configuration. This only shows
     * the configuration provided to the strategizable.
     *
     * @return {@code true} if this {@code Strategizable} is enabled in its configuration
     */
    boolean isEnabled();
}
