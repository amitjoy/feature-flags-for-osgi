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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.Feature;
import com.amitinside.featureflags.FeatureService;
import com.google.common.collect.Maps;

/**
 * This service implements the {@link FeatureService}. It keeps track of all
 * {@link Feature} services.
 */
@Component
public final class FeatureManager implements FeatureService {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    private void bindFeature(final Feature f, final Map<String, Object> props) {
        synchronized (allFeatures) {
            final String name = f.name();
            final FeatureDescription info = new FeatureDescription(f, props);

            List<FeatureDescription> candidates = allFeatures.get(name);
            if (candidates == null) {
                candidates = new ArrayList<>();
                allFeatures.put(name, candidates);
            }
            candidates.add(info);
            Collections.sort(candidates);

            calculateActiveProviders();
        }
    }

    /**
     * {@link Feature} service unbinding callback
     */
    @SuppressWarnings("unused")
    private void unbindFeature(final Feature f, final Map<String, Object> props) {
        synchronized (allFeatures) {
            final String name = f.name();
            final FeatureDescription info = new FeatureDescription(f, props);

            final List<FeatureDescription> candidates = allFeatures.get(name);
            if (candidates != null) { // sanity check
                candidates.remove(info);
                if (candidates.isEmpty()) {
                    allFeatures.remove(name);
                }
            }
            calculateActiveProviders();
        }
    }

    /**
     * Calculates map of active features (eliminating Feature name
     * collisions). Must be called while synchronized on this.allFeatures
     */
    private void calculateActiveProviders() {
        final Map<String, Feature> activeMap = new HashMap<>();
        for (final Map.Entry<String, List<FeatureDescription>> entry : allFeatures.entrySet()) {
            final FeatureDescription desc = entry.getValue().get(0);
            activeMap.put(entry.getKey(), desc.feature);
            if (entry.getValue().size() > 1) {
                logger.warn("More than one feature service for feature {}", entry.getKey());
            }
        }
        activeFeatures = activeMap;
    }

    /**
     * Internal class caching some feature meta data like service id and
     * ranking.
     */
    private static final class FeatureDescription implements Comparable<FeatureDescription> {

        public final int ranking;

        public final long serviceId;

        public final Feature feature;

        public FeatureDescription(final Feature feature, final Map<String, Object> props) {
            this.feature = feature;
            final Object sr = props.get(Constants.SERVICE_RANKING);
            if (sr instanceof Integer) {
                ranking = (Integer) sr;
            } else {
                ranking = 0;
            }
            serviceId = (Long) props.get(Constants.SERVICE_ID);
        }

        @Override
        public int compareTo(final FeatureDescription o) {
            if (ranking < o.ranking) {
                return 1;
            } else if (ranking > o.ranking) {
                return -1;
            }
            // If ranks are equal, then sort by service id in descending order.
            return serviceId < o.serviceId ? -1 : 1;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof FeatureDescription) {
                return ((FeatureDescription) obj).serviceId == serviceId;
            }
            return false;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (serviceId ^ serviceId >>> 32);
            return result;
        }
    }
}