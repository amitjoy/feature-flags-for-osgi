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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.Factory;
import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.web.FeatureFlagsServlet;
import com.amitinside.featureflags.web.util.RequestHelper;
import com.amitinside.featureflags.web.util.RequestHelper.GroupData;
import com.google.gson.Gson;

@Component(name = "FeatureGroupServlet", immediate = true)
public final class FeatureGroupServlet extends HttpServlet implements FeatureFlagsServlet {

    private static final String ALIAS = "groups";
    private static final long serialVersionUID = 7683703693369965631L;

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FeatureService featureService;
    private final Gson gson = new Gson();

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) {
        final List<String> uris = RequestHelper.parseFullUrl(req);
        if (uris.size() == 1 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            //@formatter:off
            final List<GroupData> data = featureService.getGroups()
                                            .map(RequestHelper::mapToGroupData)
                                            .collect(Collectors.toList());
            //@formatter:on
            final String json = gson.toJson(new DataHolder(data));
            if (json == null || json.equalsIgnoreCase("null")) {
                resp.setStatus(SC_NO_CONTENT);
                return;
            }
            try (final PrintWriter writer = resp.getWriter()) {
                resp.setContentType("application/json");
                resp.setStatus(SC_OK);
                writer.write(json);
            } catch (final IOException e) {
                logger.error("{}", e.getMessage(), e);
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }
        if (uris.size() == 2 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            //@formatter:off
            final GroupData data = featureService.getGroup(uris.get(1))
                                        .map(RequestHelper::mapToGroupData)
                                        .orElse(null);
            //@formatter:on
            final String json = gson.toJson(data);
            if (json == null || json.equalsIgnoreCase("null")) {
                resp.setStatus(SC_NO_CONTENT);
                return;
            }
            try (PrintWriter writer = resp.getWriter()) {
                resp.setContentType("application/json");
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

            GroupData json = null;
            try {
                json = gson.fromJson(jsonData, GroupData.class);
            } catch (final Exception e) {
                logger.error("{}", e.getMessage(), e);
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
                return;
            }
            final String name = json.getName();
            final String desc = json.getDescription();
            final String strategy = json.getStrategy();
            final boolean enabled = json.isEnabled();
            final Map<String, Object> properties = json.getProperties();

            //@formatter:off
            final Factory factory = Factory.make(name, c -> c.withDescription(desc)
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

    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        final List<String> uris = RequestHelper.parseFullUrl(req);
        if (uris.size() == 3 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            final String flag = uris.get(2);
            final boolean isEnabled = Boolean.parseBoolean(flag);
            final String name = uris.get(1);
            if (isEnabled) {
                featureService.enableGroup(name);
            } else {
                featureService.disableGroup(name);
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
            final GroupData data = gson.fromJson(jsonData, GroupData.class);
            //@formatter:off
            final Factory factory = Factory.make(uris.get(1), c -> c.withDescription(data.getDescription())
                                                                 .withStrategy(data.getStrategy())
                                                                 .withProperties(data.getProperties())
                                                                 .withEnabled(data.isEnabled())
                                                                 .build());
            //@formatter:on
            final boolean isUpdated = featureService.updateGroup(factory);
            if (isUpdated) {
                resp.setStatus(SC_OK);
            } else {
                resp.setStatus(SC_NOT_MODIFIED);
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

    @Override
    public String getAlias() {
        return "/" + ALIAS;
    }
}
