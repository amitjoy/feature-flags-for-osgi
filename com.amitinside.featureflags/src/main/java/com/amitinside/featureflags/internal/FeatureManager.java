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
 * {@link Feature} services and {@link ActivationStrategy} services
 */
@Component(name = "FeatureManager")
public final class FeatureManager implements FeatureService {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Multimap<String, FeatureDescription> allFeatures = TreeMultimap.create();
    private final Map<String, ActivationStrategy> allStrategies = Maps.newHashMap();
    private Map<String, Feature> activeFeatures = Maps.newHashMap();

    private final Lock featuresLock = new ReentrantLock(true);
    private final Lock strategiesLock = new ReentrantLock(true);

    @Override
    public Stream<Feature> getFeatures() {
        return activeFeatures.values().stream();
    }

    @Override
    public Stream<ActivationStrategy> getStrategies() {
        return allStrategies.values().stream();
    }

    @Override
    public Optional<Feature> getFeature(final String featureName) {
        requireNonNull(featureName, "Feature name cannot be null");
        return Optional.ofNullable(activeFeatures.get(featureName));
    }

    @Override
    public Optional<ActivationStrategy> getStrategy(final String strategyName) {
        requireNonNull(strategyName, "Strategy name cannot be null");
        return Optional.ofNullable(allStrategies.get(strategyName));
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
                final ActivationStrategy strategy = allStrategies.get(strategyId);
                if (strategy != null) {
                    //@formatter:off
                    final Map<String, Object> properties = allFeatures.values().stream()
                            .sorted()
                            .filter(x -> x.feature == feature)
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
            final FeatureDescription info = new FeatureDescription(feature, props);
            allFeatures.put(name, info);
            calculateActiveFeatures();
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
            final FeatureDescription info = new FeatureDescription(feature, props);
            allFeatures.remove(name, info);
            calculateActiveFeatures();
        } finally {
            featuresLock.unlock();
        }
    }

    /**
     * Calculates map of active features (eliminating Feature name
     * collisions)
     */
    private void calculateActiveFeatures() {
        final Map<String, Feature> activeMap = Maps.newHashMap();
        for (final Entry<String, FeatureDescription> entry : allFeatures.entries()) {
            final String key = entry.getKey();
            final SortedSet<FeatureDescription> value = (SortedSet<FeatureDescription>) allFeatures.get(key);
            activeMap.put(key, value.first().feature);
            if (value.size() > 1) {
                logger.warn("More than one features with same name - [{}] are available.", key);
            }
        }
        activeFeatures = activeMap;
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
            allStrategies.put(strategy.getName(), strategy);
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
            allStrategies.remove(strategy.getName());
        } finally {
            strategiesLock.unlock();
        }
    }

    /**
     * Internal class caching some feature meta data like service id and
     * ranking.
     */
    private static final class FeatureDescription implements Comparable<FeatureDescription> {
        private final int ranking;
        private final long serviceId;
        private final Feature feature;
        private final Map<String, Object> props;

        public FeatureDescription(final Feature feature, final Map<String, Object> props) {
            this.feature = feature;
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
        public int compareTo(final FeatureDescription o) {
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