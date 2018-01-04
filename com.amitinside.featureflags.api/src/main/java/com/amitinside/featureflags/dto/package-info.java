/*******************************************************************************
 * Copyright (c) 2017-2018 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/**
 * Provides data transfer object for feature instance
 *
 * <p>
 * Bundles wishing to use this package must list the package in the
 * Import-Package header of the bundle's manifest. This package has two types of
 * users: the consumers that use the API in this package and the providers that
 * implement the API in this package.
 *
 * <p>
 * Example import for consumers using the API in this package:
 * <p>
 * {@code  Import-Package: com.amitinside.featureflags.dto;version="[1.0,2.0)"}
 * <p>
 * Example import for providers implementing the API in this package:
 * <p>
 * {@code  Import-Package: com.amitinside.featureflags.dto;version="[1.0,1.1)"}
 *
 * @since 1.0
 */
@Version("1.0")
package com.amitinside.featureflags.dto;

import org.osgi.annotation.versioning.Version;