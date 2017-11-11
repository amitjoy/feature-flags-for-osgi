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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.amitinside.featureflags.FeatureManager;
import com.amitinside.featureflags.dto.ConfigurationDTO;
import com.amitinside.featureflags.dto.FeatureDTO;

@Path("/featureflags")
@Component(name = "FeatureFlagsRESTResource", immediate = true)
public final class RESTResource {

    private FeatureManager featureService;

    @GET
    @Path("/configurations")
    @Produces(APPLICATION_JSON)
    public List<ConfigurationDTO> getConfigurations() {
        return featureService.getConfigurations().collect(Collectors.toList());
    }

    @GET
    @Path("/features")
    @Produces(APPLICATION_JSON)
    public List<FeatureDTO> getFeatures(final String configurationPID) {
        return featureService.getFeatures(configurationPID).collect(Collectors.toList());
    }

    @GET
    @Path("/configuration")
    @Produces(APPLICATION_JSON)
    public ConfigurationDTO getConfiguration(final String configurationPID) {
        return featureService.getConfiguration(configurationPID).orElse(null);
    }

    @GET
    @Path("/feature")
    @Produces(APPLICATION_JSON)
    public FeatureDTO getFeature(final String configurationPID, final String featureName) {
        return featureService.getFeature(configurationPID, featureName).orElse(null);
    }

    @PUT
    @Path("/feature")
    public boolean putFeature(final String configurationPID, final String featureName, final boolean isEnabled) {
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
