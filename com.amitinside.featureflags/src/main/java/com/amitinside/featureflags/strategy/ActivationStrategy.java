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
package com.amitinside.featureflags.strategy;

import java.util.Optional;

import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.feature.Feature;

/**
 * This interface represents a custom strategy for deciding whether
 * a feature is active or not.
 * <p>
 * Strategy names {@link #name()} should be globally unique.
 * </p>
 *
 * This interface is intended to be implemented by feature providers.
 *
 * @see Feature
 * @see FeatureService
 */
public interface ActivationStrategy {

    /**
     * The name of the strategy.
     *
     * @return The name of this strategy which must not be {@code null} or an
     *         empty string.
     */
    String name();

    /**
     * The description of the strategy.
     *
     * @return The optional description of this strategy wrapped in {@link Optional}
     *         or empty {@link Optional} instance
     */
    Optional<String> description();

    /**
     * This method is responsible to decide whether a feature is active or not.
     * The implementation can use the custom configuration parameters of the
     * strategy stored in the feature and information from the currently acting
     * user to find a decision.
     *
     * @param feature The {@link Feature} instance to check for enablement
     *
     * @return {@code true} if the feature should be enabled, otherwise {@code false}
     * @throws NullPointerException if the specified argument {@code feature} is {@code null}
     */
    boolean isEnabled(Feature feature);

}