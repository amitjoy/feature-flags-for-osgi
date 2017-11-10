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

/**
 * Data Transfer Object for a configuration instance comprising features
 *
 * @NotThreadSafe
 */
public class ConfigurationDTO extends DTO {

    /**
     * The PID of the configuration.
     *
     * @see {@code Configuration#getPid()}
     */
    public String pid;

    /**
     * The list of features that are contained in this configuration
     */
    public List<FeatureDTO> features;

}