/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.feature.group;

import org.osgi.annotation.versioning.ConsumerType;

import com.amitinside.featureflags.FeatureManager;
import com.amitinside.featureflags.Strategizable;
import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.strategy.ActivationStrategy;

/**
 * A feature group is defined by its name. Feature Groups are registered as OSGi
 * services.
 * <p>
 * Feature Group names {@link #getName()} should be globally unique (case-insensitive).
 * If multiple feature groups have the same name, the feature group with the highest
 * service ranking is accessible through the {@link FeatureManager} service while those
 * with lower service rankings are ignored. If service rankings are equal, sort by service
 * ID in descending order. That is, services with lower service IDs will be accessible
 * whereas those with higher service IDs are ignored.
 * </p>
 * <p>
 * If the feature group is enabled, all the features belonging to it, will by default be
 * enabled. Hence, no associated strategy would be effective on belonging {@link Feature}s.
 * </p>
 * <p>
 * To check enablement of any feature group, use {@link FeatureManager#isGroupEnabled(String)}.
 * </p>
 *
 * This interface is intended to be implemented by feature providers.
 *
 * @see Feature
 * @see FeatureManager
 * @see ActivationStrategy
 *
 * @ThreadSafe
 */
@ConsumerType
public interface FeatureGroup extends Strategizable {
}