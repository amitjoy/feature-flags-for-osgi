/*******************************************************************************
 * Copyright (c) 2017-2019 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.dto;

import com.amitinside.featureflags.FeatureManager;

/**
 * Data Transfer Object for a feature.
 *
 * <p>
 * A feature is defined by its identifier, name, description, flag denoting
 * whether the feature is enabled and the bundle's unique identifier in which
 * the feature is specified.
 * </p>
 *
 * <p>
 * A feature is specified in OSGi configuration with an identifier format of
 * {@code osgi.feature.X} where {@code X} is the ID of your feature and
 * {@code osgi.feature.} ({@link FeatureManager#METATYPE_FEATURE_ID_PREFIX}) is
 * a standard prefix to specify the features. <br/>
 * <br/>
 * <b>Note that</b>, a feature identifier is case sensitive as well.
 * </p>
 *
 * @noextend This class is not intended to be extended by consumers.
 *
 * @see FeatureManager
 *
 * @NotThreadSafe
 */
public class FeatureDTO {

	/**
	 * The identifier of the feature
	 */
	public String id;

	/**
	 * The bundle identifier containing the feature
	 */
	public long bundleId;

	/**
	 * The name of the feature
	 */
	public String name;

	/**
	 * The description of the feature
	 */
	public String description;

	/**
	 * The enablement flag of the feature that denotes whether the feature is
	 * enabled or not
	 */
	public boolean isEnabled;

}
