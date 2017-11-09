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

import java.util.Set;

import org.osgi.annotation.versioning.ConsumerType;
import org.osgi.dto.DTO;

/**
 * Data Transfer Object for a configuration instance comprising features
 *
 * @NotThreadSafe
 */
@ConsumerType
public class ConfigurationDTO extends DTO {

    /**
     * The PID of the configuration.
     */
    public String pid;

    /**
     * The list of features that are contained in this configuration
     */
    public Set<String> features;

}