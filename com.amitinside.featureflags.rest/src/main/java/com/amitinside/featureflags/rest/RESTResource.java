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
import com.amitinside.featureflags.rest.util.RequestHelper.FeatureRequest;
import com.amitinside.featureflags.rest.util.RequestHelper.GroupData;
import com.amitinside.featureflags.rest.util.RequestHelper.GroupRequest;
import com.amitinside.featureflags.rest.util.RequestHelper.StrategyData;
import com.amitinside.featureflags.rest.util.RequestHelper.StrategyRequest;

import osgi.enroute.rest.api.REST;
import osgi.enroute.rest.api.RESTRequest;

@Component(name = "RESTResource", immediate = true)
public final class RESTResource implements REST {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FeatureService featureService;

    public List<FeatureData> getFeatures(final RESTRequest req) {
        //@formatter:off
        return featureService.getFeatures()
                             .map(RequestHelper::mapToFeatureData)
                             .collect(Collectors.toList());
        //@formatter:on
    }

    public FeatureData getFeatures(final RESTRequest req, final String name) {
        final Optional<Feature> feature = featureService.getFeature(name);
        //@formatter:off
        return feature.map(RequestHelper::mapToFeatureData)
                      .orElse(null);
        //@formatter:on
    }

    public List<FeatureData> getFeaturesByGroup(final RESTRequest req, final String name) {
        //@formatter:off
        return featureService.getFeaturesByGroup(name)
                             .map(RequestHelper::mapToFeatureData)
                             .collect(Collectors.toList());
        //@formatter:on
    }

    public List<FeatureData> getFeaturesByStrategy(final RESTRequest req, final String name) {
        //@formatter:off
        return featureService.getFeaturesByStrategy(name)
                             .map(RequestHelper::mapToFeatureData)
                             .collect(Collectors.toList());
        //@formatter:on
    }

    public void postFeatures(final FeatureRequest req) {
        final HttpServletResponse resp = req._response();
        final FeatureData json = req._body();
        final String description = json.description;
        final String strategy = json.strategy;
        final List<String> groups = json.groups;
        final Map<String, Object> properties = json.properties;
        final boolean enabled = json.enabled;
        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make(json.name, c -> c.withDescription(description)
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

    public void putFeatures(final RESTRequest req, final String name, final boolean flag) {
        if (flag) {
            featureService.enableFeature(name);
        } else {
            featureService.disableFeature(name);
        }
    }

    public void putFeatures(final FeatureRequest req) {
        final HttpServletResponse resp = req._response();
        final FeatureData json = req._body();
        final String description = json.description;
        final String strategy = json.strategy;
        final List<String> groups = json.groups;
        final Map<String, Object> properties = json.properties;
        final boolean enabled = json.enabled;
        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make(json.name, c -> c.withDescription(description)
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
        }
    }

    public void deleteFeatures(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        if (featureService.removeFeature(name)) {
            resp.setStatus(SC_OK);
        } else {
            resp.setStatus(SC_NOT_MODIFIED);
        }
    }

    public List<GroupData> getGroups(final RESTRequest req) {
        //@formatter:off
        return featureService.getGroups()
                             .map(RequestHelper::mapToGroupData)
                             .collect(Collectors.toList());
        //@formatter:on
    }

    public GroupData getGroups(final RESTRequest req, final String name) {
        //@formatter:off
        return featureService.getGroup(name)
                             .map(RequestHelper::mapToGroupData)
                             .orElse(null);
        //@formatter:on
    }

    public List<GroupData> getGroupsByStrategy(final RESTRequest req, final String name) {
        //@formatter:off
        return featureService.getGroupsByStrategy(name)
                             .map(RequestHelper::mapToGroupData)
                             .collect(Collectors.toList());
        //@formatter:on
    }

    public void postGroups(final GroupRequest req) {
        final HttpServletResponse resp = req._response();
        final GroupData json = req._body();
        final String desc = json.description;
        final String strategy = json.strategy;
        final boolean enabled = json.enabled;
        final Map<String, Object> properties = json.properties;

        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make(json.name, c -> c.withDescription(desc)
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

    public void putGroups(final RESTRequest req, final String name, final boolean flag) {
        if (flag) {
            featureService.enableGroup(name);
        } else {
            featureService.disableGroup(name);
        }
    }

    public void putGroups(final GroupRequest req) {
        final HttpServletResponse resp = req._response();
        final GroupData json = req._body();
        final String desc = json.description;
        final String strategy = json.strategy;
        final boolean enabled = json.enabled;
        final Map<String, Object> properties = json.properties;
        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make(json.name, c -> c.withDescription(desc)
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

    public void deleteGroups(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        if (featureService.removeGroup(name)) {
            resp.setStatus(SC_OK);
        } else {
            resp.setStatus(SC_NOT_MODIFIED);
        }
    }

    public List<StrategyData> getStrategies(final RESTRequest req) {
        //@formatter:off
        return featureService.getStrategies()
                             .map(RequestHelper::mapToStrategyData)
                             .collect(Collectors.toList());
        //@formatter:on
    }

    public StrategyData getStrategies(final RESTRequest req, final String name) {
        //@formatter:off
        return featureService.getStrategy(name)
                             .map(RequestHelper::mapToStrategyData)
                             .orElse(null);
        //@formatter:on
    }

    public void postStrategies(final StrategyRequest req) {
        final HttpServletResponse resp = req._response();
        final StrategyData json = req._body();
        final StrategyType type = json.type.equalsIgnoreCase("system") ? SYSTEM_PROPERTY : SERVICE_PROPERTY;
        final String desc = json.description;
        final String key = json.key;
        final String value = json.value;
        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make(json.name, type,
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
            }
        } else {
            resp.setStatus(SC_NO_CONTENT);
        }
    }

    public void deleteStrategies(final RESTRequest req, final String name) {
        final HttpServletResponse resp = req._response();
        if (featureService.removePropertyBasedStrategy(name)) {
            resp.setStatus(SC_OK);
        } else {
            resp.setStatus(SC_NOT_MODIFIED);
        }
    }

    public void putStrategies(final StrategyRequest req) {
        final HttpServletResponse resp = req._response();
        final StrategyData json = req._body();
        final StrategyType type = json.type.equalsIgnoreCase("system") ? SYSTEM_PROPERTY : SERVICE_PROPERTY;
        final String desc = json.description;
        final String key = json.key;
        final String value = json.value;
        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make(json.name, type,
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

}
