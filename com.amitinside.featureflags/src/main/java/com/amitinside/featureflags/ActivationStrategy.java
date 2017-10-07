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
package com.amitinside.featureflags;

/**
 * This interface represents a custom strategy for deciding whether
 * a feature is active or not.
 *
 * This interface is intended to be implemented by feature providers.
 */
public interface ActivationStrategy {

    /**
     * The name to be used if default strategy will be used
     */
    String DEFAULT_STRATEGY = "default";

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
     * @return The optional description of this strategy, which may be
     *         {@code null} or an empty string.
     */
    String description();

    /**
     * This method is responsible to decide whether a feature is active or not.
     * The implementation can use the custom configuration parameters of the
     * strategy stored in the feature and information from the currently acting
     * user to find a decision.
     *
     * @param feature The feature instance to check for enablement
     *
     * @return {@code true} if the feature should be enabled, otherwise {@code false}
     * @throws NullPointerException if the specified argument {@code feature} is {@code null}
     */
    boolean isEnabled(Feature feature);

}