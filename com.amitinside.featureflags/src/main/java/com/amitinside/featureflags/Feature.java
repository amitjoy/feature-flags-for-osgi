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
 * A feature is defined by its name. Features are registered as OSGi services.
 * <p>
 * Feature {@link #name() names} should be globally unique. If multiple
 * features have the same name, the feature with the highest service ranking is
 * accessible through the {@link FeatureService} service while those with lower
 * service rankings are ignored.
 * <p>
 * This interface is expected to be implemented by feature providers.
 */
public interface Feature {

    /**
     * The name of the feature.
     *
     * @return The name of this feature which must not be {@code null} or an
     *         empty string.
     */
    String name();

    /**
     * The description of the feature.
     *
     * @return The optional description of this feature, which may be
     *         {@code null} or an empty string.
     */
    String description();

    /**
     * Checks whether the feature is enabled.
     *
     * @return {@code true} if this {@code Feature} is enabled in the given
     *         {@link ExecutionContext}.
     */
    boolean isEnabled();
}