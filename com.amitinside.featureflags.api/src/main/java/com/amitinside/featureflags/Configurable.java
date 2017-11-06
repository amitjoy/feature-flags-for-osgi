/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * A class implements the {@code Configurable} interface to indicate that
 * the instance is configurable through OSGi Configuration Admin
 * and hence is a participant in this feature flags implementation.
 */
@ConsumerType
public interface Configurable {
}