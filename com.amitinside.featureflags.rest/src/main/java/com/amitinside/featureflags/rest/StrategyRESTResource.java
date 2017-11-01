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
import static com.google.common.base.Preconditions.checkArgument;
import static javax.servlet.http.HttpServletResponse.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.StrategyFactory;
import com.amitinside.featureflags.StrategyFactory.StrategyType;
import com.amitinside.featureflags.rest.util.RequestHelper;
import com.amitinside.featureflags.rest.util.RequestHelper.StrategyData;
import com.google.gson.Gson;

import osgi.enroute.rest.api.REST;
import osgi.enroute.rest.api.RESTRequest;

@Component(name = "StrategyRESTResource", immediate = true)
public final class StrategyRESTResource implements REST {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FeatureService featureService;
    private final Gson gson = new Gson();

    public String getStrategies(final RESTRequest req) {
        //@formatter:off
        final List<StrategyData> data = featureService.getStrategies()
                                                .map(RequestHelper::mapToStrategyData)
                                                .collect(Collectors.toList());
        //@formatter:on
        final String json = gson.toJson(new DataHolder(data));
        checkArgument(json == null || json.equalsIgnoreCase("null"));
        return json;
    }

    public String getStrategy(final RESTRequest req, final String name) {
        //@formatter:off
        final StrategyData data = featureService.getStrategy(name)
                                            .map(RequestHelper::mapToStrategyData)
                                            .orElse(null);
        //@formatter:on
        final String json = gson.toJson(data);
        checkArgument(json == null || json.equalsIgnoreCase("null"));
        return json;
    }

    public void postStrategy(final RESTRequest req) {
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
        final String name = json.getName();
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

    interface Request extends RESTRequest {
        DataHolder _body();
    }

    private static final class DataHolder {
        private final List<StrategyData> strategies;

        public DataHolder(final List<StrategyData> strategies) {
            this.strategies = strategies;
        }

        @SuppressWarnings("unused")
        public List<StrategyData> getFeatures() {
            return strategies;
        }
    }

}
