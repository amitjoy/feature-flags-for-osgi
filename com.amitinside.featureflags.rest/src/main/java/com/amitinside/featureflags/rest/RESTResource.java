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
@SuppressWarnings("unused")
public final class RESTResource {

    private FeatureManager featureService;

    @GET
    @Path("/configurations")
    @Produces(APPLICATION_JSON)
    public List<Configuration> getConfigurations() {
        return featureService.getConfigurations().map(this::convertDTO).collect(Collectors.toList());
    }

    @GET
    @Path("/features")
    @Produces(APPLICATION_JSON)
    public List<Feature> getFeatures(final String configurationPID) {
        return featureService.getFeatures(configurationPID).map(this::convertDTO).collect(Collectors.toList());
    }

    @GET
    @Path("/configuration")
    @Produces(APPLICATION_JSON)
    public Configuration getConfiguration(final String configurationPID) {
        return featureService.getConfiguration(configurationPID).map(this::convertDTO).orElse(null);
    }

    @GET
    @Path("/feature")
    @Produces(APPLICATION_JSON)
    public Feature getFeature(final String configurationPID, final String featureName) {
        return featureService.getFeature(configurationPID, featureName).map(this::convertDTO).orElse(null);
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

    private Feature convertDTO(final FeatureDTO dto) {
        final Feature feature = new Feature();
        feature.name = dto.name;
        feature.description = dto.description;
        feature.isEnabled = dto.isEnabled;
        return feature;
    }

    private Configuration convertDTO(final ConfigurationDTO dto) {
        final Configuration config = new Configuration();
        config.pid = dto.pid;
        config.features = dto.features.stream().map(this::convertDTO).collect(Collectors.toList());
        return config;
    }

    private static class Configuration {
        public String pid;
        public List<Feature> features;
    }

    private static class Feature {
        public String name;
        public String description;
        public boolean isEnabled;
    }

}
