/*******************************************************************************
 * Copyright (c) 2017 QIVICON
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
package com.qivicon.featureflags.internal;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.qivicon.featureflags.internal.ConfiguredFeature;

public final class FeatureTest {

    private Map<String, Object> featureProperties;

    @Test
    public void testAllPropertiesAvailability() {
        featureProperties = Maps.newHashMap();
        featureProperties.put("name", "feature1");
        featureProperties.put("description", "My Feature");
        featureProperties.put("enabled", true);
        featureProperties.put("group", "group1");
        featureProperties.put("strategy", "strategy1");

        final ConfiguredFeature feature = new ConfiguredFeature();
        feature.activate(featureProperties);

        assertEquals(feature.getName(), "feature1");
        assertEquals(feature.getDescription().get(), "My Feature");
        assertEquals(feature.isEnabled(), true);
        assertEquals(feature.getGroup().get(), "group1");
        assertEquals(feature.getStrategy().get(), "strategy1");
    }

    @Test
    public void testDescriptionSetToNameIfMissing() {
        featureProperties = Maps.newHashMap();
        featureProperties.put("name", "feature1");
        featureProperties.put("enabled", true);
        featureProperties.put("group", "group1");
        featureProperties.put("strategy", "strategy1");

        final ConfiguredFeature feature = new ConfiguredFeature();
        feature.activate(featureProperties);

        assertTrue(feature.getDescription().isPresent());

        assertEquals(feature.getName(), "feature1");
        assertEquals(feature.getDescription().get(), "feature1");
        assertEquals(feature.isEnabled(), true);
        assertEquals(feature.getGroup().get(), "group1");
        assertEquals(feature.getStrategy().get(), "strategy1");
    }

    @Test
    public void testGroupMissing() {
        featureProperties = Maps.newHashMap();
        featureProperties.put("name", "feature1");
        featureProperties.put("description", "My Feature");
        featureProperties.put("enabled", true);
        featureProperties.put("strategy", "strategy1");

        final ConfiguredFeature feature = new ConfiguredFeature();
        feature.activate(featureProperties);

        assertFalse(feature.getGroup().isPresent());

        assertEquals(feature.getName(), "feature1");
        assertEquals(feature.isEnabled(), true);
        assertEquals(feature.getDescription().get(), "My Feature");
        assertEquals(feature.getStrategy().get(), "strategy1");
    }

    @Test
    public void testStrategyMissing() {
        featureProperties = Maps.newHashMap();
        featureProperties.put("name", "feature1");
        featureProperties.put("description", "My Feature");
        featureProperties.put("enabled", true);
        featureProperties.put("group", "group1");

        final ConfiguredFeature feature = new ConfiguredFeature();
        feature.activate(featureProperties);

        assertFalse(feature.getStrategy().isPresent());

        assertEquals(feature.getName(), "feature1");
        assertEquals(feature.isEnabled(), true);
        assertEquals(feature.getDescription().get(), "My Feature");
        assertEquals(feature.getGroup().get(), "group1");
    }

}
