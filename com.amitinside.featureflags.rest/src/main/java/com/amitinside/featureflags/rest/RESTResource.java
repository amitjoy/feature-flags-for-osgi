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

import static com.amitinside.featureflags.StrategyFactory.StrategyType.*;
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
import com.amitinside.featureflags.StrategyFactory;
import com.amitinside.featureflags.StrategyFactory.StrategyType;
import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.rest.util.RequestHelper;
import com.amitinside.featureflags.rest.util.RequestHelper.FeatureData;
import com.amitinside.featureflags.rest.util.RequestHelper.GroupData;
import com.amitinside.featureflags.rest.util.RequestHelper.StrategyData;
import com.google.gson.Gson;

import osgi.enroute.rest.api.REST;
import osgi.enroute.rest.api.RESTRequest;

@Component(name = "RESTResource", immediate = true)
public final class RESTResource implements REST {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FeatureService featureService;
    private final Gson gson = new Gson();

    public String getFeatures(final RESTRequest req) {
        final HttpServletResponse resp = req._response();
        //@formatter:off
        final List<FeatureData> data = featureService.getFeatures()
                                            .map(RequestHelper::mapToFeatureData)
                                            .collect(Collectors.toList());
        //@formatter:on
        final String json = gson.toJson(new DataHolder<>(data));
        if (json == null || json.equalsIgnoreCase("null")) {
            resp.setStatus(SC_NO_CONTENT);
            return "";
        }
        resp.setContentType("application/json");
        return json;
    }

    public String getFeature(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        final Optional<Feature> feature = featureService.getFeature(name);
        //@formatter:off
        final FeatureData data = feature.map(RequestHelper::mapToFeatureData)
                                        .orElse(null);
        //@formatter:on
        final String json = gson.toJson(data);
        if (json == null || json.equalsIgnoreCase("null")) {
            resp.setStatus(SC_NO_CONTENT);
            return "";
        }
        resp.setContentType("application/json");
        return json;
    }

    public String getFeaturesByGroup(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        //@formatter:off
        final List<FeatureData> data = featureService.getFeaturesByGroup(name)
                                            .map(RequestHelper::mapToFeatureData)
                                            .collect(Collectors.toList());
        //@formatter:on
        final String json = gson.toJson(new DataHolder<>(data));
        if (json == null || json.equalsIgnoreCase("null")) {
            resp.setStatus(SC_NO_CONTENT);
            return "";
        }
        resp.setContentType("application/json");
        return json;
    }

    public String getFeaturesByStrategy(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        //@formatter:off
        final List<FeatureData> data = featureService.getFeaturesByStrategy(name)
                                            .map(RequestHelper::mapToFeatureData)
                                            .collect(Collectors.toList());
        //@formatter:on
        final String json = gson.toJson(new DataHolder<>(data));
        if (json == null || json.equalsIgnoreCase("null")) {
            resp.setStatus(SC_NO_CONTENT);
            return "";
        }
        resp.setContentType("application/json");
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

    public String getGroups(final RESTRequest req) {
        final HttpServletResponse resp = req._response();
        //@formatter:off
        final List<GroupData> data = featureService.getGroups()
                                        .map(RequestHelper::mapToGroupData)
                                        .collect(Collectors.toList());
        //@formatter:on
        final String json = gson.toJson(new DataHolder<>(data));
        if (json == null || json.equalsIgnoreCase("null")) {
            resp.setStatus(SC_NO_CONTENT);
            return "";
        }
        resp.setContentType("application/json");
        return json;
    }

    public String getGroup(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        //@formatter:off
        final GroupData data = featureService.getGroup(name)
                                    .map(RequestHelper::mapToGroupData)
                                    .orElse(null);
        //@formatter:on
        final String json = gson.toJson(data);
        if (json == null || json.equalsIgnoreCase("null")) {
            resp.setStatus(SC_NO_CONTENT);
            return "";
        }
        resp.setContentType("application/json");
        return json;
    }

    public String getGroupsByStrategy(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        //@formatter:off
        final List<GroupData> data = featureService.getGroupsByStrategy(name)
                                            .map(RequestHelper::mapToGroupData)
                                            .collect(Collectors.toList());
        //@formatter:on
        final String json = gson.toJson(new DataHolder<>(data));
        if (json == null || json.equalsIgnoreCase("null")) {
            resp.setStatus(SC_NO_CONTENT);
            return "";
        }
        resp.setContentType("application/json");
        return json;
    }

    public void postGroup(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        String jsonData = null;
        try (final BufferedReader reader = req._request().getReader()) {
            jsonData = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (final IOException e) {
            logger.error("{}", e.getMessage(), e);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            return;
        }
        GroupData json = null;
        try {
            json = gson.fromJson(jsonData, GroupData.class);
        } catch (final Exception e) {
            logger.error("{}", e.getMessage(), e);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            return;
        }
        final String desc = json.getDescription();
        final String strategy = json.getStrategy();
        final boolean enabled = json.isEnabled();
        final Map<String, Object> properties = json.getProperties();

        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make(name, c -> c.withDescription(desc)
                                                             .withStrategy(strategy)
                                                             .withProperties(properties)
                                                             .withEnabled(enabled)
                                                             .build());
        //@formatter:on
        final Optional<String> pid = featureService.createGroup(factory);
        if (pid.isPresent()) {
            try (final PrintWriter writer = resp.getWriter()) {
                resp.setStatus(SC_CREATED);
                writer.write(pid.get());
            } catch (final IOException e) {
                logger.error("{}", e.getMessage(), e);
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            resp.setStatus(SC_NO_CONTENT);
        }
    }

    public void putGroup(final RESTRequest req, final String name, final boolean flag) {
        if (flag) {
            featureService.enableGroup(name);
        } else {
            featureService.disableGroup(name);
        }
    }

    public void putGroup(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        String jsonData = null;
        try (final BufferedReader reader = req._request().getReader()) {
            jsonData = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (final IOException e) {
            logger.error("{}", e.getMessage(), e);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            return;
        }
        GroupData json = null;
        try {
            json = gson.fromJson(jsonData, GroupData.class);
        } catch (final Exception e) {
            logger.error("{}", e.getMessage(), e);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            return;
        }
        final String desc = json.getDescription();
        final String strategy = json.getStrategy();
        final boolean enabled = json.isEnabled();
        final Map<String, Object> properties = json.getProperties();
        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make(name, c -> c.withDescription(desc)
                                                             .withStrategy(strategy)
                                                             .withProperties(properties)
                                                             .withEnabled(enabled)
                                                             .build());
        //@formatter:on
        final boolean isUpdated = featureService.updateGroup(factory);
        if (isUpdated) {
            resp.setStatus(SC_OK);
        } else {
            resp.setStatus(SC_NOT_MODIFIED);
        }
    }

    public void deleteGroup(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        if (featureService.removeGroup(name)) {
            resp.setStatus(SC_OK);
        } else {
            resp.setStatus(SC_NOT_MODIFIED);
        }
    }

    public String getStrategies(final RESTRequest req) {
        final HttpServletResponse resp = req._response();
        //@formatter:off
        final List<StrategyData> data = featureService.getStrategies()
                                                .map(RequestHelper::mapToStrategyData)
                                                .collect(Collectors.toList());
        //@formatter:on
        final String json = gson.toJson(new DataHolder<>(data));
        if (json == null || json.equalsIgnoreCase("null")) {
            resp.setStatus(SC_NO_CONTENT);
            return "";
        }
        resp.setContentType("application/json");
        return json;
    }

    public String getStrategy(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        //@formatter:off
        final StrategyData data = featureService.getStrategy(name)
                                            .map(RequestHelper::mapToStrategyData)
                                            .orElse(null);
        //@formatter:on
        final String json = gson.toJson(data);
        if (json == null || json.equalsIgnoreCase("null")) {
            resp.setStatus(SC_NO_CONTENT);
            return "";
        }
        resp.setContentType("application/json");
        return json;
    }

    public void postStrategy(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        String jsonData = null;
        try (final BufferedReader reader = req._request().getReader()) {
            jsonData = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (final IOException e) {
            logger.error("{}", e.getMessage(), e);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            return;
        }
        StrategyData json = null;
        try {
            json = gson.fromJson(jsonData, StrategyData.class);
        } catch (final Exception e) {
            logger.error("{}", e.getMessage(), e);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            return;
        }
        final StrategyType type = json.getType().equalsIgnoreCase("system") ? SYSTEM_PROPERTY : SERVICE_PROPERTY;
        final String desc = json.getDescription();
        final String key = json.getKey();
        final String value = json.getValue();
        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make(name, type,
                                                          c -> c.withDescription(desc)
                                                                .withKey(key)
                                                                .withValue(value)
                                                                .build());
        //@formatter:on
        final Optional<String> pid = featureService.createPropertyBasedStrategy(factory);
        if (pid.isPresent()) {
            try (PrintWriter writer = resp.getWriter()) {
                resp.setStatus(SC_CREATED);
                writer.write(pid.get());
            } catch (final IOException e) {
                logger.error("{}", e.getMessage(), e);
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
                return;
            }
        } else {
            resp.setStatus(SC_NO_CONTENT);
        }
    }

    public void deleteStrategy(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        if (featureService.removePropertyBasedStrategy(name)) {
            resp.setStatus(SC_OK);
        } else {
            resp.setStatus(SC_NOT_MODIFIED);
        }
    }

    public void putStrategy(final RESTRequest req, final String name) {
        String jsonData = null;
        final HttpServletResponse resp = req._response();
        try (final BufferedReader reader = req._request().getReader()) {
            jsonData = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (final IOException e) {
            logger.error("{}", e.getMessage(), e);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            return;
        }
        StrategyData json = null;
        try {
            json = gson.fromJson(jsonData, StrategyData.class);
        } catch (final Exception e) {
            logger.error("{}", e.getMessage(), e);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            return;
        }
        final StrategyType type = json.getType().equalsIgnoreCase("system") ? SYSTEM_PROPERTY : SERVICE_PROPERTY;
        final String desc = json.getDescription();
        final String key = json.getKey();
        final String value = json.getValue();
        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make(name, type,
                                                          c -> c.withDescription(desc)
                                                                .withKey(key)
                                                                .withValue(value)
                                                                .build());
        //@formatter:on
        final boolean isUpdated = featureService.updatePropertyBasedStrategy(factory);
        if (isUpdated) {
            resp.setStatus(SC_OK);
        } else {
            resp.setStatus(SC_NO_CONTENT);
            return;
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

    private static final class DataHolder<T> {
        private final List<T> elements;

        public DataHolder(final List<T> elements) {
            this.elements = elements;
        }

        @SuppressWarnings("unused")
        public List<T> getElements() {
            return elements;
        }
    }

}
