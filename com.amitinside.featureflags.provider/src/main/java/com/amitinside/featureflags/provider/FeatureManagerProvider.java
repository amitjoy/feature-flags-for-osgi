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

import static com.amitinside.featureflags.provider.ManagerHelper.getConfiguredFeatures;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.osgi.service.cm.ConfigurationEvent.CM_UPDATED;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.amitinside.featureflags.dto.FeatureDTO;
import com.amitinside.featureflags.provider.ManagerHelper.Feature;
import com.google.common.collect.ArrayListMultimap;
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
    public Stream<FeatureDTO> getFeatures() {
        //@formatter:off
        return allFeatures.values()
                          .stream()
                          .map(ManagerHelper::toFeatureDTO);
        //@formatter:on
    }

    @Override
    public Stream<FeatureDTO> getFeatures(final String featureID) {
        requireNonNull(featureID, "Feature ID cannot be null");
        checkArgument(!featureID.isEmpty(), "Feature ID cannot be empty");

        //@formatter:off
        return allFeatures.values()
                          .stream()
                          .filter(f -> f.id.equals(featureID))
                          .map(ManagerHelper::toFeatureDTO);
        //@formatter:on
    }

    @Override
    public void updateFeature(final String featureID, final boolean isEnabled) {
        requireNonNull(featureID, "Feature ID cannot be null");
        checkArgument(!featureID.isEmpty(), "Feature ID cannot be empty");

        final Map<String, Object> props = Maps.newHashMap();
        props.put(METATYPE_FEATURE_ID_PREFIX + featureID, isEnabled);
        final Map<String, Object> filteredProps = Maps.filterValues(props, Objects::nonNull);
        try {
            //@formatter:off
            final List<String> configurations = allFeatures.asMap()
                                                           .entrySet()
                                                           .stream()
                                                           .filter(e -> e.getValue()
                                                                         .stream()
                                                                         .filter(f -> f.id.equals(featureID))
                                                                         .findAny()
                                                                         .isPresent())
                                                           .map(Entry::getKey)
                                                           .collect(Collectors.toList());
            //@formatter:on
            for (final String configurationPID : configurations) {
                final Configuration configuration = configurationAdmin.getConfiguration(configurationPID, "?");
                if (configuration != null) {
                    configuration.update(new Hashtable<>(filteredProps));
                }
            }
        } catch (final Exception e) {
            // not required
        }
    }

    @Override
    public void configurationEvent(final ConfigurationEvent event) {
        final int type = event.getType();
        final String pid = event.getPid();
        if (type == CM_UPDATED) {
            final Map<String, Boolean> configuredFeatures = getConfiguredFeatures(pid, configurationAdmin);
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

}