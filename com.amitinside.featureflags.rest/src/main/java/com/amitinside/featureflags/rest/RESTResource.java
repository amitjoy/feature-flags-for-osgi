/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.amitinside.featureflags.FeatureManager;
import com.amitinside.featureflags.dto.ConfigurationDTO;
import com.amitinside.featureflags.dto.FeatureDTO;

import osgi.enroute.rest.api.REST;
import osgi.enroute.rest.api.RESTRequest;

@Component(name = "FeatureFlagsRESTResource", immediate = true)
public final class RESTResource implements REST {

    private FeatureManager featureService;

    public List<ConfigurationDTO> getConfigurations(final RESTRequest req) {
        //@formatter:off
        return featureService.getConfigurations()
                             .collect(Collectors.toList());
        //@formatter:on
    }

    public List<FeatureDTO> getFeatures(final RESTRequest req, final String configurationPID) {
        //@formatter:off
        return featureService.getFeatures(configurationPID)
                             .collect(Collectors.toList());
        //@formatter:on
    }

    public ConfigurationDTO getConfiguration(final RESTRequest req, final String configurationPID) {
        return featureService.getConfiguration(configurationPID).orElse(null);
    }

    public FeatureDTO getFeature(final RESTRequest req, final String configurationPID, final String featureName) {
        return featureService.getFeature(configurationPID, featureName).orElse(null);
    }

    public boolean putFeature(final RESTRequest req, final String configurationPID, final String featureName,
            final boolean isEnabled) {
        return featureService.updateFeature(configurationPID, featureName, isEnabled);
    }

    /**
     * {@link FeatureManager} service binding callback
     */
    @Reference
    protected void setFeatureService(final FeatureManager featureService) {
        this.featureService = featureService;
    }

    /**
     * {@link FeatureManager} service unbinding callback
     */
    protected void unsetFeatureService(final FeatureManager featureService) {
        this.featureService = null;
    }

}
