/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.dto;

import org.osgi.dto.DTO;

/**
 * Data Transfer Object for a feature instance.
 *
 * @NotThreadSafe
 */
public class FeatureDTO extends DTO {

    /**
     * The name of the feature.
     */
    public String name;

    /**
     * The description of the feature.
     */
    public String description;

    /**
     * Checks whether the feature is enabled in its configuration.
     */
    public boolean isEnabled;

}