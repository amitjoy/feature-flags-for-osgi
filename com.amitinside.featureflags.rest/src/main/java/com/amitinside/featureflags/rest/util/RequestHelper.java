/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.rest.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.osgi.dto.DTO;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import com.amitinside.featureflags.Constants;
import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.feature.group.FeatureGroup;
import com.amitinside.featureflags.strategy.ActivationStrategy;
import com.google.common.collect.Maps;

import osgi.enroute.rest.api.RESTRequest;

public final class RequestHelper {

    private static final BundleContext context = FrameworkUtil.getBundle(RequestHelper.class).getBundleContext();

    /** Constructor */
    private RequestHelper() {
        throw new IllegalAccessError("Non-Instantiable");
    }

    /**
     * Class used to represent Strategy JSON data
     */
    public static class StrategyData extends DTO {
        public String name;
        public String description;
        public String type;
        public String key;
        public String value;
    }

    /**
     * Class used to represent Group JSON data
     */
    public static class GroupData extends DTO {
        public String name;
        public String description;
        public String strategy;
        public boolean enabled;
        public Map<String, Object> properties;
    }

    /**
     * Class used to represent Feature Group JSON data
     */
    public static final class FeatureData extends DTO {
        public String name;
        public String description;
        public String strategy;
        public List<String> groups;
        public boolean enabled;
        public Map<String, Object> properties;
    }

    public interface FeatureRequest extends RESTRequest {
        FeatureData _body();
    }

    public interface GroupRequest extends RESTRequest {
        GroupData _body();
    }

    public interface StrategyRequest extends RESTRequest {
        StrategyData _body();
    }

    public static FeatureData mapToFeatureData(final Feature feature) {
        final String name = feature.getName();
        final String strategy = feature.getStrategy().orElse(null);
        final String description = feature.getDescription().orElse(null);
        final List<String> groups = feature.getGroups().collect(Collectors.toList());
        final boolean isEnabled = feature.isEnabled();
        final Map<String, Object> props = Maps
                .newHashMap(ServiceHelper.getServiceProperties(context, feature, Feature.class, null));
        removeProperties(props);
        final FeatureData data = new FeatureData();
        data.name = name;
        data.description = description;
        data.strategy = strategy;
        data.groups = groups;
        data.enabled = isEnabled;
        data.properties = props;
        return data;
    }

    private static void removeProperties(final Map<String, Object> props) {
        props.remove("component.id");
        props.remove("component.name");
        props.remove("service.id");
        props.remove("objectClass");
        props.remove("service.scope");
        props.remove("service.factoryPid");
        props.remove("service.bundleid");
        props.remove("service.pid");
        props.remove("name");
        props.remove("description");
        props.remove("strategy");
        props.remove("groups");
        props.remove("enabled");
    }

    public static GroupData mapToGroupData(final FeatureGroup group) {
        final String name = group.getName();
        final String strategy = group.getStrategy().orElse(null);
        final String description = group.getDescription().orElse(null);
        final boolean isEnabled = group.isEnabled();
        final Map<String, Object> props = Maps
                .newHashMap(ServiceHelper.getServiceProperties(context, group, FeatureGroup.class, null));
        removeProperties(props);
        final GroupData data = new GroupData();
        data.name = name;
        data.description = description;
        data.strategy = strategy;
        data.enabled = isEnabled;
        data.properties = props;
        return data;
    }

    public static StrategyData mapToStrategyData(final ActivationStrategy strategy) {
        final String name = strategy.getName();
        final String description = strategy.getDescription().orElse(null);
        final Map<String, Object> props = Maps
                .newHashMap(ServiceHelper.getServiceProperties(context, strategy, ActivationStrategy.class, null));
        String type = null;
        String key = null;
        String value = null;
        if (props != null) {
            final String factoryPid = (String) props.get("service.factoryPid");
            if (Constants.STRATEGY_SERVICE_PROPERTY_PID.equalsIgnoreCase(factoryPid)) {
                type = "Service";
            } else {
                type = "System";
            }
            key = (String) props.get("property_key");
            value = (String) props.get("property_value");
        }
        final StrategyData data = new StrategyData();
        data.name = name;
        data.description = description;
        data.type = type;
        data.key = key;
        data.value = value;
        return data;
    }
}