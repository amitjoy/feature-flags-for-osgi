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
import com.amitinside.featureflags.feature.group.FeatureGroup;
import com.amitinside.featureflags.web.FeatureFlagsServlet;
import com.amitinside.featureflags.web.util.HttpServletRequestHelper;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

@Component(name = "FeatureGroupServlet", immediate = true)
public final class FeatureGroupServlet extends HttpServlet implements FeatureFlagsServlet {

    private static final String ALIAS = "groups";
    private static final long serialVersionUID = 7683703693369965631L;

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FeatureService featureService;
    private final Gson gson = new Gson();
    private final Map<FeatureGroup, Map<String, Object>> groupProperties = Maps.newHashMap();

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) {
        final List<String> uris = HttpServletRequestHelper.parseFullUrl(req);
        if (uris.size() == 1 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            final List<GroupData> data = featureService.getGroups().map(this::mapToGroupData)
                    .collect(Collectors.toList());
            final String json = gson.toJson(new DataHolder(data));
            resp.setStatus(SC_OK);
            try (final PrintWriter writer = resp.getWriter()) {
                writer.write(json);
            } catch (final IOException e) {
                logger.error("{}", e.getMessage(), e);
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }
        if (uris.size() == 2 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            final GroupData data = featureService.getGroup(uris.get(1)).map(this::mapToGroupData).orElse(null);

            final String json = gson.toJson(data);
            resp.setStatus(SC_OK);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(json);
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
            final GroupData data = gson.fromJson(jsonData, GroupData.class);
            //@formatter:off
            final Factory factory = Factory.make(data.getName(), c -> c.withDescription(data.getDescription())
                                                                 .withStrategy(data.getStrategy())
                                                                 .withProperties(data.getProperties())
                                                                 .withEnabled(data.isEnabled())
                                                                 .build());
            //@formatter:on
            final Optional<String> pid = featureService.createGroup(factory);
            if (pid.isPresent()) {
                resp.setStatus(HttpServletResponse.SC_OK);
                try (PrintWriter writer = resp.getWriter()) {
                    writer.write(pid.get());
                } catch (final IOException e) {
                    logger.error("{}", e.getMessage(), e);
                    resp.setStatus(SC_INTERNAL_SERVER_ERROR);
                    return;
                }
            } else {
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            }
        }

    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        final List<String> uris = HttpServletRequestHelper.parseFullUrl(req);
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
     * {@link FeatureGroup} service binding callback
     */
    @Reference(cardinality = MULTIPLE, policy = DYNAMIC)
    protected void bindFeatureGroup(final FeatureGroup group, final Map<String, Object> props) {
        groupProperties.put(group, props);
    }

    /**
     * {@link FeatureGroup} service unbinding callback
     */
    protected void unbindFeatureGroup(final FeatureGroup group, final Map<String, Object> props) {
        groupProperties.remove(group);
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

    /**
     * Internal class used to represent Group JSON data
     */
    private static final class GroupData {
        private final String name;
        private final String description;
        private final String strategy;
        private final boolean enabled;
        private final Map<String, Object> properties;

        public GroupData(final String name, final String description, final String strategy, final boolean enabled,
                final Map<String, Object> properties) {
            this.name = name;
            this.description = description;
            this.strategy = strategy;
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

        public Map<String, Object> getProperties() {
            return properties;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    private GroupData mapToGroupData(final FeatureGroup group) {
        final String name = group.getName();
        final String strategy = group.getStrategy().orElse(null);
        final String description = group.getDescription().orElse(null);
        final boolean isEnabled = group.isEnabled();
        return new GroupData(name, description, strategy, isEnabled, groupProperties.get(group));
    }

    @Override
    public String getAlias() {
        return "/" + ALIAS;
    }
}
