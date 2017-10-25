/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.web.servlets;

import static javax.servlet.http.HttpServletResponse.*;
import static org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.Factory;
import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.web.FeatureFlagsServlet;
import com.amitinside.featureflags.web.util.HttpServletRequestHelper;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

@Component(name = "FeatureServlet", immediate = true)
public final class FeatureServlet extends HttpServlet implements FeatureFlagsServlet {

    private static final String ALIAS = "features";
    private static final long serialVersionUID = 7683703693369965631L;

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FeatureService featureService;
    private final Gson gson = new Gson();
    private final Map<Feature, Map<String, Object>> featureProperties = Maps.newHashMap();

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) {
        final List<String> uris = HttpServletRequestHelper.parseFullUrl(req);
        if (uris.size() == 1 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            final List<FeatureData> data = featureService.getFeatures().map(this::mapToFeatureData)
                    .collect(Collectors.toList());
            final String json = gson.toJson(new DataHolder(data));
            try (final PrintWriter writer = resp.getWriter()) {
                writer.write(json);
                resp.setStatus(SC_OK);
            } catch (final IOException e) {
                logger.error("{}", e.getMessage(), e);
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }
        if (uris.size() == 2 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            final Optional<Feature> feature = featureService.getFeature(uris.get(1));
            final FeatureData data = feature.map(this::mapToFeatureData).orElse(null);

            final String json = gson.toJson(data);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(json);
                resp.setStatus(SC_OK);
            } catch (final IOException e) {
                logger.error("{}", e.getMessage(), e);
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) {
        final List<String> uris = HttpServletRequestHelper.parseFullUrl(req);
        if (uris.size() == 1 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            String jsonData = null;
            try (final BufferedReader reader = req.getReader()) {
                jsonData = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            } catch (final IOException e) {
                logger.error("{}", e.getMessage(), e);
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
                return;
            }
            final FeatureData data = gson.fromJson(jsonData, FeatureData.class);
            //@formatter:off
            final Factory factory = Factory.make(data.getName(), c -> c.withDescription(data.getDescription())
                                                                 .withStrategy(data.getStrategy())
                                                                 .withGroups(data.getGroups())
                                                                 .withProperties(data.getProperties())
                                                                 .withEnabled(data.isEnabled())
                                                                 .build());
            //@formatter:on
            final Optional<String> pid = featureService.createFeature(factory);
            if (pid.isPresent()) {
                try (PrintWriter writer = resp.getWriter()) {
                    writer.write(pid.get());
                    resp.setStatus(SC_OK);
                } catch (final IOException ex) {
                    logger.error("{}", ex.getMessage(), ex);
                    resp.setStatus(SC_INTERNAL_SERVER_ERROR);
                    return;
                }
            } else {
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            }
        }

    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) {
        final List<String> uris = HttpServletRequestHelper.parseFullUrl(req);
        if (uris.size() == 3 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            final String flag = uris.get(2);
            final boolean isEnabled = Boolean.parseBoolean(flag);
            final String name = uris.get(1);
            if (isEnabled) {
                featureService.enableFeature(name);
            } else {
                featureService.disableFeature(name);
            }
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) {
        final List<String> uris = HttpServletRequestHelper.parseFullUrl(req);
        if (uris.size() == 2 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            final String name = uris.get(1);
            featureService.removeFeature(name);
            resp.setStatus(SC_OK);
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

    /**
     * {@link Feature} service binding callback
     */
    @Reference(cardinality = MULTIPLE, policy = DYNAMIC)
    protected void bindFeature(final Feature feature, final Map<String, Object> props) {
        featureProperties.put(feature, props);
    }

    /**
     * {@link Feature} service unbinding callback
     */
    protected void unbindFeature(final Feature feature, final Map<String, Object> props) {
        featureProperties.remove(feature);
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

    /**
     * Internal class used to represent Feature Group JSON data
     */
    private static final class FeatureData {
        private final String name;
        private final String description;
        private final String strategy;
        private final List<String> groups;
        private final boolean enabled;
        private final Map<String, Object> properties;

        public FeatureData(final String name, final String description, final String strategy,
                final List<String> groups, final boolean enabled, final Map<String, Object> properties) {
            this.name = name;
            this.description = description;
            this.strategy = strategy;
            this.groups = groups;
            this.enabled = enabled;
            this.properties = properties;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getStrategy() {
            return strategy;
        }

        public List<String> getGroups() {
            return groups;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    private FeatureData mapToFeatureData(final Feature feature) {
        final String name = feature.getName();
        final String strategy = feature.getStrategy().orElse(null);
        final String description = feature.getDescription().orElse(null);
        final List<String> groups = feature.getGroups().collect(Collectors.toList());
        final boolean isEnabled = feature.isEnabled();
        return new FeatureData(name, description, strategy, groups, isEnabled, featureProperties.get(feature));
    }

    @Override
    public String getAlias() {
        return "/" + ALIAS;
    }
}
