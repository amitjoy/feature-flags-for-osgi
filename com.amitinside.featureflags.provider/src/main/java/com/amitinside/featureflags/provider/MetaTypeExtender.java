package com.amitinside.featureflags.provider;

import static com.amitinside.featureflags.provider.ManagerHelper.getFeaturesFromAttributeDefinitions;
import static com.amitinside.featureflags.provider.ManagerHelper.getPIDs;
import static java.util.Objects.requireNonNull;
import static org.apache.felix.utils.log.Logger.LOG_DEBUG;
import static org.apache.felix.utils.log.Logger.LOG_ERROR;
import static org.apache.felix.utils.log.Logger.LOG_WARNING;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.felix.utils.extender.AbstractExtender;
import org.apache.felix.utils.extender.Extension;
import org.apache.felix.utils.extender.SimpleExtension;
import org.apache.felix.utils.log.Logger;
import org.osgi.framework.Bundle;
import org.osgi.service.metatype.MetaTypeService;

import com.amitinside.featureflags.provider.ManagerHelper.Feature;

/**
 * This extender tracks started bundles for all existing feature metatype
 * informations (or starting if they have a lazy activation policy) and will
 * create an {@link Extension} for each of them to manage it.
 *
 * The extender will handle all concurrency and synchronization issues.
 *
 * The extender guarantee that all extensions will be stopped synchronously with
 * the STOPPING event of a given bundle and that all extensions will be stopped
 * before the extender bundle is stopped.
 */
public final class MetaTypeExtender extends AbstractExtender {

    /** Logger Instance */
    private final Logger                     logger;

    /** Metatype Service Instance Reference */
    private final MetaTypeService            metaTypeService;

    /** Data container -> Key: Bundle Instance Value: Configuration PID(s) */
    private final Map<Bundle, List<String>>  bundlePIDs;

    /** Data container -> Key: Configuration PID Value: Feature DTOs */
    private final Map<String, List<Feature>> allFeatures;

    /**
     * Constructor
     *
     * @param metaTypeService {@link MetaTypeService} instance
     * @param bundlePIDs container to store all configuration PIDs associated
     *            in a bundle's metatype
     * @param allFeatures container to store all configuration PIDs in the
     *            runtime
     *
     * @throws NullPointerException if any of the specified arguments is
     *             {@code null}
     */
    public MetaTypeExtender(final MetaTypeService metaTypeService, final Logger logger,
            final Map<Bundle, List<String>> bundlePIDs, final Map<String, List<Feature>> allFeatures) {
        this.logger          = requireNonNull(logger, "Logger instance cannot be null");
        this.metaTypeService = requireNonNull(metaTypeService, "MetaTypeService instance cannot be null");
        this.bundlePIDs      = requireNonNull(bundlePIDs, "Bundle PIDs map instance cannot be null");
        this.allFeatures     = requireNonNull(allFeatures, "All features map instance cannot be null");
    }

    @Override
    protected Extension doCreateExtension(final Bundle bundle) throws Exception {
        return new MetaTypeExtension(bundle);
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

    private class MetaTypeExtension extends SimpleExtension {
        private final Bundle bundle;

        public MetaTypeExtension(final Bundle bundle) {
            super(bundle);
            this.bundle = bundle;
        }

        @Override
        protected void doStart() throws Exception {
            for (final String pid : getPIDs(bundle, metaTypeService)) {
                final Map<String, List<Feature>> featuresFromADs = getFeaturesFromAttributeDefinitions(bundle, pid,
                        metaTypeService);
                allFeatures.putAll(featuresFromADs);
                bundlePIDs.computeIfAbsent(bundle, p -> new ArrayList<>())
                        .add(pid);
            }
        }

        @Override
        protected void doDestroy() throws Exception {
            final Collection<String> pids = bundlePIDs.get(bundle);
            pids.forEach(allFeatures::remove);
            bundlePIDs.remove(bundle);
        }
    }

}
