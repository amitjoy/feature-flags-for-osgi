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
package com.qivicon.featureflags;

/**
 * Constants used with the {@link FeatureService} service.
 *
 * @Immutable
 */
public final class Constants {

    /** Factory PID of {@code Feature} configuration instances */
    public static final String FEATURE_FACTORY_PID = "com.qivicon.featureflags.feature";

    /** Factory PID of {@code FeatureGroup} configuration instances */
    public static final String FEATURE_GROUP_FACTORY_PID = "com.qivicon.featureflags.feature.group";

    /** Constructor */
    private Constants() {
        throw new IllegalAccessError("Non-Instantiable");
    }

}
