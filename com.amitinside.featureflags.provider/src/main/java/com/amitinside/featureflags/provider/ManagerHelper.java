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

import static com.amitinside.featureflags.FeatureManager.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;
import static org.osgi.service.metatype.ObjectClassDefinition.ALL;

import java.util.List;
import java.util.Map;

import org.apache.felix.utils.collections.DictionaryAsMap;
import org.apache.felix.utils.manifest.Clause;
import org.apache.felix.utils.manifest.Parser;
import org.osgi.framework.Bundle;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;

import com.amitinside.featureflags.dto.ConfigurationDTO;
import com.amitinside.featureflags.dto.FeatureDTO;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Feature Manager Helper class
 */
public final class ManagerHelper {

    /** Constructor */
    private ManagerHelper() {
        throw new IllegalAccessError("Non-Instantiable");
    }

    /**
     * Placeholder for Feature DTO. Used for internal purposes.
     */
    public static class Feature {
        public String id;
        public String name;
        public String description;
        public boolean isEnabled;
        public Map<String, String> properties;
    }

    public static String extractFeatureID(final String id) {
        requireNonNull(id, "Feature ID cannot be null");
        return id.substring(METATYPE_FEATURE_ID_PREFIX.length(), id.length());
    }

    public static FeatureDTO toFeatureDTO(final Feature f) {
        requireNonNull(f, "Feature cannot be null");
        final FeatureDTO feature = new FeatureDTO();
        feature.id = f.id;
        feature.name = f.name;
        feature.description = f.description;
        feature.isEnabled = f.isEnabled;
        feature.properties = f.properties;
        return feature;
    }

    public static ConfigurationDTO toConfigurationDTO(final String pid, final List<Feature> features) {
        requireNonNull(pid, "Configuration PID cannot be null");
        requireNonNull(features, "List of Features cannot be null");

        final ConfigurationDTO config = new ConfigurationDTO();
        config.pid = pid;
        //@formatter:off
        config.features = features.stream()
                                  .map(ManagerHelper::toFeatureDTO)
                                  .collect(collectingAndThen(toList(), ImmutableList::copyOf));
        //@formatter:on
        return config;
    }

    public static Feature toFeature(final String id, final String name, final String description,
            final String[] defaultValues, final String[] labels, final String[] values) {
        requireNonNull(id, "Feature ID cannot be null");
        final Feature feature = new Feature();
        feature.id = extractFeatureID(id);
        feature.name = name;
        feature.description = description;
        feature.isEnabled = defaultValues == null ? false : Boolean.valueOf(defaultValues[0]);
        feature.properties = mergeAsMap(labels, values);
        return feature;
    }

    public static List<String> getPIDs(final Bundle bundle, final MetaTypeService metaTypeService) {
        requireNonNull(bundle, "Bundle Instance cannot be null");
        requireNonNull(metaTypeService, "Metatype Service Instance cannot be null");

        final MetaTypeInformation metatypeInfo = metaTypeService.getMetaTypeInformation(bundle);
        return ManagerHelper.asList(metatypeInfo.getPids());
    }

    public static List<AttributeDefinition> getAttributeDefinitions(final Bundle bundle, final String pid,
            final MetaTypeService metaTypeService) {
        requireNonNull(bundle, "Bundle Instance cannot be null");
        requireNonNull(pid, "Configuration PID cannot be null");
        requireNonNull(metaTypeService, "Metatype Service Instance cannot be null");

        final MetaTypeInformation metaTypeInformation = metaTypeService.getMetaTypeInformation(bundle);
        final ObjectClassDefinition ocd = metaTypeInformation.getObjectClassDefinition(pid, null);
        return ManagerHelper.asList(ocd.getAttributeDefinitions(ALL));
    }

    public static <K, V> Map<K, V> mergeAsMap(final K[] labels, final V[] values) {
        if (labels == null || values == null || labels.length != values.length) {
            return null;
        }
        final ImmutableMap.Builder<K, V> properties = ImmutableMap.builder();
        for (int i = 0; i < values.length; i++) {
            properties.put(labels[i], values[i]);
        }
        return properties.build();
    }

    public static <T> List<T> asList(final T[] elements) {
        return elements == null ? ImmutableList.of() : ImmutableList.copyOf(elements);
    }

    public static boolean hasFeatureCapability(final Bundle bundle) {
        requireNonNull(bundle, "Bundle Instance cannot be null");

        final String extenderNamespace = "osgi.extender";
        final String capabilityToMatch = "(" + extenderNamespace + "=" + FEATURE_CAPABILITY_NAME + ")";

        @SuppressWarnings("unchecked")
        final Map<String, String> headers = new DictionaryAsMap<>(bundle.getHeaders());
        final String header = headers.get("Require-Capability");

        final Clause[] clauses = Parser.parseHeader(header);
        for (final Clause clause : clauses) {
            final String nameSpace = clause.getName();
            if (extenderNamespace.equals(nameSpace)) {
                final String directive = clause.getDirective("filter");
                if (capabilityToMatch.contains(directive)) {
                    return true;
                }
            }
        }
        return false;
    }

}
