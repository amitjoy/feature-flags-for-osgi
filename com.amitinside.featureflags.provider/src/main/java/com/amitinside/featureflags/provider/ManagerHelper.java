package com.amitinside.featureflags.provider;

import static com.amitinside.featureflags.api.FeatureManager.METATYPE_FEATURE_ID_PREFIX;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.osgi.service.metatype.ObjectClassDefinition.ALL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.felix.utils.collections.DictionaryAsMap;
import org.osgi.framework.Bundle;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;

import com.amitinside.featureflags.api.FeatureDTO;

/**
 * Feature Manager Helper class
 */
public final class ManagerHelper {

    /** Constructor */
    private ManagerHelper() {
        throw new IllegalAccessError("Non-Instantiable");
    }

    public static void checkArgument(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Placeholder for Feature DTO. Used for internal purposes.
     */
    public static class Feature {
        public String  id;
        public long    bundleId;
        public String  name;
        public String  description;
        public boolean isEnabled;
    }

    public static String getFeatureID(final String id) {
        requireNonNull(id, "Feature ID cannot be null");
        return id.substring(METATYPE_FEATURE_ID_PREFIX.length(), id.length());
    }

    public static FeatureDTO toFeatureDTO(final Feature f) {
        requireNonNull(f, "Feature cannot be null");
        final FeatureDTO feature = new FeatureDTO();
        feature.id          = f.id;
        feature.bundleId    = f.bundleId;
        feature.name        = f.name;
        feature.description = f.description;
        feature.isEnabled   = f.isEnabled;
        return feature;
    }

    public static Feature toFeature(final AttributeDefinition ad, final long bundleId) {
        requireNonNull(ad, "Attribute Definition cannot be null");

        final Feature feature = new Feature();
        feature.id = getFeatureID(ad.getID());

        final String name = ad.getName();
        feature.name        = name != null ? name : feature.id;

        feature.description = ad.getDescription();

        final String[] defaultValue = ad.getDefaultValue();
        feature.isEnabled = defaultValue == null ? false : Boolean.valueOf(defaultValue[0]);

        feature.bundleId  = bundleId;
        return feature;
    }

    public static List<String> getPIDs(final Bundle bundle, final MetaTypeService metaTypeService) {
        requireNonNull(bundle, "Bundle Instance cannot be null");
        requireNonNull(metaTypeService, "MetaType Service Instance cannot be null");

        final MetaTypeInformation metatypeInfo = metaTypeService.getMetaTypeInformation(bundle);
        return ManagerHelper.asList(metatypeInfo.getPids());
    }

    public static List<AttributeDefinition> getAttributeDefinitions(final Bundle bundle, final String pid,
            final MetaTypeService metaTypeService) {
        requireNonNull(bundle, "Bundle Instance cannot be null");
        requireNonNull(pid, "Configuration PID cannot be null");
        requireNonNull(metaTypeService, "MetaType Service Instance cannot be null");

        final MetaTypeInformation   metaTypeInformation = metaTypeService.getMetaTypeInformation(bundle);
        final ObjectClassDefinition ocd                 = metaTypeInformation.getObjectClassDefinition(pid, null);
        return asList(ocd.getAttributeDefinitions(ALL));
    }

    public static Map<String, List<Feature>> getFeaturesFromAttributeDefinitions(final Bundle bundle, final String pid,
            final MetaTypeService metaTypeService) {
        requireNonNull(bundle, "Bundle Instance cannot be null");
        requireNonNull(pid, "Configuration PID cannot be null");
        requireNonNull(metaTypeService, "MetaType Service Instance cannot be null");

        final Map<String, List<Feature>> allFeatures = new HashMap<>();
        for (final AttributeDefinition ad : getAttributeDefinitions(bundle, pid, metaTypeService)) {
            if (ad.getID().startsWith(METATYPE_FEATURE_ID_PREFIX)) {
                List<Feature> features = null;
                if (allFeatures.get(pid) != null) {
                    features = allFeatures.get(pid);
                } else {
                    features = new ArrayList<>();
                    allFeatures.put(pid, features);
                }
                features.add(toFeature(ad, bundle.getBundleId()));
            }
        }
        return allFeatures;
    }

    public static Map<String, Boolean> getConfiguredFeatures(final String configurationPID,
            final ConfigurationAdmin configurationAdmin) {
        try {
            final Configuration       configuration = configurationAdmin.getConfiguration(configurationPID, "?");
            final Map<String, Object> properties    = new DictionaryAsMap<>(configuration.getProperties());
            // @formatter:off
			return properties.entrySet().stream()
										.filter(e -> e.getKey().startsWith(METATYPE_FEATURE_ID_PREFIX))
										.filter(e -> e.getValue() instanceof Boolean)
										.collect(toMap(e -> getFeatureID(e.getKey()), e -> (Boolean) e.getValue()));
			// @formatter:on
        } catch (final Exception e) {
            // cannot do anything
        }
        return Collections.emptyMap();
    }

    public static Map<String, Object> asMap(Dictionary<String, Object> dictionary) {
        if (dictionary == null) {
            return new HashMap<>();
        }
        final List<String> keys = Collections.list(dictionary.keys());
        return keys.stream().collect(toMap(Function.identity(), dictionary::get));
    }

    public static <T> List<T> asList(final T[] elements) {
        return elements == null ? Collections.emptyList() : Collections.unmodifiableList(Arrays.asList(elements));
    }

}
