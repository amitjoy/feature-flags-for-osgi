/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Amit Kumar Mondal
 *
 *******************************************************************************/
package com.amitinside.featureflags.feature;

import java.util.Optional;

import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.strategy.ActivationStrategy;

/**
 * A feature is defined by its name. Features are registered as OSGi services.
 * <p>
 * Feature names {@link #getName()} should be globally unique. If multiple
 * features have the same name, the feature with the highest service ranking is
 * accessible through the {@link FeatureService} service while those with lower
 * service rankings are ignored. If service rankings are equal, sort by service ID
 * in descending order. That is, services with lower service IDs will be accessible
 * whereas those with higher service IDs are ignored.
 * </p>
 *
 * This interface is intended to be implemented by feature providers.
 *
 * @see FeatureService
 * @see ActivationStrategy
 *
 * @ThreadSafe
 */
public interface Feature {

    /**
     * The name of the feature.
     *
     * @return The name of this feature which must not be {@code null} or an
     *         empty string.
     */
    String getName();

    /**
     * The description of the feature.
     *
     * @return The optional description of this feature wrapped in {@link Optional}
     *         or empty {@link Optional} instance
     */
    Optional<String> getDescription();

    /**
     * The associated strategy identifier that will be used to check
     * whether this feature is active or not
     *
     * @return The identifier of this feature wrapped in {@link Optional}
     *         or empty {@link Optional} instance
     */
    Optional<String> getStrategy();

    /**
     * Checks whether the feature is enabled.
     *
     * @return {@code true} if this {@code Feature} is enabled for the associated
     *         strategy
     */
    boolean isEnabled();
}