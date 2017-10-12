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
package com.amitinside.featureflags.feature.group;

import java.util.Optional;

import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.Strategizable;
import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.strategy.ActivationStrategy;

/**
 * A feature group is defined by its name. Feature Groups are registered as OSGi
 * services.
 * <p>
 * Feature Group names {@link #getName()} should be globally unique (case-insensitive).
 * If multiple feature groups have the same name, the feature group with the highest
 * service ranking is accessible through the {@link FeatureService} service while those
 * with lower service rankings are ignored. If service rankings are equal, sort by service
 * ID in descending order. That is, services with lower service IDs will be accessible
 * whereas those with higher service IDs are ignored.
 * </p>
 *
 * This interface is intended to be implemented by feature providers.
 *
 * @see Feature
 * @see FeatureService
 * @see ActivationStrategy
 *
 * @ThreadSafe
 */
public interface FeatureGroup extends Strategizable {

    /**
     * The name of the feature group.
     *
     * @return The name of this feature group which must not be {@code null} or an
     *         empty string.
     */
    String getName();

    /**
     * The description of the feature group.
     *
     * @return The optional description of this feature group wrapped in {@link Optional}
     *         or empty {@link Optional} instance
     */
    Optional<String> getDescription();

    /**
     * The associated strategy identifier that will be used to check
     * whether this feature group will be enabled or not.
     *
     * @return The strategy identifier of this feature wrapped in {@link Optional}
     *         or empty {@link Optional} instance
     */
    Optional<String> getStrategy();

    /**
     * Checks whether the feature group is enabled. If the feature group is enabled, all
     * the features belonging to it, will by default be enabled. Hence, no associated
     * strategy would be effective on belonging {@link Feature}s.
     *
     * @return {@code true} if this {@code FeatureGroup} is enabled
     */
    boolean isEnabled();

}