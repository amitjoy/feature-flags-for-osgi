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

import java.util.Map;

import org.osgi.dto.DTO;

import com.amitinside.featureflags.FeatureManager;

/**
 * Data Transfer Object for a feature.
 *
 * <p>
 * A feature is defined by its identifier, name, description, flag denoting whether
 * the feature is enabled and extra properties to better categorize the feature
 * </p>
 *
 * <p>
 * A feature is specified in OSGi configuration with a id format of
 * {@code osgi.feature.X} where X is the id of your feature and {@code osgi.feature.}
 * ({@link FeatureManager#FEATURE_ID_PREFIX}) is a standard prefix to specify the
 * features.
 * </p>
 *
 * @noextend This class is not intended to be extended by consumers.
 *
 * @see ConfigurationDTO
 * @see FeatureManager
 *
 * @NotThreadSafe
 */
public class FeatureDTO extends DTO {

    /**
     * The identifier of the feature
     */
    public String id;

    /**
     * The name of the feature
     */
    public String name;

    /**
     * The description of the feature
     */
    public String description;

    /**
     * The enablement flag of the feature that denotes whether the feature
     * is enabled or not
     */
    public boolean isEnabled;

    /**
     * The extra properties to categorize the feature
     */
    public Map<String, String> properties;

}
