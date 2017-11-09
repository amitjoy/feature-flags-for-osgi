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

import static com.amitinside.featureflags.Constants.FEATURE_AD_NAME_PREFIX;
import static java.util.Objects.requireNonNull;
import static org.osgi.service.cm.ConfigurationEvent.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.FeatureManager;
import com.amitinside.featureflags.dto.ConfigurationDTO;
import com.amitinside.featureflags.dto.FeatureDTO;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * This implements the {@link FeatureManager}.
 */
@Component(name = "FeatureManager", immediate = true, service = FeatureManager.class)
public final class FeatureManagerProvider implements FeatureManager, org.osgi.service.cm.ConfigurationListener {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(FeatureManagerProvider.class);

    /** Data container -> Key: Configuration PID Value: Feature Name(s) */
    private final Multimap<String, String> allFeatures = TreeMultimap.create();

    /** Configuration Admin Service Instance Reference */
    private ConfigurationAdmin configurationAdmin;

    @Override
    public void configurationEvent(final ConfigurationEvent event) {
        final int type = event.getType();
        final ServiceReference reference = event.getReference();
        final boolean isFeatureContainter = checkIfFeatureConfigExists(reference);
        if (isFeatureContainter && type == CM_UPDATED) {
            final String pid = event.getPid();
            //@formatter:off
            final String[] keys = reference.getPropertyKeys();
            Arrays.stream(keys)
                  .filter(k -> k.startsWith(FEATURE_AD_NAME_PREFIX))
                  .map(p -> p.substring(FEATURE_AD_NAME_PREFIX.length(), p.length()))
                  .forEach(p -> allFeatures.put(pid, p));
            //@formatter:on
        }
        if (type == CM_DELETED) {
            allFeatures.removeAll(event.getPid());
        }
    }

    @Override
    public Stream<ConfigurationDTO> getConfigurations() {
        //@formatter:off
        return allFeatures.keys()
                          .stream()
                          .map(this::convertToConfigurationDTO)
                          .filter(Objects::nonNull);
        //@formatter:on
    }

    @Override
    public Stream<FeatureDTO> getFeatures(final String configurationPID) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");

        //@formatter:off
        return allFeatures.get(configurationPID)
                          .stream()
                          .map(f -> convertToFeatureDTO(configurationPID, f))
                          .filter(Objects::nonNull);
        //@formatter:on
    }

    @Override
    public Optional<FeatureDTO> getFeature(final String configurationPID, final String featureName) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        requireNonNull(featureName, "Feature Name cannot be null");

        //@formatter:off
        return allFeatures.get(configurationPID)
                          .stream()
                          .map(f -> convertToFeatureDTO(configurationPID, f))
                          .filter(Objects::nonNull)
                          .filter(f -> f.name.equalsIgnoreCase(featureName))
                          .findAny();
        //@formatter:on
    }

    @Override
    public Optional<ConfigurationDTO> getConfiguration(final String configurationPID) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        return Optional.ofNullable(convertToConfigurationDTO(configurationPID));
    }

    @Override
    public boolean updateFeature(final String configurationPID, final String featureName, final boolean valueToSet) {
        requireNonNull(configurationPID, "Configuration PID cannot be null");
        requireNonNull(featureName, "Feature Name cannot be null");

        final Map<String, Object> props = Maps.newHashMap();
        props.put(FEATURE_AD_NAME_PREFIX + featureName, valueToSet);
        final Map<String, Object> filteredProps = Maps.filterValues(props, Objects::nonNull);
        try {
            final Configuration configuration = configurationAdmin.getConfiguration(configurationPID);
            if (configuration != null) {
                configuration.update(new Hashtable<>(filteredProps));
                return true;
            }
        } catch (final IOException e) {
            logger.trace("Cannot retrieve configuration for {}", configurationPID, e);
        }
        return false;
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

    private boolean checkIfFeatureConfigExists(final ServiceReference reference) {
        //@formatter:off
        final String[] keys = reference.getPropertyKeys();
        return Arrays.stream(keys)
                     .filter(k -> k.startsWith(FEATURE_AD_NAME_PREFIX))
                     .findAny()
                     .isPresent();
        //@formatter:on
    }

    @SuppressWarnings("unchecked")
    private FeatureDTO convertToFeatureDTO(final String configurationPID, final String featureName) {
        try {
            final Configuration configuration = configurationAdmin.getConfiguration(configurationPID);
            if (configuration != null) {
                final Dictionary<String, Object> properties = configuration.getProperties();
                final Object value = properties.get(FEATURE_AD_NAME_PREFIX + featureName);
                boolean enabled = false;
                if (value instanceof Boolean) {
                    enabled = (boolean) value;
                }
                final FeatureDTO dto = new FeatureDTO();
                dto.name = featureName;
                dto.isEnabled = enabled;
                return dto;
            }
        } catch (final IOException e) {
            logger.trace("Cannot retrieve configuration for {}", configurationPID, e);
        }
        return null;
    }

    private ConfigurationDTO convertToConfigurationDTO(final String configurationPID) {
        final Collection<String> features = allFeatures.get(configurationPID);
        if (features.isEmpty()) {
            return null;
        }
        final ConfigurationDTO dto = new ConfigurationDTO();
        dto.features = ImmutableSet.copyOf(features);
        return dto;
    }

}