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
import com.amitinside.featureflags.web.util.RequestHelper;
import com.amitinside.featureflags.web.util.RequestHelper.FeatureData;
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
        final List<String> uris = RequestHelper.parseFullUrl(req);
        if (uris.size() == 1 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            //@formatter:off
            final List<FeatureData> data = featureService.getFeatures()
                                                .map(f -> RequestHelper.mapToFeatureData(f, featureProperties))
                                                .collect(Collectors.toList());
            //@formatter:on
            final String json = gson.toJson(new DataHolder(data));
            try (final PrintWriter writer = resp.getWriter()) {
                resp.setStatus(SC_OK);
                writer.write(json);
            } catch (final IOException e) {
                logger.error("{}", e.getMessage(), e);
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }
        if (uris.size() == 2 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            final Optional<Feature> feature = featureService.getFeature(uris.get(1));
            //@formatter:off
            final FeatureData data = feature.map(f -> RequestHelper.mapToFeatureData(f, featureProperties))
                                            .orElse(null);
            //@formatter:on
            final String json = gson.toJson(data);
            try (final PrintWriter writer = resp.getWriter()) {
                resp.setStatus(SC_OK);
                writer.write(json);
            } catch (final IOException e) {
                logger.error("{}", e.getMessage(), e);
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) {
        final List<String> uris = RequestHelper.parseFullUrl(req);
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
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) {
        final List<String> uris = RequestHelper.parseFullUrl(req);
        if (uris.size() == 3 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            final String flag = uris.get(2);
            final boolean isEnabled = Boolean.parseBoolean(flag);
            final String name = uris.get(1);
            if (isEnabled) {
                featureService.enableFeature(name);
            } else {
                featureService.disableFeature(name);
            }
            resp.setStatus(SC_OK);
        }
        if (uris.size() == 2 && uris.get(0).equalsIgnoreCase(ALIAS)) {
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
            final Factory factory = Factory.make(uris.get(1), c -> c.withDescription(data.getDescription())
                                                                 .withStrategy(data.getStrategy())
                                                                 .withGroups(data.getGroups())
                                                                 .withProperties(data.getProperties())
                                                                 .withEnabled(data.isEnabled())
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
    }

    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) {
        final List<String> uris = RequestHelper.parseFullUrl(req);
        if (uris.size() == 2 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            final String name = uris.get(1);
            if (featureService.removeGroup(name)) {
                resp.setStatus(SC_OK);
            } else {
                resp.setStatus(SC_NOT_MODIFIED);
            }
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

    @Override
    public String getAlias() {
        return "/" + ALIAS;
    }
}
