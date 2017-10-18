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

import static com.amitinside.featureflags.ConfigurationEvent.Type.*;
import static com.amitinside.featureflags.Constants.*;
import static com.amitinside.featureflags.internal.Config.*;
import static java.util.Objects.requireNonNull;
import static org.osgi.framework.Constants.*;
import static org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.ConfigurationEvent;
import com.amitinside.featureflags.ConfigurationEvent.Type;
import com.amitinside.featureflags.Factory;
import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.Strategizable;
import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.feature.group.FeatureGroup;
import com.amitinside.featureflags.listener.ConfigurationListener;
import com.amitinside.featureflags.strategy.ActivationStrategy;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * This service implements the {@link FeatureService}. It keeps track of all
 * {@link Feature}, {@link FeatureGroup} and {@link ActivationStrategy} services.
 */
@Component(name = "FeatureManager", immediate = true)
public class FeatureManager implements FeatureService, org.osgi.service.cm.ConfigurationListener {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Multimap<String, Description<Feature>> allFeatures = TreeMultimap.create();
    private final Multimap<String, Description<ActivationStrategy>> allStrategies = TreeMultimap.create();
    private final Multimap<String, Description<FeatureGroup>> allFeatureGroups = TreeMultimap.create();

    private final Map<String, Feature> activeFeatures = Maps.newHashMap();
    private final Map<String, FeatureGroup> activeFeatureGroups = Maps.newHashMap();
    private final Map<String, ActivationStrategy> activeStrategies = Maps.newHashMap();

    private final List<ConfigurationListener> listeners = Lists.newCopyOnWriteArrayList();

    private final Lock featuresLock = new ReentrantLock(true);
    private final Lock strategiesLock = new ReentrantLock(true);
    private final Lock featureGroupsLock = new ReentrantLock(true);

    private ConfigurationAdmin configurationAdmin;
    private BundleContext context;

    @Override
    public Stream<Feature> getFeatures() {
        return activeFeatures.values().stream();
    }

    @Override
    public Stream<ActivationStrategy> getStrategies() {
        return activeStrategies.values().stream();
    }

    @Override
    public Stream<FeatureGroup> getGroups() {
        return activeFeatureGroups.values().stream();
    }

    @Override
    public Optional<Feature> getFeature(final String featureName) {
        requireNonNull(featureName, "Feature name cannot be null");
        return Optional.ofNullable(activeFeatures.get(featureName));
    }

    @Override
    public Optional<ActivationStrategy> getStrategy(final String strategyName) {
        requireNonNull(strategyName, "Strategy name cannot be null");
        return Optional.ofNullable(activeStrategies.get(strategyName));
    }

    @Override
    public Optional<FeatureGroup> getGroup(final String groupName) {
        requireNonNull(groupName, "Group name cannot be null");
        return Optional.ofNullable(activeFeatureGroups.get(groupName));
    }

    @Override
    public boolean isFeatureEnabled(final String featureName) {
        requireNonNull(featureName, "Feature name cannot be null");
        final Feature feature = getFeature(featureName).orElse(null);
        return feature != null ? checkEnablement(feature) : false;
    }

    @Override
    public boolean isGroupEnabled(final String groupName) {
        requireNonNull(groupName, "Group name cannot be null");
        final FeatureGroup group = getGroup(groupName).orElse(null);
        return group != null ? checkGroupEnablement(group) : false;
    }

    @Override
    public boolean enableFeature(final String featureName) {
        requireNonNull(featureName, "Feature name cannot be null");
        return toggleFeature(featureName, true);
    }

    @Override
    public boolean disableFeature(final String featureName) {
        requireNonNull(featureName, "Feature name cannot be null");
        return toggleFeature(featureName, false);
    }

    @Override
    public boolean enableGroup(final String groupName) {
        requireNonNull(groupName, "Feature Group name cannot be null");
        return toggleFeatureGroup(groupName, true);
    }

    @Override
    public boolean disableGroup(final String groupName) {
        requireNonNull(groupName, "Feature Group name cannot be null");
        return toggleFeatureGroup(groupName, false);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void configurationEvent(final org.osgi.service.cm.ConfigurationEvent event) {
        final ServiceReference reference = event.getReference();
        try {
            final Object service = context.getService(reference);
            if (service instanceof Feature || service instanceof FeatureGroup) {
                final Strategizable instance = (Strategizable) service;
                listeners.forEach(l -> l.accept(getEvent(instance, event.getType())));
            }
        } finally {
            context.ungetService(reference);
        }
    }

    @Override
    public Optional<String> createFeature(final Factory featureFactory) {
        requireNonNull(featureFactory, "Feature factory cannot be null");
        // extract data
        final List<String> groups = featureFactory.getGroups();
        final String name = featureFactory.getName();
        final String description = featureFactory.getDescription().orElse(null);
        final String strategy = featureFactory.getStrategy().orElse(null);
        final boolean isEnabled = featureFactory.isEnabled();
        final Map<String, Object> serviceProperties = featureFactory.getProperties();

        final Map<String, Object> props = Maps.newHashMap();
        props.put(NAME.value(), name);
        props.put(DESCRIPTION.value(), description);
        props.put(STRATEGY.value(), strategy);
        props.put(GROUPS.value(), groups.toArray(new String[0]));
        props.put(ENABLED.value(), isEnabled);
        props.putAll(serviceProperties);
        // remove all null values
        final Map<String, Object> filteredProps = Maps.filterValues(props, Objects::nonNull);
        try {
            final Configuration configuration = configurationAdmin.createFactoryConfiguration(FEATURE_FACTORY_PID);
            configuration.update(new Hashtable<>(filteredProps));
            return Optional.ofNullable(configuration.getPid());
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> createGroup(final Factory groupFactory) {
        requireNonNull(groupFactory, "Group factory cannot be null");
        // extract data
        final String name = groupFactory.getName();
        final String description = groupFactory.getDescription().orElse(null);
        final String strategy = groupFactory.getStrategy().orElse(null);
        final boolean isEnabled = groupFactory.isEnabled();
        final Map<String, Object> serviceProperties = groupFactory.getProperties();

        final Map<String, Object> props = Maps.newHashMap();
        props.put(NAME.value(), name);
        props.put(DESCRIPTION.value(), description);
        props.put(STRATEGY.value(), strategy);
        props.put(ENABLED.value(), isEnabled);
        props.putAll(serviceProperties);
        // remove all null values
        final Map<String, Object> filteredProps = Maps.filterValues(props, Objects::nonNull);
        try {
            final Configuration configuration = configurationAdmin
                    .createFactoryConfiguration(FEATURE_GROUP_FACTORY_PID);
            configuration.update(new Hashtable<>(filteredProps));
            return Optional.ofNullable(configuration.getPid());
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void removeFeature(final String name) {
        requireNonNull(name, "Feature name cannot be null");
        final String pid = getFeaturePID(name);
        deleteConfiguration(name, pid);
    }

    @Override
    public void removeGroup(final String name) {
        requireNonNull(name, "Feature Group name cannot be null");
        final String pid = getGroupPID(name);
        deleteConfiguration(name, pid);
    }

    /**
     * OSGi Service Component Activation Callback
     */
    @Activate
    protected void activate(final BundleContext context) {
        this.context = context;
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
     * {@link Feature} service binding callback
     */
    @Reference(cardinality = MULTIPLE, policy = DYNAMIC)
    protected void bindFeature(final Feature feature, final Map<String, Object> props) {
        featuresLock.lock();
        try {
            final String name = feature.getName();
            // ignore if null or empty
            if (Strings.isNullOrEmpty(name)) {
                return;
            }
            bindInstance(feature, name, props, allFeatures, activeFeatures);
        } finally {
            featuresLock.unlock();
        }
    }

    /**
     * {@link Feature} service unbinding callback
     */
    protected void unbindFeature(final Feature feature, final Map<String, Object> props) {
        featuresLock.lock();
        try {
            final String name = feature.getName();
            unbindInstance(feature, name, props, allFeatures, activeFeatures);
        } finally {
            featuresLock.unlock();
        }
    }

    /**
     * {@link ActivationStrategy} service binding callback
     */
    @Reference(cardinality = MULTIPLE, policy = DYNAMIC)
    protected void bindStrategy(final ActivationStrategy strategy, final Map<String, Object> props) {
        strategiesLock.lock();
        try {
            final String name = strategy.getName();
            // ignore if null or empty
            if (Strings.isNullOrEmpty(name)) {
                return;
            }
            bindInstance(strategy, name, props, allStrategies, activeStrategies);
        } finally {
            strategiesLock.unlock();
        }
    }

    /**
     * {@link ActivationStrategy} service unbinding callback
     */
    protected void unbindStrategy(final ActivationStrategy strategy, final Map<String, Object> props) {
        strategiesLock.lock();
        try {
            final String name = strategy.getName();
            unbindInstance(strategy, name, props, allStrategies, activeStrategies);
        } finally {
            strategiesLock.unlock();
        }
    }

    /**
     * {@link FeatureGroup} service binding callback
     */
    @Reference(cardinality = MULTIPLE, policy = DYNAMIC)
    protected void bindFeatureGroup(final FeatureGroup group, final Map<String, Object> props) {
        featureGroupsLock.lock();
        try {
            final String name = group.getName();
            // ignore if null or empty
            if (Strings.isNullOrEmpty(name)) {
                return;
            }
            bindInstance(group, name, props, allFeatureGroups, activeFeatureGroups);
        } finally {
            featureGroupsLock.unlock();
        }
    }

    /**
     * {@link FeatureGroup} service unbinding callback
     */
    protected void unbindFeatureGroup(final FeatureGroup group, final Map<String, Object> props) {
        featureGroupsLock.lock();
        try {
            final String name = group.getName();
            unbindInstance(group, name, props, allFeatureGroups, activeFeatureGroups);
        } finally {
            featureGroupsLock.unlock();
        }
    }

    /**
     * {@link ConfigurationListener} service binding callback
     */
    @Reference(cardinality = MULTIPLE, policy = DYNAMIC)
    protected void bindConfigurationListener(final ConfigurationListener listener) {
        listeners.add(listener);
    }

    /**
     * {@link ConfigurationListener} service unbinding callback
     */
    protected void unbindConfigurationListener(final ConfigurationListener listener) {
        listeners.remove(listener);
    }

    private <T> void bindInstance(final T instance, final String name, final Map<String, Object> props,
            final Multimap<String, Description<T>> allInstances, final Map<String, T> activeInstances) {
        final Description<T> info = new Description<>(instance, props);
        allInstances.put(name.toLowerCase(), info);
        calculateActiveInstances(allInstances, activeInstances);
    }

    private <T> void unbindInstance(final T instance, final String name, final Map<String, Object> props,
            final Multimap<String, Description<T>> allInstances, final Map<String, T> activeInstances) {
        final Description<T> info = new Description<>(instance, props);
        allInstances.remove(name, info);
        calculateActiveInstances(allInstances, activeInstances);
    }

    private ConfigurationEvent getEvent(final Strategizable instance, final int type) {
        Map<String, Object> properties = ImmutableMap.of();
        if (instance instanceof Feature) {
            properties = getFeatureProperties((Feature) instance);
        } else if (instance instanceof FeatureGroup) {
            properties = getFeatureGroupProperties((FeatureGroup) instance);
        }
        final Type eventType = type == 1 ? UPDATED : DELETED;
        return new ConfigurationEvent(eventType, instance, properties);
    }

    private boolean toggleFeature(final String featureName, final boolean status) {
        final String pid = getFeaturePID(featureName);
        return pid.isEmpty() ? false : checkAndUpdateConfiguration(featureName, pid, status);
    }

    private boolean toggleFeatureGroup(final String groupName, final boolean status) {
        final String pid = getGroupPID(groupName);
        return pid.isEmpty() ? false : checkAndUpdateConfiguration(groupName, pid, status);
    }

    private String getFeaturePID(final String featureName) {
        //@formatter:off
        return allFeatures.values().stream()
                .sorted()
                .filter(x -> x.instance.getName().equalsIgnoreCase(featureName))
                .findFirst()
                .map(f -> f.props)
                .map(m -> m.get(SERVICE_PID))
                .map(String.class::cast)
                .orElse("");
        //@formatter:on
    }

    private String getGroupPID(final String groupName) {
        //@formatter:off
        return allFeatureGroups.values().stream()
                .sorted()
                .filter(x -> x.instance.getName().equalsIgnoreCase(groupName))
                .findFirst()
                .map(f -> f.props)
                .map(m -> m.get(SERVICE_PID))
                .map(String.class::cast)
                .orElse("");
        //@formatter:on
    }

    protected boolean checkAndUpdateConfiguration(final String name, final String pid, final boolean status) {
        try {
            final Configuration configuration = configurationAdmin.getConfiguration(pid, "?");
            final Map<String, Object> newProps = Maps.newHashMap();
            newProps.put(ENABLED.value(), status);
            configuration.update(new Hashtable<>(newProps));
            return true;
        } catch (final IOException e) {
            logger.trace("Cannot retrieve configuration for {}", name, e);
        }
        return false;
    }

    private boolean checkEnablement(final Feature feature) {
        final long noOfGroups = feature.getGroups().count();
        if (noOfGroups > 0) {
            FeatureGroup group;
            if (noOfGroups == 1) {
                group = getGroup(feature.getGroups().findAny().orElse("")).orElse(null);
            } else {
                return feature.getGroups().anyMatch(this::isGroupEnabled);
            }
            return checkGroupEnablement(group);
        }
        return checkFeatureStrategyEnablement(feature);
    }

    private boolean checkGroupEnablement(final FeatureGroup group) {
        final String strategyId = group.getStrategy().orElse("");
        if (!strategyId.isEmpty()) {
            final ActivationStrategy strategy = getStrategy(strategyId).orElse(null);
            if (strategy != null) {
                return strategy.isEnabled(group, getFeatureGroupProperties(group));
            }
        } else {
            return group.isEnabled();
        }
        return false;
    }

    private boolean checkFeatureStrategyEnablement(final Feature feature) {
        final String strategyId = feature.getStrategy().orElse("");
        if (!strategyId.isEmpty()) {
            final ActivationStrategy strategy = getStrategy(strategyId).orElse(null);
            if (strategy != null) {
                return strategy.isEnabled(feature, getFeatureProperties(feature));
            }
        }
        return feature.isEnabled();
    }

    private Map<String, Object> getFeatureProperties(final Feature feature) {
        //@formatter:off
        return allFeatures.values().stream()
                .sorted()
                .filter(x -> Objects.equals(x.instance, feature))
                .findFirst()
                .map(f -> f.props)
                .orElse(ImmutableMap.of());
        //@formatter:on
    }

    private Map<String, Object> getFeatureGroupProperties(final FeatureGroup group) {
        //@formatter:off
        return allFeatureGroups.values().stream()
                .sorted()
                .filter(x -> Objects.equals(x.instance, group))
                .findFirst()
                .map(g -> g.props)
                .orElse(ImmutableMap.of());
        //@formatter:on
    }

    private void deleteConfiguration(final String name, final String pid) {
        try {
            final Configuration configuration = configurationAdmin.getConfiguration(pid);
            configuration.delete();
        } catch (final IOException e) {
            logger.trace("Cannot retrieve configuration for {}", name, e);
        }
    }

    /**
     * Calculates map of active elements (Strategy or Feature) (eliminating name
     * collisions)
     */
    private <T> void calculateActiveInstances(final Multimap<String, Description<T>> allElements,
            final Map<String, T> activeInstance) {
        final Map<String, T> activeMap = Maps.newHashMap();
        for (final Entry<String, Description<T>> entry : allElements.entries()) {
            final String key = entry.getKey();
            final SortedSet<Description<T>> value = (SortedSet<Description<T>>) allElements.get(key);
            final T instance = value.first().instance;
            activeMap.put(key, instance);
            if (value.size() > 1) {
                logger.warn("More than one " + instance.getClass().getSimpleName()
                        + " services with same name - [{}] are available.", key);
            }
        }
        activeInstance.clear();
        activeInstance.putAll(activeMap);
    }

    /**
     * Internal class caching some feature or strategy meta data like service ID and
     * ranking.
     */
    private static final class Description<T> implements Comparable<Description<T>> {
        private final int ranking;
        private final long serviceId;
        private final T instance;
        private final Map<String, Object> props;

        public Description(final T instance, final Map<String, Object> props) {
            this.instance = instance;
            this.props = ImmutableMap.copyOf(props);
            final Object sr = props.get(SERVICE_RANKING);
            ranking = Optional.ofNullable(sr).filter(e -> e instanceof Integer).map(Integer.class::cast).orElse(0);
            serviceId = (long) props.get(SERVICE_ID);
        }

        /**
         * First sort by highest service rankings. If service rankings are equal,
         * then sort by service ID in descending order.
         */
        @Override
        public int compareTo(final Description<T> o) {
            return ComparisonChain.start().compare(o.ranking, ranking).compare(serviceId, o.serviceId).result();
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof Description)) {
                return false;
            }
            final long otherServiceId = ((Description<?>) obj).serviceId;
            return serviceId == otherServiceId;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(serviceId);
        }

        @Override
        public String toString() {
            //@formatter:off
            return com.google.common.base.Objects.toStringHelper(this)
                                        .add("Ranking", ranking)
                                        .add("ServiceID", serviceId)
                                        .add("Instance", instance)
                                        .add("Properties", props)
                                        .toString();
            //@formatter:on
        }
    }

}