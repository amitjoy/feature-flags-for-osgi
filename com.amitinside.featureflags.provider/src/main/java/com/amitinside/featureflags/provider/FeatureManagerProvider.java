/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.provider;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;
import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.service.cm.ConfigurationEvent.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.FeatureManager;
import com.amitinside.featureflags.dto.ConfigurationDTO;
import com.amitinside.featureflags.dto.FeatureDTO;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * This implements the {@link FeatureManager}.
 */
@Component(name = "FeatureManager", immediate = true)
public final class FeatureManagerProvider implements FeatureManager, ConfigurationListener {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(FeatureManagerProvider.class);

    /** Data container -> Key: Configuration PID Value: Feature DTOs */
    private final Multimap<String, FeatureDTO> allFeatures = ArrayListMultimap.create();

    /** Data container -> Key: Bundle Instance Value: Configuration PID(s) */
    private final Multimap<Bundle, String> bundlePids = ArrayListMultimap.create();

    /** Configuration Admin Service Instance Reference */
    private ConfigurationAdmin configurationAdmin;

    /** Metatype Service Instance Reference */
    private MetaTypeService metaTypeService;

    /** Bundle Tracker Instance Reference */
    private BundleTracker bundleTracker;

    @Activate
    protected void activate(final BundleContext bundleContext) {
        // track all bundles to check existing features
        final BundleTrackerCustomizer customizer = new FeatureMetaTypeTracker(metaTypeService, bundlePids, allFeatures);
        bundleTracker = new BundleTracker(bundleContext, ACTIVE, customizer);
        bundleTracker.open();
    }

    @Deactivate
    protected void deactivate() {
        if (bundleTracker != null) {
            bundleTracker.close();
            bundleTracker = null;
        }
    }

    /**
     * {@link ConfigurationAdmin} service binding callback
     */
    @Reference
    protected void setConfigurationAdmin(final ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

    /**
     * {@link ConfigurationAdmin} service unbinding callback
     */
    protected void unsetConfigurationAdmin(final ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = null;
    }

    /**
     * {@link MetaTypeService} service binding callback
     */
    @Reference
    protected void setMetaTypeService(final MetaTypeService metaTypeService) {
        this.metaTypeService = metaTypeService;
    }

    /**
     * {@link MetaTypeService} service unbinding callback
     */
    protected void unsetMetaTypeService(final MetaTypeService metaTypeService) {
        this.metaTypeService = null;
    }

    public List<ConfigurationDTO> getConfigs() {
        //@formatter:off
        return allFeatures.keys()
                          .stream()
                          .map(this::convertToConfiguration)
                          .filter(Objects::nonNull)
                          .collect(toList());
        //@formatter:on
    }

    @Override
    public Stream<ConfigurationDTO> getConfigurations() {
        //@formatter:off
        return allFeatures.keys()
                          .stream()
                          .map(this::convertToConfiguration)
                          .filter(Objects::nonNull);
        //@formatter:on
    }

    @Override
    public Stream<FeatureDTO> getFeatures(final String configurationPID) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        checkArgument(!configurationPID.isEmpty(), "Configuration PID cannot be empty");

        return allFeatures.get(configurationPID).stream();
    }

    @Override
    public Optional<ConfigurationDTO> getConfiguration(final String configurationPID) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        checkArgument(!configurationPID.isEmpty(), "Configuration PID cannot be empty");

        return Optional.ofNullable(convertToConfiguration(configurationPID));
    }

    @Override
    public Optional<FeatureDTO> getFeature(final String configurationPID, final String featureName) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        requireNonNull(featureName, "Feature Name cannot be null");
        checkArgument(!configurationPID.isEmpty(), "Configuration PID cannot be empty");
        checkArgument(!featureName.isEmpty(), "Feature Name cannot be empty");

        //@formatter:off
        return allFeatures.get(configurationPID)
                          .stream()
                          .filter(f -> f.name.equalsIgnoreCase(featureName))
                          .findAny();
        //@formatter:on
    }

    @Override
    public CompletableFuture<Void> updateFeature(final String configurationPID, final String featureName,
            final boolean isEnabled) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        requireNonNull(featureName, "Feature Name cannot be null");
        checkArgument(!configurationPID.isEmpty(), "Configuration PID cannot be empty");
        checkArgument(!featureName.isEmpty(), "Feature Name cannot be empty");

        final Map<String, Object> props = Maps.newHashMap();
        props.put(FEATURE_NAME_PREFIX + featureName, isEnabled);
        final Map<String, Object> filteredProps = Maps.filterValues(props, Objects::nonNull);
        return CompletableFuture.runAsync(() -> {
            try {
                final Configuration configuration = configurationAdmin.getConfiguration(configurationPID, "?");
                if (configuration != null) {
                    configuration.update(new Hashtable<>(filteredProps));
                }
            } catch (final IOException e) {
                throw new RuntimeException(
                        String.format("Cannot update feature [%s] with PID [%s]", featureName, configurationPID));
            }
        });
    }

    @Override
    public void configurationEvent(final ConfigurationEvent event) {
        final int type = event.getType();
        final String pid = event.getPid();
        final Map<String, Boolean> configuredFeatures = getConfiguredFeatures(pid);
        if (!configuredFeatures.isEmpty() && type == CM_UPDATED) {
            for (final Entry<String, Boolean> entry : configuredFeatures.entrySet()) {
                final String featureName = entry.getKey();
                final boolean isEnabled = entry.getValue();
                final Collection<FeatureDTO> features = allFeatures.get(pid);
                //@formatter:off
                features.stream()
                        .filter(f -> f.name.equalsIgnoreCase(featureName))
                        .forEach(f -> f.isEnabled = isEnabled);
                //@formatter:on
            }
        }
        if (type == CM_DELETED) {
            allFeatures.removeAll(pid);
        }
    }

    private Map<String, Boolean> getConfiguredFeatures(final String configurationPID) {
        try {
            final Configuration configuration = configurationAdmin.getConfiguration(configurationPID, "?");
            if (configuration != null) {
                @SuppressWarnings("unchecked")
                final Dictionary<String, Object> properties = configuration.getProperties();
                final Iterator<String> keysIterator = Iterators.forEnumeration(properties.keys());
                final Map<String, Object> props = Maps.toMap(keysIterator, properties::get);
                final Map<String, Object> filteredProps = Maps.filterValues(props, Objects::nonNull);
                //@formatter:off
                final Function<? super Entry<String, Object>, ? extends String> nameMapper =
                                                    k -> k.getKey().substring(FEATURE_NAME_PREFIX.length(),
                                                                                       k.getKey().length());
                final Function<? super Entry<String, Object>, ? extends Boolean> valueMapper =
                                                    v -> (Boolean) v.getValue();
                return filteredProps.entrySet().stream()
                                               .filter(e -> e.getKey().startsWith(FEATURE_NAME_PREFIX))
                                               .collect(toMap(nameMapper, valueMapper));
                //@formatter:on
            }
        } catch (final IOException e) {
            logger.error("Cannot retrieve configuration for {}", configurationPID, e);
        }
        return ImmutableMap.of();
    }

    private ConfigurationDTO convertToConfiguration(final String configurationPID) {
        final Collection<FeatureDTO> features = allFeatures.get(configurationPID);
        if (features.isEmpty()) {
            return null;
        }
        return createConfiguration(configurationPID, Lists.newArrayList(features));
    }

    private static ConfigurationDTO createConfiguration(final String pid, final List<FeatureDTO> features) {
        final ConfigurationDTO config = new ConfigurationDTO();
        config.pid = pid;
        config.features = features;
        return config;
    }

}