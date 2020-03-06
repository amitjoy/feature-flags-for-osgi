package com.amitinside.featureflags.provider;

import static com.amitinside.featureflags.provider.ManagerHelper.checkArgument;
import static com.amitinside.featureflags.provider.ManagerHelper.getConfiguredFeatures;
import static java.util.Objects.requireNonNull;
import static org.apache.felix.utils.log.Logger.LOG_INFO;
import static org.osgi.service.cm.ConfigurationEvent.CM_UPDATED;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.felix.utils.log.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.MetaTypeService;

import com.amitinside.featureflags.api.FeatureDTO;
import com.amitinside.featureflags.api.FeatureManager;
import com.amitinside.featureflags.provider.ManagerHelper.Feature;

/**
 * This implements the {@link FeatureManager}.
 */
@ProvideFeatureCapability
@Component(name = "FeatureManager")
public final class FeatureManagerProvider implements FeatureManager, ConfigurationListener {

    /** Data container -> Key: Configuration PID Value: Feature DTOs */
    private final Map<String, List<Feature>> allFeatures = new HashMap<>();

    /** Data container -> Key: Bundle Instance Value: Configuration PID(s) */
    private final Map<Bundle, List<String>>  bundlePIDs  = new HashMap<>();

    /** Logger Instance */
    private Logger                           logger;

    /** Metatype Extender Instance Reference */
    private MetaTypeExtender                 extender;

    /** Metatype Service Instance Reference */
    @Reference
    private MetaTypeService                  metaTypeService;

    /** Configuration Admin Service Instance Reference */
    @Reference
    private ConfigurationAdmin               configurationAdmin;

    @Activate
    protected void activate(final BundleContext bundleContext) throws Exception {
        logger   = new Logger(bundleContext);
        extender = new MetaTypeExtender(metaTypeService, logger, bundlePIDs, allFeatures);
        extender.start(bundleContext);
    }

    @Deactivate
    protected void deactivate(final BundleContext bundleContext) throws Exception {
        extender.stop(bundleContext);
    }

    /**
     * Returns the internal {@link MetaTypeExtender} instance. This is required for
     * unit testing purposes.
     */
    protected MetaTypeExtender getExtender() {
        return extender;
    }

    @Override
    public Stream<FeatureDTO> getFeatures() {
        return allFeatures.values()
                .stream()
                .flatMap(List::stream)
                .map(ManagerHelper::toFeatureDTO);
    }

    @Override
    public Stream<FeatureDTO> getFeatures(final String featureID) {
        requireNonNull(featureID, "Feature ID cannot be null");
        checkArgument(!featureID.isEmpty(), "Feature ID cannot be empty");

        return allFeatures.values()
                .stream()
                .flatMap(List::stream)
                .filter(f -> f.id.equals(featureID))
                .map(ManagerHelper::toFeatureDTO);
    }

    @Override
    public void updateFeature(final String featureID, final boolean isEnabled) {
        requireNonNull(featureID, "Feature ID cannot be null");
        checkArgument(!featureID.isEmpty(), "Feature ID cannot be empty");

        logger.log(LOG_INFO, String.format("Updating feature [%s] to [%b]", featureID, isEnabled));

        try {
            final List<String> configurations = allFeatures.entrySet()
                    .stream()
                    .filter(e -> e.getValue()
                            .stream()
                            .anyMatch(f -> f.id.equals(featureID)))
                    .map(Entry::getKey)
                    .collect(Collectors.toList());
            for (final String configurationPID : configurations) {
                final Configuration configuration = configurationAdmin.getConfiguration(configurationPID, "?");
                if (configuration != null) {
                    final Dictionary<String, Object> existingProps = configuration.getProperties();
                    final Map<String, Object>        newProps      = ManagerHelper.asMap(existingProps);
                    newProps.put(METATYPE_FEATURE_ID_PREFIX + featureID, isEnabled);
                    configuration.updateIfDifferent(new Hashtable<>(newProps));
                }
            }
        } catch (final Exception e) {
            // never occur since configuration location check has been ignored
        }
    }

    @Override
    public void configurationEvent(final ConfigurationEvent event) {
        final int    type = event.getType();
        final String pid  = event.getPid();
        if (type == CM_UPDATED) {
            final Map<String, Boolean> configuredFeatures = getConfiguredFeatures(pid, configurationAdmin);
            for (final Entry<String, Boolean> entry : configuredFeatures.entrySet()) {
                final String              featureID = entry.getKey();
                final boolean             isEnabled = entry.getValue();
                final Collection<Feature> features  = allFeatures.get(pid);
                features.stream()
                        .filter(f -> f.id.equalsIgnoreCase(featureID))
                        .peek(f -> logger.log(LOG_INFO,
                                String.format("Updated feature [%s] to [%b]", f.toString(), isEnabled)))
                        .forEach(f -> f.isEnabled = isEnabled);
            }
        } else {
            allFeatures.remove(pid);
        }
    }

}