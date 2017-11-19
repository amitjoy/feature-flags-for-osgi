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

import java.util.List;

import org.osgi.dto.DTO;

import com.amitinside.featureflags.FeatureManager;

/**
 * Data Transfer Object for a configuration comprising features.
 *
 * <p>
 * A feature configuration is defined by its associated configuration PID and list
 * of associated features.
 * </p>
 *
 * <p>
 * A feature is specified in OSGi configuration with a id format of
 * {@code osgi.feature.X} where X is the id of your feature and {@code osgi.feature.}
 * ({@link FeatureManager#METATYPE_FEATURE_ID_PREFIX}) is a standard prefix to specify the
 * features.
 * <br/>
 * <br/>
 * An OSGi configuration is considered to be a feature specific configuration
 * if it comprises features as aforementioned.
 * </p>
 *
 * @noextend This class is not intended to be extended by consumers.
 *
 * @see FeatureDTO
 * @see FeatureManager
 *
 * @NotThreadSafe
 */
public class ConfigurationDTO extends DTO {

    /**
     * The configuration PID in which the associated features are specified
     */
    public String pid;

    /**
     * The associated features
     */
    public List<FeatureDTO> features;

}
