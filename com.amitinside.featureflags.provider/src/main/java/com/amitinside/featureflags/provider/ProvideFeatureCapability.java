/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.provider;

import static com.amitinside.featureflags.FeatureManager.FEATURE_CAPABILITY_NAME;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import aQute.bnd.annotation.headers.ProvideCapability;

@ProvideCapability(ns = "osgi.extender", name = FEATURE_CAPABILITY_NAME, version = "1.0")
@Retention(RetentionPolicy.CLASS)
public @interface ProvideFeatureCapability {
}
