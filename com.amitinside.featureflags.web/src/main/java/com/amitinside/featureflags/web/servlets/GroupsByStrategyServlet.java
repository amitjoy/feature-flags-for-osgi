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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.feature.group.FeatureGroup;
import com.amitinside.featureflags.web.FeatureFlagsServlet;
import com.amitinside.featureflags.web.util.RequestHelper;
import com.amitinside.featureflags.web.util.RequestHelper.GroupData;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

@Component(name = "GroupsByStrategyServlet", immediate = true)
public final class GroupsByStrategyServlet extends HttpServlet implements FeatureFlagsServlet {

    private static final String ALIAS = "groupsByStrategy";
    private static final long serialVersionUID = 7683703693369965631L;

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FeatureService featureService;
    private final Gson gson = new Gson();
    private final Map<FeatureGroup, Map<String, Object>> groupsProperties = Maps.newHashMap();

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) {
        final List<String> uris = RequestHelper.parseFullUrl(req);
        if (uris.size() == 2 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            //@formatter:off
            final List<GroupData> data = featureService.getGroupsByStrategy(uris.get(1))
                                            .map(g -> RequestHelper.mapToGroupData(g, groupsProperties))
                                            .collect(Collectors.toList());
            //@formatter:on
            final String json = gson.toJson(new DataHolder(data));
            try (final PrintWriter writer = resp.getWriter()) {
                resp.setStatus(SC_OK);
                writer.write(json);
            } catch (final IOException e) {
                logger.error("{}", e.getMessage(), e);
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
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
     * {@link FeatureGroup} service binding callback
     */
    @Reference(cardinality = MULTIPLE, policy = DYNAMIC)
    protected void bindFeatureGroup(final FeatureGroup group, final Map<String, Object> props) {
        groupsProperties.put(group, props);
    }

    /**
     * {@link FeatureGroup} service unbinding callback
     */
    protected void unbindFeatureGroup(final FeatureGroup group, final Map<String, Object> props) {
        groupsProperties.remove(group);
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
