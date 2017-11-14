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

import org.osgi.annotation.versioning.ProviderType;

import com.amitinside.featureflags.FeatureManager;

/**
 * A feature is defined by its name, description and flag denoting whether
 * the feature is enabled.
 * <p>
 * A feature is specified in OSGi configurations with a name format of
 * {@code osgi.feature.X} where X is the name of your feature.
 * </p>
 *
 * @noextend This class is not intended to be extended by consumers.
 *
 * @see ConfigurationDTO
 * @see FeatureManager
 *
 * @Immutable
 * @ThreadSafe
 */
@ProviderType
public class FeatureDTO {

    public String name;
    public String description;
    public boolean isEnabled;

}
