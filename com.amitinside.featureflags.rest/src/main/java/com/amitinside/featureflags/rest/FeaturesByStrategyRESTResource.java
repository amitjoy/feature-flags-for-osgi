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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.rest.util.RequestHelper;
import com.amitinside.featureflags.rest.util.RequestHelper.FeatureData;
import com.google.gson.Gson;

import osgi.enroute.rest.api.REST;
import osgi.enroute.rest.api.RESTRequest;

@Component(name = "FeaturesByStrategyRESTResource", immediate = true)
public final class FeaturesByStrategyRESTResource implements REST {

    private FeatureService featureService;
    private final Gson gson = new Gson();

    public String getFeatures(final RESTRequest req, final String name) {
        //@formatter:off
        final List<FeatureData> data = featureService.getFeaturesByStrategy(name)
                                            .map(RequestHelper::mapToFeatureData)
                                            .collect(Collectors.toList());
        //@formatter:on
        final String json = gson.toJson(new DataHolder(data));
        checkArgument(json == null || json.equalsIgnoreCase("null"));
        return json;
    }

    /**
     * {@link FeatureService} service binding callback
     */
    @Reference
    protected void setFeatureService(final FeatureService featureService) {
        this.featureService = featureService;
    }

    /**
     * {@link FeatureService} service unbinding callback
     */
    protected void unsetFeatureService(final FeatureService featureService) {
        this.featureService = null;
    }

    private static final class DataHolder {
        private final List<FeatureData> features;

        public DataHolder(final List<FeatureData> features) {
            this.features = features;
        }

        @SuppressWarnings("unused")
        public List<FeatureData> getFeatures() {
            return features;
        }
    }
}
