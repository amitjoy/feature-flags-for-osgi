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

import static com.amitinside.featureflags.FeatureManager.FEATURE_ID_PREFIX;
import static com.amitinside.featureflags.provider.FeatureManagerProvider.extractFeatureID;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.osgi.service.metatype.ObjectClassDefinition.ALL;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.provider.FeatureManagerProvider.Feature;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * The {@link MetaTypeTrackerCustomizer} is used to track all existing metatype
 * informations in a bundle. It specifically tracks if the associated metatype
 * information does specify any feature.
 */
public final class MetaTypeTrackerCustomizer implements BundleTrackerCustomizer {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(MetaTypeTrackerCustomizer.class);

    /** Metatype Service Instance Reference */
    private final MetaTypeService metaTypeService;

    /** Data container -> Key: Bundle Instance Value: Configuration PID(s) */
    private final Multimap<Bundle, String> bundlePids;

    /** Data container -> Key: Configuration PID Value: Feature DTOs */
    private final Multimap<String, Feature> allFeatures;

    /**
     * Constructor
     *
     * @param metaTypeService {@link MetaTypeService} instance
     * @param bundlePids container to store all configuration PIDs associated in a bundle's metatype
     * @param allFeatures container to store all configuration PIDs in the runtime
     *
     * @throws NullPointerException if any of the specified arguments is {@code null}
     */
    public MetaTypeTrackerCustomizer(final MetaTypeService metaTypeService, final Multimap<Bundle, String> bundlePids,
            final Multimap<String, Feature> allFeatures) {
        requireNonNull(metaTypeService, "MetaTypeService instance cannot be null");
        requireNonNull(bundlePids, "Multimap instance cannot be null");
        requireNonNull(allFeatures, "Multimap instance cannot be null");

        this.metaTypeService = metaTypeService;
        this.bundlePids = bundlePids;
        this.allFeatures = allFeatures;
    }

    @Override
    public Object addingBundle(final Bundle bundle, final BundleEvent event) {
        logger.trace("Adding bundle [{}] to the Metatype Tracker", bundle.getSymbolicName());

        for (final String pid : getPIDs(bundle)) {
            bundlePids.put(bundle, pid);
            for (final AttributeDefinition ad : getAttributeDefinitions(bundle, pid)) {
                if (ad.getID().startsWith(FEATURE_ID_PREFIX)) {
                    //@formatter:off
                    allFeatures.put(pid, toFeature(ad.getID(),
                                                   ad.getName(),
                                                   ad.getDescription(),
                                                   ad.getDefaultValue(),
                                                   ad.getOptionLabels(),
                                                   ad.getOptionValues()));
                    //@formatter:on
                }
            }
        }
        return bundle;
    }

    @Override
    public void modifiedBundle(final Bundle bundle, final BundleEvent event, final Object object) {
        // not required
    }

    @Override
    public void removedBundle(final Bundle bundle, final BundleEvent event, final Object object) {
        logger.trace("Removing bundle [{}] from the Metatype Tracker", bundle.getSymbolicName());

        if (bundlePids.containsKey(bundle)) {
            final Collection<String> pids = bundlePids.get(bundle);
            bundlePids.removeAll(bundle);
            for (final String pid : pids) {
                allFeatures.removeAll(pid);
            }
        }
    }

    private List<String> getPIDs(final Bundle bundle) {
        final MetaTypeInformation metatypeInfo = metaTypeService.getMetaTypeInformation(bundle);
        return Lists.newArrayList(metatypeInfo.getPids());
    }

    private List<AttributeDefinition> getAttributeDefinitions(final Bundle bundle, final String pid) {
        final MetaTypeInformation metaTypeInformation = metaTypeService.getMetaTypeInformation(bundle);
        final ObjectClassDefinition ocd = metaTypeInformation.getObjectClassDefinition(pid, null);
        final AttributeDefinition[] ads = ocd.getAttributeDefinitions(ALL);
        return ads == null ? ImmutableList.of() : Lists.newArrayList(ads);
    }

    private static Feature toFeature(final String id, final String name, final String description,
            final String[] defaultValues, final String[] labels, final String[] values) {
        final Feature feature = new Feature();
        feature.id = extractFeatureID(id);
        feature.name = name;
        feature.description = description;
        feature.isEnabled = defaultValues == null ? false : Boolean.valueOf(defaultValues[0]);
        feature.properties = combineArrays(labels, values);
        return feature;
    }

    private static Map<String, String> combineArrays(final String[] labels, final String[] values) {
        if (labels == null || values == null || labels.length != values.length) {
            return null;
        }
        //@formatter:off
        return IntStream.range(0, values.length)
                        .boxed()
                        .collect(toMap(i -> labels[i], i -> values[i]));
        //@formatter:on
    }

}
