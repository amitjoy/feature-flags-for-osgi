/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;

import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.feature.group.FeatureGroup;
import com.amitinside.featureflags.strategy.ActivationStrategy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings({ "unchecked", "rawtypes" })
public final class ConfigurationAdminMock implements ConfigurationAdmin {

    private final FeatureManager manager;
    private final ServiceReference reference;
    private final List<ConfigurationListener> listeners = Lists.newCopyOnWriteArrayList();
    private final Object instance;

    public ConfigurationAdminMock(final FeatureManager manager, final ServiceReference reference,
            final Object instance) {
        this.manager = manager;
        this.reference = reference;
        this.instance = instance;
    }

    @Override
    public Configuration createFactoryConfiguration(final String factoryPid) throws IOException {
        return createFactoryConfiguration(factoryPid, null);
    }

    @Override
    public Configuration createFactoryConfiguration(final String factoryPid, final String location) throws IOException {
        return new ConfigurationMock(UUID.randomUUID().toString(), manager, reference, instance);
    }

    @Override
    public Configuration getConfiguration(final String pid, final String location) throws IOException {
        return new ConfigurationMock(pid, manager, reference, instance);
    }

    @Override
    public Configuration getConfiguration(final String pid) throws IOException {
        return getConfiguration(pid, null);
    }

    @Override
    public Configuration[] listConfigurations(final String filter) throws IOException, InvalidSyntaxException {
        return null;
    }

    public void addListener(final ConfigurationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final ConfigurationListener listener) {
        listeners.remove(listener);
    }

    public final class ConfigurationMock implements Configuration {

        private final String name;
        private final FeatureManager manager;
        private final ServiceReference reference;
        private final Object instance;

        public ConfigurationMock(final String name, final FeatureManager manager, final ServiceReference reference,
                final Object instance) {
            this.name = name;
            this.manager = manager;
            this.reference = reference;
            this.instance = instance;
        }

        @Override
        public String getPid() {
            return name;
        }

        @Override
        public Dictionary getProperties() {
            return null;
        }

        @Override
        public void update(final Dictionary properties) throws IOException {
            final Enumeration<String> enums = properties.keys();
            boolean isFeature = false;
            boolean isGroup = false;
            boolean isStrategy = false;
            Feature f = null;
            FeatureGroup g = null;
            ActivationStrategy s = null;
            if (instance instanceof Feature) {
                isFeature = true;
                f = (Feature) instance;
            }
            if (instance instanceof FeatureGroup) {
                isGroup = true;
                g = (FeatureGroup) instance;
            }
            if (instance instanceof ActivationStrategy) {
                isStrategy = true;
                s = (ActivationStrategy) instance;
            }
            Feature newFeature = null;
            FeatureGroup newGroup = null;
            final Map<String, Object> otherProps = Maps.newHashMap();
            while (enums.hasMoreElements()) {
                final String key = enums.nextElement();
                final Object value = properties.get(key);
                if (key.equalsIgnoreCase("enabled")) {
                    newFeature = updateNewFeature(isFeature, f, value);
                    newGroup = updateNewGroup(isGroup, g, value);
                } else {
                    otherProps.put(key, value);
                }
            }
            doIfFeature(isFeature, f, newFeature);
            doIfGroup(isGroup, g, newGroup);
            doIfStrategy(isStrategy, s);
            final ConfigurationEvent event = new ConfigurationEvent(reference, 1, "", name);
            listeners.forEach(l -> l.configurationEvent(event));
        }

        private FeatureGroup updateNewGroup(final boolean isGroup, final FeatureGroup g, final Object value) {
            if (isGroup && g != null) {
                return TestHelper.createFeatureGroup(g.getName(), g.getDescription().get(), (boolean) value,
                        g.getStrategy().orElse(null));
            }
            return null;
        }

        private Feature updateNewFeature(final boolean isFeature, final Feature f, final Object value) {
            if (isFeature && f != null) {
                return TestHelper.createFeature(f.getName(), f.getDescription().get(), (boolean) value,
                        f.getGroups().findAny().orElse(null), f.getStrategy().orElse(null));
            }
            return null;
        }

        private void doIfGroup(final boolean isGroup, final FeatureGroup g, final FeatureGroup newGroup) {
            if (isGroup) {
                final Map<String, Object> props = TestHelper.createServiceProperties(2, 5, "pid1");
                manager.unbindFeatureGroup(g, props);
                manager.bindFeatureGroup(newGroup, props);
            }
        }

        private void doIfFeature(final boolean isFeature, final Feature f, final Feature newFeature) {
            if (isFeature) {
                final Map<String, Object> props = TestHelper.createServiceProperties(2, 5, "pid1");
                manager.unbindFeature(f, props);
                manager.bindFeature(newFeature, props);
            }
        }

        private void doIfStrategy(final boolean isStrategy, final ActivationStrategy s) {
            ActivationStrategy newStrategy;
            if (isStrategy && s != null) {
                if (s instanceof SystemPropertyActivationStrategy) {
                    newStrategy = TestHelper.createSystemPropertyActivationStrategy(s.getName(),
                            s.getDescription().get(), "dummyKey", "dummyValue");
                } else {
                    newStrategy = TestHelper.createServicePropertyActivationStrategy(s.getName(),
                            s.getDescription().get(), "dummyKey", "dummyValue");
                }
                final Map<String, Object> props = TestHelper.createServiceProperties(2, 5, "pid1");
                manager.unbindStrategy(s, props);
                manager.bindStrategy(newStrategy, props);
            }
        }

        @Override
        public void delete() throws IOException {
            final ConfigurationEvent event = new ConfigurationEvent(reference, 2, "", name);
            listeners.forEach(l -> l.configurationEvent(event));
        }

        @Override
        public String getFactoryPid() {
            return null;
        }

        @Override
        public void update() throws IOException {
            // mock and not required
        }

        @Override
        public void setBundleLocation(final String location) {
            // mock and not required
        }

        @Override
        public String getBundleLocation() {
            return null;
        }

        public long getChangeCount() {
            return 0;
        }
    }

}
