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

import java.util.Optional;
import java.util.stream.Stream;

/**
 * The {@link FeatureService} service is the applications access point to the Feature
 * Flag functionality. It can be used to query the available features.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface FeatureService {

    /**
     * Retrieve all (known) features.
     * <p>
     * Features are known if they are registered as {@link Feature} services or
     * are configured with OSGi configuration whose factory PID is
     * {@code com.amitinside.featureflags.feature}.
     * </p>
     *
     * @return The known features
     */
    Stream<Feature> getFeatures();

    /**
     * Returns the feature with the given name.
     * <p>
     * Features are known if they are registered as {@link Feature} services or
     * are configured with OSGi configuration whose factory PID is
     * {@code com.amitinside.featureflags.feature}.
     * </p>
     *
     * @param name The name of the feature.
     * @return The feature wrapped in {@link Optional} or empty {@link Optional} instance
     *         if not known or the name is an empty string or {@code null}.
     * @throws NullPointerException if the specified argument {@code name} is {@code null}
     */
    Optional<Feature> getFeature(String name);

    /**
     * Returns {@code true} if a feature with the given name is known and
     * enabled under the feature associated strategy.
     * <p>
     * Features are known if they are registered as {@link Feature} services or
     * are configured with OSGi configuration whose factory PID is
     * {@code com.amitinside.featureflags.feature}.
     * </p>
     * If a feature declares a valid strategy, the activation or the enablement would
     * be validated against that activation strategy, otherwise the explicitly declared
     * enabled flag in the feature would be used.
     *
     * @param name The name of the feature to check for enablement.
     * @return {@code true} if the named feature is known and enabled.
     *         Specifically {@code false} is also returned if the named feature
     *         is not known.
     * @throws NullPointerException if the specified argument {@code name} is {@code null}
     */
    boolean isEnabled(String name);
}