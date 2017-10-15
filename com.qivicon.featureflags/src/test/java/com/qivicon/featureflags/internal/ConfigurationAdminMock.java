/*******************************************************************************
 * Copyright (c) 2017 QIVICON
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Amit Kumar Mondal
 *
 *******************************************************************************/
package com.qivicon.featureflags.internal;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qivicon.featureflags.Strategizable;
import com.qivicon.featureflags.feature.Feature;
import com.qivicon.featureflags.feature.group.FeatureGroup;

@SuppressWarnings({ "unchecked", "rawtypes" })
public final class ConfigurationAdminMock implements ConfigurationAdmin {

    private final FeatureManager manager;
    private final ServiceReference reference;
    private final List<ConfigurationListener> listeners = Lists.newCopyOnWriteArrayList();
    private final Strategizable instance;

    public ConfigurationAdminMock(final FeatureManager manager, final ServiceReference reference,
            final Strategizable instance) {
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
        private final Strategizable instance;

        public ConfigurationMock(final String name, final FeatureManager manager, final ServiceReference reference,
                final Strategizable instance) {
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
            Feature f = null;
            FeatureGroup g = null;
            if (instance instanceof Feature) {
                isFeature = true;
                f = (Feature) instance;
            }
            if (instance instanceof FeatureGroup) {
                isGroup = true;
                g = (FeatureGroup) instance;
            }
            Feature newFeature = null;
            FeatureGroup newGroup = null;
            final Map<String, Object> otherProps = Maps.newHashMap();
            while (enums.hasMoreElements()) {
                final String key = enums.nextElement();
                final Object value = properties.get(key);
                if (key.equalsIgnoreCase("enabled")) {
                    if (isFeature && f != null) {
                        newFeature = TestHelper.createFeature(f.getName(), f.getDescription().get(), (boolean) value,
                                f.getGroup().orElse(null), f.getStrategy().orElse(null));
                    }
                    if (isGroup && g != null) {
                        newGroup = TestHelper.createFeatureGroup(g.getName(), g.getDescription().get(), (boolean) value,
                                g.getStrategy().orElse(null));
                    }
                } else {
                    otherProps.put(key, value);
                }
            }
            if (isFeature) {
                final Map<String, Object> props = TestHelper.createServiceProperties(2, 5, "pid1");
                manager.unbindFeature(f, props);
                manager.bindFeature(newFeature, props);
            }
            if (isGroup) {
                final Map<String, Object> props = TestHelper.createServiceProperties(2, 5, "pid1");
                manager.unbindFeatureGroup(g, props);
                manager.bindFeatureGroup(newGroup, props);
            }
            final ConfigurationEvent event = new ConfigurationEvent(reference, 1, "", name);
            listeners.forEach(l -> l.configurationEvent(event));
        }

        @Override
        public void delete() throws IOException {
        }

        @Override
        public String getFactoryPid() {
            return null;
        }

        @Override
        public void update() throws IOException {
        }

        @Override
        public void setBundleLocation(final String location) {
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
