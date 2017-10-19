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

/**
 * Constants used with the {@link FeatureService} service.
 *
 * @Immutable
 */
public final class Constants {

    /** Factory PID of {@code Feature} configuration instances */
    public static final String FEATURE_FACTORY_PID = "com.amitinside.featureflags.feature";

    /** Factory PID of {@code FeatureGroup} configuration instances */
    public static final String FEATURE_GROUP_FACTORY_PID = "com.amitinside.featureflags.feature.group";

    /** Factory PID of System Property {@code ActivationStrategy} configuration instances */
    public static final String STRATEGY_SYSTEM_PROPERTY_PID = "com.amitinside.featureflags.strategy.system.property";

    /** Constructor */
    private Constants() {
        throw new IllegalAccessError("Non-Instantiable");
    }

}
