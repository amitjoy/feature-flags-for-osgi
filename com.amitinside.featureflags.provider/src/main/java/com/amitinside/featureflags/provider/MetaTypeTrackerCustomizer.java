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

import static com.amitinside.featureflags.FeatureManager.METATYPE_FEATURE_ID_PREFIX;
import static com.amitinside.featureflags.provider.ManagerHelper.*;
import static java.util.Objects.requireNonNull;

import java.util.Collection;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.provider.ManagerHelper.Feature;
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
        this.metaTypeService = requireNonNull(metaTypeService, "MetaTypeService instance cannot be null");
        this.bundlePids = requireNonNull(bundlePids, "Multimap instance cannot be null");
        this.allFeatures = requireNonNull(allFeatures, "Multimap instance cannot be null");
    }

    @Override
    public Object addingBundle(final Bundle bundle, final BundleEvent event) {
        logger.trace("Adding bundle [{}] to the Metatype Tracker", bundle.getSymbolicName());

        for (final String pid : getPIDs(bundle, metaTypeService)) {
            bundlePids.put(bundle, pid);
            for (final AttributeDefinition ad : getAttributeDefinitions(bundle, pid, metaTypeService)) {
                if (ad.getID().startsWith(METATYPE_FEATURE_ID_PREFIX)) {
                    logger.trace("Found bundle [{}] with feature(s)", bundle.getSymbolicName());
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

        final Collection<String> pids = bundlePids.get(bundle);
        bundlePids.removeAll(bundle);
        for (final String pid : pids) {
            allFeatures.removeAll(pid);
        }
    }

}
