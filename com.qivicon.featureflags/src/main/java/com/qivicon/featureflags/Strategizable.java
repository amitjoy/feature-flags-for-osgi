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

import com.qivicon.featureflags.feature.Feature;
import com.qivicon.featureflags.feature.group.FeatureGroup;
import com.qivicon.featureflags.strategy.ActivationStrategy;

/**
 * A class implements the {@code Strategizable} interface to
 * indicate that the instances of the class can be used to
 * provide strategic information for its enablement. Strategic
 * information can be provided by implementing {@link ActivationStrategy}
 * interface.
 *
 * @noimplement This interface is not intended to be implemented by feature providers.
 * @noextend This interface is not intended to be extended by feature providers.
 *
 * @see Feature
 * @see FeatureGroup
 * @see ActivationStrategy
 *
 * @ThreadSafe
 */
public interface Strategizable {
}
