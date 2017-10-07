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
 * Constants used with the {@link FeatureServcice} service.
 *
 * @Immutable
 */
public final class Constants {

    /** Constructor */
    private Constants() {
        throw new IllegalAccessError("Non-Instantiable");
    }

    /** Factory PID of {@link Feature} configuration instances */
    public static final String PID = "com.amitinside.featureflags.feature";

}
