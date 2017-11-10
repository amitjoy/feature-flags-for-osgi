/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.provider;

/**
 * Constants required for feature flags.
 */
public final class Constants {

    /** Prefix required for attribute definitions in configuration */
    public static final String FEATURE_AD_NAME_PREFIX = "osgi.feature.";

    /** Constructor */
    private Constants() {
        throw new IllegalAccessError("Non-Instantiable");
    }

}
