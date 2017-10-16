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
package com.qivicon.featureflags.feature;

import java.util.stream.Stream;

import com.qivicon.featureflags.FeatureService;
import com.qivicon.featureflags.Strategizable;
import com.qivicon.featureflags.feature.group.FeatureGroup;
import com.qivicon.featureflags.strategy.ActivationStrategy;

/**
 * A feature is defined by its name. Features are registered as OSGi services.
 * <p>
 * Feature names {@link #getName()} should be globally unique (case-insensitive).
 * If multiple features have the same name, the feature with the highest service
 * ranking is accessible through the {@link FeatureService} service while those
 * with lower service rankings are ignored. If service rankings are equal, sort
 * by service ID in descending order. That is, services with lower service IDs
 * will be accessible whereas those with higher service IDs are ignored.
 * </p>
 * <p>
 * If a feature belongs to a valid feature group or it specifies any valid
 * activation strategy, this would not return the actual enablement value.
 * </p>
 * <p>
 * To check enablement of any feature, use {@link FeatureService#isFeatureEnabled(String)}.
 * </p>
 *
 * This interface is intended to be implemented by feature providers.
 *
 * @see FeatureGroup
 * @see FeatureService
 * @see ActivationStrategy
 *
 * @ThreadSafe
 */
public interface Feature extends Strategizable {

    /**
     * The associated group identifiers that will be used to check
     * whether this feature belongs to any group. A feature can associate
     * multiple groups but the enabled one will only be taken into
     * account.
     * <p>
     * If a feature associates multiple groups in which multiple
     * groups are active, then the feature groups are sorted
     * according to their natural order of the names of the groups. Any of
     * the groups with lower sort order and is enabled, would eventually determine the
     * enablement of this feature.
     * </p>
     *
     * @return The group identifiers of this feature or empty {@link Stream}
     */
    Stream<String> getGroups();

}