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
import com.amitinside.featureflags.rest.util.RequestHelper;
import com.amitinside.featureflags.rest.util.RequestHelper.GroupData;
import com.google.gson.Gson;

import osgi.enroute.rest.api.REST;
import osgi.enroute.rest.api.RESTRequest;

@Component(name = "FeatureGroupRESTResource", immediate = true)
public final class FeatureGroupRESTResource implements REST {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FeatureService featureService;
    private final Gson gson = new Gson();

    public String getGroups(final RESTRequest req) {
        //@formatter:off
        final List<GroupData> data = featureService.getGroups()
                                        .map(RequestHelper::mapToGroupData)
                                        .collect(Collectors.toList());
        //@formatter:on
        final String json = gson.toJson(new DataHolder(data));
        checkArgument(json == null || json.equalsIgnoreCase("null"));
        return json;
    }

    public String getGroup(final RESTRequest req, final String name) {
        //@formatter:off
        final GroupData data = featureService.getGroup(name)
                                    .map(RequestHelper::mapToGroupData)
                                    .orElse(null);
        //@formatter:on
        final String json = gson.toJson(data);
        checkArgument(json == null || json.equalsIgnoreCase("null"));
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
        private final List<GroupData> groups;

        public DataHolder(final List<GroupData> groups) {
            this.groups = groups;
        }

        @SuppressWarnings("unused")
        public List<GroupData> getGroups() {
            return groups;
        }
    }

}
