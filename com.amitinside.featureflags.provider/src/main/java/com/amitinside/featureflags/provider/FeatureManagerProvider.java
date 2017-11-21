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

import static com.amitinside.featureflags.provider.ManagerHelper.extractFeatureID;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.osgi.service.cm.ConfigurationEvent.CM_UPDATED;
import static org.osgi.service.log.LogService.LOG_ERROR;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.apache.felix.utils.collections.DictionaryAsMap;
import org.apache.felix.utils.log.Logger;
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

import com.amitinside.featureflags.FeatureManager;
import com.amitinside.featureflags.dto.ConfigurationDTO;
import com.amitinside.featureflags.dto.FeatureDTO;
import com.amitinside.featureflags.provider.ManagerHelper.Feature;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * This implements the {@link FeatureManager}.
 */
@ProvideFeatureCapability
@Component(name = "FeatureManager", immediate = true)
public final class FeatureManagerProvider implements FeatureManager, ConfigurationListener {

    /** Data container -> Key: Configuration PID Value: Feature DTOs */
    private final Multimap<String, Feature> allFeatures = ArrayListMultimap.create();

    /** Data container -> Key: Bundle Instance Value: Configuration PID(s) */
    private final Multimap<Bundle, String> bundlePids = ArrayListMultimap.create();

    /** Configuration Admin Service Instance Reference */
    private ConfigurationAdmin configurationAdmin;

    /** Metatype Service Instance Reference */
    private MetaTypeService metaTypeService;

    /** Metatype Extender Instance Reference */
    private MetaTypeExtender extender;

    /** Logger Instance */
    private Logger logger;

    @Activate
    protected void activate(final BundleContext bundleContext) throws Exception {
        logger = new Logger(bundleContext);
        extender = new MetaTypeExtender(metaTypeService, logger, bundlePids, allFeatures);
        extender.start(bundleContext);
    }

    @Deactivate
    protected void deactivate(final BundleContext bundleContext) throws Exception {
        extender.stop(bundleContext);
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

    protected MetaTypeExtender getExtender() {
        return extender;
    }

    @Override
    public Stream<ConfigurationDTO> getConfigurations() {
        //@formatter:off
        return allFeatures.keys()
                          .stream()
                          .map(this::toConfigurationDTO)
                          .filter(Objects::nonNull);
        //@formatter:on
    }

    @Override
    public Stream<FeatureDTO> getFeatures(final String configurationPID) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        checkArgument(!configurationPID.isEmpty(), "Configuration PID cannot be empty");

        //@formatter:off
        return allFeatures.get(configurationPID)
                          .stream()
                          .map(ManagerHelper::toFeatureDTO);
        //@formatter:on
    }

    @Override
    public Optional<ConfigurationDTO> getConfiguration(final String configurationPID) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        checkArgument(!configurationPID.isEmpty(), "Configuration PID cannot be empty");

        return Optional.ofNullable(toConfigurationDTO(configurationPID));
    }

    @Override
    public Optional<FeatureDTO> getFeature(final String configurationPID, final String featureID) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        requireNonNull(featureID, "Feature ID cannot be null");
        checkArgument(!configurationPID.isEmpty(), "Configuration PID cannot be empty");
        checkArgument(!featureID.isEmpty(), "Feature ID cannot be empty");

        //@formatter:off
        return allFeatures.get(configurationPID)
                          .stream()
                          .filter(f -> f.id.equalsIgnoreCase(featureID))
                          .findAny()
                          .map(ManagerHelper::toFeatureDTO);
        //@formatter:on
    }

    @Override
    public CompletableFuture<Void> updateFeature(final String configurationPID, final String featureID,
            final boolean isEnabled) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        requireNonNull(featureID, "Feature ID cannot be null");
        checkArgument(!configurationPID.isEmpty(), "Configuration PID cannot be empty");
        checkArgument(!featureID.isEmpty(), "Feature ID cannot be empty");

        final Map<String, Object> props = Maps.newHashMap();
        props.put(METATYPE_FEATURE_ID_PREFIX + featureID, isEnabled);
        final Map<String, Object> filteredProps = Maps.filterValues(props, Objects::nonNull);
        return CompletableFuture.runAsync(() -> {
            try {
                final Configuration configuration = configurationAdmin.getConfiguration(configurationPID, "?");
                if (configuration != null) {
                    configuration.update(new Hashtable<>(filteredProps));
                }
            } catch (final Exception e) {
                Throwables.propagate(e);
            }
        });
    }

    @Override
    public void configurationEvent(final ConfigurationEvent event) {
        final int type = event.getType();
        final String pid = event.getPid();
        final Map<String, Boolean> configuredFeatures = getConfiguredFeatures(pid);
        if (type == CM_UPDATED) {
            for (final Entry<String, Boolean> entry : configuredFeatures.entrySet()) {
                final String featureID = entry.getKey();
                final boolean isEnabled = entry.getValue();
                final Collection<Feature> features = allFeatures.get(pid);
                //@formatter:off
                features.stream()
                        .filter(f -> f.id.equalsIgnoreCase(featureID))
                        .forEach(f -> f.isEnabled = isEnabled);
                //@formatter:on
            }
        } else {
            allFeatures.removeAll(pid);
        }
    }

    private Map<String, Boolean> getConfiguredFeatures(final String configurationPID) {
        try {
            final Configuration configuration = configurationAdmin.getConfiguration(configurationPID, "?");
            @SuppressWarnings("unchecked")
            final Map<String, Object> properties = new DictionaryAsMap<>(configuration.getProperties());
            //@formatter:off
            return properties.entrySet().stream()
                                        .filter(e -> e.getKey().startsWith(METATYPE_FEATURE_ID_PREFIX))
                                        .filter(e -> e.getValue() instanceof Boolean)
                                        .collect(toMap(e -> extractFeatureID(e.getKey()),
                                                       e -> (Boolean) e.getValue()));
            //@formatter:on
        } catch (final IOException e) {
            logger.log(LOG_ERROR, String.format("Cannot retrieve configuration for %s", configurationPID), e);
        }
        return ImmutableMap.of();
    }

    private ConfigurationDTO toConfigurationDTO(final String configurationPID) {
        final Collection<Feature> features = allFeatures.get(configurationPID);
        if (features.isEmpty()) {
            return null;
        }
        return ManagerHelper.toConfigurationDTO(configurationPID, Lists.newArrayList(features));
    }

}