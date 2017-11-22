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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.amitinside.featureflags.FeatureManager;
import com.amitinside.featureflags.dto.FeatureDTO;

/**
 * This class exposes REST Resource Endpoints to manage features
 * <p>
 * The URI patterns are as follows:
 * <ul>
 * <li>GET: /featureflags/features</li>
 * <li>GET: /featureflags/features/{featureID}</li>
 * <li>PUT: /featureflags/features/{featureID}?isEnabled=true</li>
 * </ul>
 * </p>
 */
@Path("featureflags")
@Component(name = "FeatureFlagsRESTResource", immediate = true, service = FeatureFlagsRESTResource.class)
public final class FeatureFlagsRESTResource {

    private FeatureManager featureManager;

    @GET
    @Path("/features")
    @Produces(APPLICATION_JSON)
    public List<FeatureDTO> getFeatures() {
        return featureManager.getFeatures().collect(Collectors.toList());
    }

    @GET
    @Path("/features/{featureID}")
    @Produces(APPLICATION_JSON)
    public List<FeatureDTO> getFeatures(@PathParam("featureID") final String featureID) {
        return featureManager.getFeatures(featureID).collect(Collectors.toList());
    }

    @PUT
    @Path("/features/{featureID}")
    public void updateFeature(@PathParam("featureID") final String featureID,
            @QueryParam("isEnabled") final boolean isEnabled) {
        featureManager.updateFeature(featureID, isEnabled);
    }

    /**
     * {@link FeatureManager} service binding callback
     */
    @Reference
    protected void setFeatureService(final FeatureManager featureManager) {
        this.featureManager = featureManager;
    }

    /**
     * {@link FeatureManager} service unbinding callback
     */
    protected void unsetFeatureService(final FeatureManager featureManager) {
        this.featureManager = null;
    }

}
