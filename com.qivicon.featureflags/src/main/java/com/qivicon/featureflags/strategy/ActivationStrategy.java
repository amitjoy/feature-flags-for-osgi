/*******************************************************************************
 * Copyright (c) 2017 QIVICON
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
package com.qivicon.featureflags.strategy;

import java.util.Map;
import java.util.Optional;

import com.qivicon.featureflags.FeatureService;
import com.qivicon.featureflags.Strategizable;
import com.qivicon.featureflags.feature.Feature;
import com.qivicon.featureflags.feature.group.FeatureGroup;

/**
 * This interface represents a custom strategy for deciding whether
 * a {@link Strategizable} instance will be active or not.
 * <p>
 * Currently {@link Feature} and {@link FeatureGroup} instances are
 * {@link Strategizable}.
 * </p>
 * <p>
 * Strategy names {@link #getName()} should be globally unique (case-insensitive).
 * If multiple strategies have the same name, the strategy with the highest service
 * ranking is accessible through the {@link FeatureService} service while those with
 * lower service rankings are ignored. If service rankings are equal, sort by service
 * ID in descending order. That is, services with lower service IDs will be accessible
 * whereas those with higher service IDs are ignored.
 * </p>
 *
 * This interface is intended to be implemented by feature providers.
 *
 * @see Feature
 * @see FeatureGroup
 * @see FeatureService
 *
 * @ThreadSafe
 */
public interface ActivationStrategy {

    /**
     * The name of the strategy.
     *
     * @return The name of this strategy which must not be {@code null} or an
     *         empty string.
     */
    String getName();

    /**
     * The description of the strategy.
     *
     * @return The optional description of this strategy wrapped in {@link Optional}
     *         or empty {@link Optional} instance
     */
    Optional<String> getDescription();

    /**
     * This method is responsible to decide whether a feature is active or not.
     * The implementation can use the custom configuration parameters of the
     * strategy stored in the feature and information from the currently acting
     * user to find a decision.
     *
     * @param strategizable The {@link Strategizable} instance to check for enablement
     * @param properties The service properties of the specified {@link Strategizable}
     *            instance
     *
     * @return {@code true} if the strategizable should be enabled, otherwise {@code false}
     * @throws NullPointerException if any of the specified arguments is {@code null}
     */
    boolean isEnabled(Strategizable strategizable, Map<String, Object> properties);

}