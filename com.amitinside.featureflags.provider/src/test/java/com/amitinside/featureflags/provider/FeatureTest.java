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

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.amitinside.featureflags.provider.ConfiguredFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class FeatureTest {

    private static final String[] EMPTY_ARRAY = new String[0];
    private Map<String, Object> featureProperties;

    @Test
    public void testAllPropertiesAvailabilityOnActivate() {
        featureProperties = Maps.newHashMap();
        featureProperties.put("name", "feature1");
        featureProperties.put("description", "My Feature");
        featureProperties.put("enabled", true);
        featureProperties.put("groups", Lists.newArrayList("group1").toArray(EMPTY_ARRAY));
        featureProperties.put("strategy", "strategy1");

        final ConfiguredFeature feature = new ConfiguredFeature();
        feature.activate(featureProperties);

        assertEquals(feature.getName(), "feature1");
        assertEquals(feature.getDescription().get(), "My Feature");
        assertEquals(feature.isEnabled(), true);
        assertEquals(feature.getGroups().findAny().get(), "group1");
        assertEquals(feature.getStrategy().get(), "strategy1");
    }

    @Test
    public void testAllPropertiesAvailabilityOnUpdate() {
        featureProperties = Maps.newHashMap();
        featureProperties.put("name", "feature1");
        featureProperties.put("description", "My Feature");
        featureProperties.put("enabled", true);
        featureProperties.put("groups", Lists.newArrayList("group1").toArray(EMPTY_ARRAY));
        featureProperties.put("strategy", "strategy1");

        final ConfiguredFeature feature = new ConfiguredFeature();
        feature.updated(featureProperties);

        assertEquals(feature.getName(), "feature1");
        assertEquals(feature.getDescription().get(), "My Feature");
        assertEquals(feature.isEnabled(), true);
        assertEquals(feature.getGroups().findAny().get(), "group1");
        assertEquals(feature.getStrategy().get(), "strategy1");
    }

    @Test
    public void testDescriptionSetToNameIfMissing() {
        featureProperties = Maps.newHashMap();
        featureProperties.put("name", "feature1");
        featureProperties.put("enabled", true);
        featureProperties.put("groups", Lists.newArrayList("group1").toArray(EMPTY_ARRAY));
        featureProperties.put("strategy", "strategy1");

        final ConfiguredFeature feature = new ConfiguredFeature();
        feature.activate(featureProperties);

        assertTrue(feature.getDescription().isPresent());

        assertEquals(feature.getName(), "feature1");
        assertEquals(feature.getDescription().get(), "feature1");
        assertEquals(feature.isEnabled(), true);
        assertEquals(feature.getGroups().findAny().get(), "group1");
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

        assertFalse(feature.getGroups().findAny().isPresent());

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
        featureProperties.put("groups", Lists.newArrayList("group1").toArray(EMPTY_ARRAY));

        final ConfiguredFeature feature = new ConfiguredFeature();
        feature.activate(featureProperties);

        assertFalse(feature.getStrategy().isPresent());

        assertEquals(feature.getName(), "feature1");
        assertEquals(feature.isEnabled(), true);
        assertEquals(feature.getDescription().get(), "My Feature");
        assertEquals(feature.getGroups().findAny().get(), "group1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNameNull() {
        featureProperties = Maps.newHashMap();
        featureProperties.put("name", null);
        featureProperties.put("description", "My Feature");
        featureProperties.put("enabled", true);
        featureProperties.put("groups", Lists.newArrayList("group1").toArray(EMPTY_ARRAY));
        featureProperties.put("strategy", "strategy1");

        final ConfiguredFeature feature = new ConfiguredFeature();
        feature.activate(featureProperties);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNameEmpty() {
        featureProperties = Maps.newHashMap();
        featureProperties.put("name", "");
        featureProperties.put("description", "My Feature");
        featureProperties.put("enabled", true);
        featureProperties.put("groups", Lists.newArrayList("group1").toArray(EMPTY_ARRAY));
        featureProperties.put("strategy", "strategy1");

        final ConfiguredFeature feature = new ConfiguredFeature();
        feature.activate(featureProperties);
    }

    @Test
    public void testToString() {
        featureProperties = Maps.newHashMap();
        featureProperties.put("name", "feature1");
        featureProperties.put("description", "My Feature");
        featureProperties.put("enabled", true);
        featureProperties.put("groups", Lists.newArrayList("group1").toArray(EMPTY_ARRAY));
        featureProperties.put("strategy", "strategy1");

        final ConfiguredFeature feature = new ConfiguredFeature();
        feature.activate(featureProperties);

        assertEquals(feature.toString(),
                "ConfiguredFeature{Name=feature1, Description=My Feature, Strategy=strategy1, Groups=[group1], Enabled=true}");
    }

    @Test
    public void testEmptyToNull() {
        featureProperties = Maps.newHashMap();
        featureProperties.put("name", "feature1");
        featureProperties.put("description", "My Feature");
        featureProperties.put("enabled", true);
        featureProperties.put("groups", Lists.newArrayList().toArray(EMPTY_ARRAY));
        featureProperties.put("strategy", "");

        final ConfiguredFeature feature = new ConfiguredFeature();
        feature.activate(featureProperties);

        assertFalse(feature.getStrategy().isPresent());
        assertFalse(feature.getGroups().findAny().isPresent());

    }

}
