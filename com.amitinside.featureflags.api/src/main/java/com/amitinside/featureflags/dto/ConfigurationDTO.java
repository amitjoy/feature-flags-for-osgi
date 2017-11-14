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

import org.osgi.annotation.versioning.ProviderType;

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
 * A feature is specified in OSGi configuration with a name format of
 * {@code osgi.feature.X} where X is the name of your feature and {@code osgi.feature.}
 * ({@link FeatureManager#FEATURE_NAME_PREFIX}) is a standard prefix to specify the
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
 * @Immutable
 * @ThreadSafe
 */
@ProviderType
public class ConfigurationDTO {

    /**
     * The configuration PID in which the associated features are specified
     */
    public String pid;

    /**
     * The associated features
     */
    public List<FeatureDTO> features;

}
