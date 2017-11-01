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
import static javax.servlet.http.HttpServletResponse.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.StrategizableFactory;
import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.rest.util.RequestHelper;
import com.amitinside.featureflags.rest.util.RequestHelper.FeatureData;
import com.google.gson.Gson;

import osgi.enroute.rest.api.REST;
import osgi.enroute.rest.api.RESTRequest;

@Component(name = "FeatureRESTResource", immediate = true)
public final class FeatureRESTResource implements REST {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FeatureService featureService;
    private final Gson gson = new Gson();

    public String getFeatures(final RESTRequest req) {
        //@formatter:off
        final List<FeatureData> data = featureService.getFeatures()
                                            .map(RequestHelper::mapToFeatureData)
                                            .collect(Collectors.toList());
        //@formatter:on
        final String json = gson.toJson(new DataHolder(data));
        checkArgument(json == null || json.equalsIgnoreCase("null"));
        return json;
    }

    public String getFeature(final RESTRequest req, final String name) {
        final Optional<Feature> feature = featureService.getFeature(name);
        //@formatter:off
        final FeatureData data = feature.map(RequestHelper::mapToFeatureData)
                                        .orElse(null);
        //@formatter:on
        final String json = gson.toJson(data);
        checkArgument(json == null || json.equalsIgnoreCase("null"));
        return json;
    }

    public void postFeature(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        String jsonData = null;
        try (final BufferedReader reader = req._request().getReader()) {
            jsonData = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (final IOException e) {
            logger.error("{}", e.getMessage(), e);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            return;
        }
        FeatureData json = null;
        try {
            json = gson.fromJson(jsonData, FeatureData.class);
        } catch (final Exception e) {
            logger.error("{}", e.getMessage(), e);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            return;
        }
        final String description = json.getDescription();
        final String strategy = json.getStrategy();
        final List<String> groups = json.getGroups();
        final Map<String, Object> properties = json.getProperties();
        final boolean enabled = json.isEnabled();
        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make(json.getName(), c -> c.withDescription(description)
                                                             .withStrategy(strategy)
                                                             .withGroups(groups)
                                                             .withProperties(properties)
                                                             .withEnabled(enabled)
                                                             .build());
        //@formatter:on
        final Optional<String> pid = featureService.createFeature(factory);
        if (pid.isPresent()) {
            try (PrintWriter writer = resp.getWriter()) {
                resp.setStatus(SC_CREATED);
                writer.write(pid.get());
            } catch (final IOException ex) {
                logger.error("{}", ex.getMessage(), ex);
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            resp.setStatus(SC_NO_CONTENT);
        }
    }

    public void putFeature(final RESTRequest req, final String name, final boolean flag) {
        if (flag) {
            featureService.enableFeature(name);
        } else {
            featureService.disableFeature(name);
        }
    }

    public void putFeature(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        String jsonData = null;
        try (final BufferedReader reader = req._request().getReader()) {
            jsonData = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (final IOException e) {
            logger.error("{}", e.getMessage(), e);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            return;
        }
        FeatureData json = null;
        try {
            json = gson.fromJson(jsonData, FeatureData.class);
        } catch (final Exception e) {
            logger.error("{}", e.getMessage(), e);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            return;
        }
        final String description = json.getDescription();
        final String strategy = json.getStrategy();
        final List<String> groups = json.getGroups();
        final Map<String, Object> properties = json.getProperties();
        final boolean enabled = json.isEnabled();
        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make(name, c -> c.withDescription(description)
                                                             .withStrategy(strategy)
                                                             .withGroups(groups)
                                                             .withProperties(properties)
                                                             .withEnabled(enabled)
                                                             .build());
        //@formatter:on
        final boolean isUpdated = featureService.updateFeature(factory);
        if (isUpdated) {
            resp.setStatus(SC_OK);
        } else {
            resp.setStatus(SC_NOT_MODIFIED);
            return;
        }
    }

    public void deleteFeature(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        if (featureService.removeFeature(name)) {
            resp.setStatus(SC_OK);
        } else {
            resp.setStatus(SC_NOT_MODIFIED);
        }
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
