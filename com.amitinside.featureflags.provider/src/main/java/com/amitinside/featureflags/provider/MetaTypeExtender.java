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

import static com.amitinside.featureflags.provider.ManagerHelper.*;
import static java.util.Objects.requireNonNull;
import static org.osgi.service.log.LogService.*;

import java.util.Collection;

import org.apache.felix.utils.extender.AbstractExtender;
import org.apache.felix.utils.extender.Extension;
import org.apache.felix.utils.extender.SimpleExtension;
import org.apache.felix.utils.log.Logger;
import org.osgi.framework.Bundle;
import org.osgi.service.metatype.MetaTypeService;

import com.amitinside.featureflags.provider.ManagerHelper.Feature;
import com.google.common.collect.Multimap;

/**
 * This extender tracks started bundles for all existing feature metatype
 * informations (or starting if they have a lazy activation policy) and
 * will create an {@link Extension} for each of them to manage it.
 *
 * The extender will handle all concurrency and synchronization issues.
 *
 * The extender guarantee that all extensions will be stopped synchronously with
 * the STOPPING event of a given bundle and that all extensions will be stopped
 * before the extender bundle is stopped.
 */
public final class MetaTypeExtender extends AbstractExtender {

    /** Logger Instance */
    private final Logger logger;

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
    public MetaTypeExtender(final MetaTypeService metaTypeService, final Logger logger,
            final Multimap<Bundle, String> bundlePids, final Multimap<String, Feature> allFeatures) {
        this.logger = requireNonNull(logger, "Logger instance cannot be null");
        this.metaTypeService = requireNonNull(metaTypeService, "MetaTypeService instance cannot be null");
        this.bundlePids = requireNonNull(bundlePids, "Multimap instance cannot be null");
        this.allFeatures = requireNonNull(allFeatures, "Multimap instance cannot be null");
    }

    @Override
    protected Extension doCreateExtension(final Bundle bundle) throws Exception {
        return new MetatypeExtension(bundle);
    }

    @Override
    protected void debug(final Bundle bundle, final String msg) {
        logger.log(LOG_DEBUG, " [" + bundle.getSymbolicName() + "] " + msg);
    }

    @Override
    protected void warn(final Bundle bundle, final String msg, final Throwable t) {
        logger.log(LOG_WARNING, " [" + bundle.getSymbolicName() + "] " + msg);
    }

    @Override
    protected void error(final String msg, final Throwable t) {
        logger.log(LOG_ERROR, msg, t);
    }

    private class MetatypeExtension extends SimpleExtension {
        private final Bundle bundle;

        public MetatypeExtension(final Bundle bundle) {
            super(bundle);
            this.bundle = bundle;
        }

        @Override
        protected void doStart() throws Exception {
            for (final String pid : getPIDs(bundle, metaTypeService)) {
                if (allFeatures.putAll(getFeaturesFromAttributeDefinitions(bundle, pid, metaTypeService))) {
                    bundlePids.put(bundle, pid);
                }
            }
        }

        @Override
        protected void doDestroy() throws Exception {
            final Collection<String> pids = bundlePids.get(bundle);
            for (final String pid : pids) {
                allFeatures.removeAll(pid);
            }
            bundlePids.removeAll(bundle);
        }
    }

}
