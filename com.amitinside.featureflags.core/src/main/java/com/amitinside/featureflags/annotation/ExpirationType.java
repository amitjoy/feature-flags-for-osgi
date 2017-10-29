/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.annotation;

/**
 * This enumeration is used to specify a feature expiration type
 */
public enum ExpirationType {
    /**
     * This specifies the type that the feature never expires
     */
    NEVER,

    /**
     * This specifies the type that the feature associates an expiration date
     */
    TIMED
}