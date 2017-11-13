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

import static com.amitinside.featureflags.Feature.FEATURE_NAME_PREFIX;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.felix.service.command.CommandProcessor.*;
import static org.osgi.service.cm.ConfigurationEvent.*;
import static org.osgi.service.metatype.ObjectClassDefinition.ALL;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.Feature;
import com.amitinside.featureflags.FeatureConfiguration;
import com.amitinside.featureflags.FeatureManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * This implements the {@link FeatureManager}.
 */
@SuppressWarnings("unchecked")
@Component(name = "FeatureManager", immediate = true, property = { COMMAND_SCOPE + "=feature",
        COMMAND_FUNCTION + "=updateFeature" })
public final class FeatureManagerProvider implements FeatureManager, ConfigurationListener {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(FeatureManagerProvider.class);

    /** Data container -> Key: Configuration PID Value: Feature Name(s) */
    private final Multimap<String, String> allFeatures = TreeMultimap.create();

    /** Configuration Admin Service Instance Reference */
    private ConfigurationAdmin configurationAdmin;

    /** Metatype Service Instance Reference */
    private MetaTypeService metaTypeService;

    /** Bundle Context Instance Reference */
    private BundleContext bundleContext;

    @Activate
    protected void activate(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        // track already existing configurations
        try {
            final Configuration[] existingConfigurations = configurationAdmin.listConfigurations(null);
            if (existingConfigurations == null) {
                return;
            }
            Arrays.stream(existingConfigurations).map(Configuration::getPid).forEach(pid -> {
                final List<String> configuredFeatures = getConfiguredFeatures(pid);
                if (!configuredFeatures.isEmpty()) {
                    configuredFeatures.forEach(p -> allFeatures.put(pid, p));
                }
            });
        } catch (final IOException | InvalidSyntaxException e) {
            logger.error("Cannot retrieve configurations", e);
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

    @Override
    public Stream<FeatureConfiguration> getConfigurations() {
        //@formatter:off
        return allFeatures.keys()
                          .stream()
                          .map(this::convertToConfiguration)
                          .filter(Objects::nonNull);
        //@formatter:on
    }

    @Override
    public Stream<Feature> getFeatures(final String configurationPID) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        checkArgument(!configurationPID.isEmpty(), "Configuration PID cannot be empty");

        //@formatter:off
        return allFeatures.get(configurationPID)
                          .stream()
                          .map(f -> convertToFeature(configurationPID, f))
                          .filter(Objects::nonNull);
        //@formatter:on
    }

    @Override
    public Optional<FeatureConfiguration> getConfiguration(final String configurationPID) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        checkArgument(!configurationPID.isEmpty(), "Configuration PID cannot be empty");

        return Optional.ofNullable(convertToConfiguration(configurationPID));
    }

    @Override
    public Optional<Feature> getFeature(final String configurationPID, final String featureName) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        requireNonNull(featureName, "Feature Name cannot be null");
        checkArgument(!configurationPID.isEmpty(), "Configuration PID cannot be empty");
        checkArgument(!featureName.isEmpty(), "Feature Name cannot be empty");

        //@formatter:off
        return allFeatures.get(configurationPID)
                          .stream()
                          .map(f -> convertToFeature(configurationPID, f))
                          .filter(Objects::nonNull)
                          .filter(f -> f.getName().equalsIgnoreCase(featureName))
                          .findAny();
        //@formatter:on
    }

    @Override
    public boolean updateFeature(final String configurationPID, final String featureName, final boolean isEnabled) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        requireNonNull(featureName, "Feature Name cannot be null");
        checkArgument(!configurationPID.isEmpty(), "Configuration PID cannot be empty");
        checkArgument(!featureName.isEmpty(), "Feature Name cannot be empty");

        final Map<String, Object> props = Maps.newHashMap();
        props.put(FEATURE_NAME_PREFIX + featureName, isEnabled);
        final Map<String, Object> filteredProps = Maps.filterValues(props, Objects::nonNull);
        try {
            final Configuration configuration = configurationAdmin.getConfiguration(configurationPID, "?");
            if (configuration != null) {
                configuration.update(new Hashtable<>(filteredProps));
                return true;
            }
        } catch (final IOException e) {
            logger.error("Cannot retrieve configuration for {}", configurationPID, e);
        }
        return false;
    }

    @Override
    public void configurationEvent(final ConfigurationEvent event) {
        final int type = event.getType();
        final String pid = event.getPid();
        final List<String> configuredFeatures = getConfiguredFeatures(pid);
        if (!configuredFeatures.isEmpty() && type == CM_UPDATED) {
            configuredFeatures.forEach(p -> allFeatures.put(pid, p));
        }
        if (type == CM_DELETED) {
            allFeatures.removeAll(pid);
        }
    }

    private List<String> getConfiguredFeatures(final String configurationPID) {
        try {
            final Configuration configuration = configurationAdmin.getConfiguration(configurationPID, "?");
            if (configuration != null) {
                final Dictionary<String, Object> properties = configuration.getProperties();
                final Iterator<String> keysIterator = Iterators.forEnumeration(properties.keys());
                final Map<String, Object> props = Maps.toMap(keysIterator, properties::get);
                final Map<String, Object> filteredProps = Maps.filterValues(props, Objects::nonNull);
                //@formatter:off
                return filteredProps.keySet().stream()
                                             .filter(k -> k.startsWith(FEATURE_NAME_PREFIX))
                                             .map(k -> k.substring(FEATURE_NAME_PREFIX.length(), k.length()))
                                             .collect(toList());
                //@formatter:on
            }
        } catch (final IOException e) {
            logger.error("Cannot retrieve configuration for {}", configurationPID, e);
        }
        return ImmutableList.of();
    }

    private Feature convertToFeature(final String configurationPID, final String featureName) {
        try {
            final Configuration configuration = configurationAdmin.getConfiguration(configurationPID, "?");
            if (configuration != null) {
                final Dictionary<String, Object> properties = configuration.getProperties();
                final Object value = properties.get(FEATURE_NAME_PREFIX + featureName);
                boolean enabled = false;
                if (value instanceof Boolean) {
                    enabled = (boolean) value;
                }
                final String description = getFeatureDescription(configurationPID, featureName).orElse(null);
                return new Feature(featureName, description, enabled);
            }
        } catch (final IOException e) {
            logger.error("Cannot retrieve configuration for {}", configurationPID, e);
        }
        return null;
    }

    private FeatureConfiguration convertToConfiguration(final String configurationPID) {
        final Collection<String> features = allFeatures.get(configurationPID);
        if (features.isEmpty()) {
            return null;
        }
        final List<Feature> specifiedFeatures = getFeatures(configurationPID).collect(toList());
        return new FeatureConfiguration(configurationPID, specifiedFeatures);
    }

    private Optional<String> getFeatureDescription(final String configurationPID, final String featureName) {
        final Bundle[] bundles = bundleContext.getBundles();
        //@formatter:off
        return Arrays.stream(bundles)
                     .map(metaTypeService::getMetaTypeInformation)
                     .filter(Objects::nonNull)
                     .filter(m -> Lists.newArrayList(m.getPids()).contains(configurationPID))
                     .map(m -> m.getObjectClassDefinition(configurationPID, null))
                     .map(o -> o.getAttributeDefinitions(ALL))
                     .filter(Objects::nonNull)
                     .flatMap(Arrays::stream)
                     .filter(ad -> ad.getID().equals(FEATURE_NAME_PREFIX + featureName))
                     .map(AttributeDefinition::getDescription)
                     .findAny();
        //@formatter:on
    }

}