/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
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
package com.amitinside.featureflags.internal;

import static java.util.Objects.requireNonNull;
import static org.osgi.framework.Constants.*;
import static org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.strategy.ActivationStrategy;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * This service implements the {@link FeatureService}. It keeps track of all
 * {@link Feature} services and {@link ActivationStrategy} services.
 */
@Component(name = "FeatureManager")
public final class FeatureManager implements FeatureService {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Multimap<String, Description<Feature>> allFeatures = TreeMultimap.create();
    private final Multimap<String, Description<ActivationStrategy>> allStrategies = TreeMultimap.create();

    private final Map<String, Feature> activeFeatures = Maps.newHashMap();
    private final Map<String, ActivationStrategy> activeStrategies = Maps.newHashMap();

    private final Lock featuresLock = new ReentrantLock(true);
    private final Lock strategiesLock = new ReentrantLock(true);

    @Override
    public Stream<Feature> getFeatures() {
        return activeFeatures.values().stream();
    }

    @Override
    public Stream<ActivationStrategy> getStrategies() {
        return activeStrategies.values().stream();
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
    public boolean isEnabled(final String featureName) {
        requireNonNull(featureName, "Feature name cannot be null");
        final Feature feature = getFeature(featureName).orElse(null);
        if (feature != null) {
            final String strategyId = feature.getStrategy().orElse("");
            if (strategyId.isEmpty()) {
                return feature.isEnabled();
            } else {
                final ActivationStrategy strategy = getStrategy(strategyId).orElse(null);
                if (strategy != null) {
                    //@formatter:off
                    final Map<String, Object> properties = allFeatures.values().stream()
                            .sorted()
                            .filter(x -> x.instance == feature)
                            .findFirst()
                            .map(f -> f.props)
                            .orElse(ImmutableMap.of());
                    //@formatter:on
                    return strategy.isEnabled(feature, properties);
                } else {
                    return feature.isEnabled();
                }
            }
        }
        return false;
    }

    /**
     * {@link Feature} service binding callback
     */
    @Reference(cardinality = MULTIPLE, policy = DYNAMIC)
    private void bindFeature(final Feature feature, final Map<String, Object> props) {
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
    @SuppressWarnings("unused")
    private void unbindFeature(final Feature feature, final Map<String, Object> props) {
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
    private void bindStrategy(final ActivationStrategy strategy, final Map<String, Object> props) {
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
    @SuppressWarnings("unused")
    private void unbindStrategy(final ActivationStrategy strategy, final Map<String, Object> props) {
        strategiesLock.lock();
        try {
            final String name = strategy.getName();
            unbindInstance(strategy, name, props, allStrategies, activeStrategies);
        } finally {
            strategiesLock.unlock();
        }
    }

    private <T> void bindInstance(final T instance, final String name, final Map<String, Object> props,
            final Multimap<String, Description<T>> allInstances, final Map<String, T> activeInstances) {
        final Description<T> info = new Description<>(instance, props);
        allInstances.put(name, info);
        calculateActiveInstances(allInstances, activeInstances);
    }

    private <T> void unbindInstance(final T instance, final String name, final Map<String, Object> props,
            final Multimap<String, Description<T>> allInstances, final Map<String, T> activeInstances) {
        final Description<T> info = new Description<>(instance, props);
        allInstances.remove(name, info);
        calculateActiveInstances(allInstances, activeInstances);
    }

    /**
     * Calculates map of active elements (Strategy or Feature) (eliminating name
     * collisions)
     */
    private <T> void calculateActiveInstances(final Multimap<String, Description<T>> allElements,
            Map<String, T> refInstance) {
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
        refInstance = activeMap;
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
            return Objects.equals(serviceId, obj);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(serviceId);
        }
    }
}