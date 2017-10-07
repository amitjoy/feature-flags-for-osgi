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
package com.amitinside.featureflags.impl;

import static org.osgi.framework.Constants.*;
import static org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.Feature;
import com.amitinside.featureflags.FeatureService;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This service implements the {@link FeatureService}. It keeps track of all
 * {@link Feature} services.
 */
@Component
public final class FeatureManager implements FeatureService {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, List<FeatureDescription>> allFeatures = Maps.newHashMap();
    private Map<String, Feature> activeFeatures = Maps.newHashMap();

    @Override
    public Stream<Feature> getFeatures() {
        return activeFeatures.values().stream();
    }

    @Override
    public Feature getFeature(final String name) {
        return activeFeatures.get(name);
    }

    @Override
    public boolean isEnabled(final String featureName) {
        final Feature feature = getFeature(featureName);
        if (feature != null) {
            return feature.isEnabled();
        }
        return false;
    }

    /**
     * {@link Feature} service binding callback
     */
    @Reference(cardinality = MULTIPLE, policy = DYNAMIC)
    private void bindFeature(final Feature feature, final Map<String, Object> props) {
        synchronized (allFeatures) {
            final String name = feature.name();
            final FeatureDescription info = new FeatureDescription(feature, props);

            List<FeatureDescription> candidates = allFeatures.get(name);
            if (candidates == null) {
                candidates = Lists.newArrayList();
                allFeatures.put(name, candidates);
            }
            candidates.add(info);
            Collections.sort(candidates);

            calculateActiveFeatures();
        }
    }

    /**
     * {@link Feature} service unbinding callback
     */
    @SuppressWarnings("unused")
    private void unbindFeature(final Feature feature, final Map<String, Object> props) {
        synchronized (allFeatures) {
            final String name = feature.name();
            final FeatureDescription info = new FeatureDescription(feature, props);

            final List<FeatureDescription> candidates = allFeatures.get(name);
            if (candidates != null) { // sanity check
                candidates.remove(info);
                if (candidates.isEmpty()) {
                    allFeatures.remove(name);
                }
            }
            calculateActiveFeatures();
        }
    }

    /**
     * Calculates map of active features (eliminating Feature name
     * collisions)
     */
    private void calculateActiveFeatures() {
        final Map<String, Feature> activeMap = Maps.newHashMap();
        for (final Entry<String, List<FeatureDescription>> entry : allFeatures.entrySet()) {
            final List<FeatureDescription> value = entry.getValue();
            final FeatureDescription desc = value.get(0);
            activeMap.put(entry.getKey(), desc.feature);
            if (value.size() > 1) {
                logger.warn("More than one feature of same name - [{}] are available.", entry.getKey());
            }
        }
        activeFeatures = activeMap;
    }

    /**
     * Internal class caching some feature meta data like service id and
     * ranking.
     */
    private static final class FeatureDescription implements Comparable<FeatureDescription> {

        private final int ranking;
        private final long serviceId;
        private final Feature feature;

        public FeatureDescription(final Feature feature, final Map<String, Object> props) {
            this.feature = feature;
            final Object sr = props.get(SERVICE_RANKING);
            if (sr instanceof Integer) {
                ranking = (int) sr;
            } else {
                ranking = 0;
            }
            serviceId = (long) props.get(SERVICE_ID);
        }

        /**
         * If service rankings are equal, then sort by service ID in descending order.
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