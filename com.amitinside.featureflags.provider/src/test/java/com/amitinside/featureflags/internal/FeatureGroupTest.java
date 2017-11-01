/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.internal;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;

public final class FeatureGroupTest {

    private Map<String, Object> groupProperties;

    @Test
    public void testAllPropertiesAvailabilityOnActivate() {
        groupProperties = Maps.newHashMap();
        groupProperties.put("name", "group1");
        groupProperties.put("description", "My Group");
        groupProperties.put("enabled", true);
        groupProperties.put("strategy", "strategy1");

        final ConfiguredFeatureGroup group = new ConfiguredFeatureGroup();
        group.activate(groupProperties);

        assertEquals(group.getName(), "group1");
        assertEquals(group.getDescription().get(), "My Group");
        assertEquals(group.isEnabled(), true);
        assertEquals(group.getStrategy().get(), "strategy1");
    }

    @Test
    public void testAllPropertiesAvailabilityOnUpdate() {
        groupProperties = Maps.newHashMap();
        groupProperties.put("name", "group1");
        groupProperties.put("description", "My Group");
        groupProperties.put("enabled", true);
        groupProperties.put("strategy", "strategy1");

        final ConfiguredFeatureGroup group = new ConfiguredFeatureGroup();
        group.updated(groupProperties);

        assertEquals(group.getName(), "group1");
        assertEquals(group.getDescription().get(), "My Group");
        assertEquals(group.isEnabled(), true);
        assertEquals(group.getStrategy().get(), "strategy1");
    }

    @Test
    public void testDescriptionSetToNameIfMissing() {
        groupProperties = Maps.newHashMap();
        groupProperties.put("name", "group1");
        groupProperties.put("enabled", true);
        groupProperties.put("strategy", "strategy1");

        final ConfiguredFeatureGroup group = new ConfiguredFeatureGroup();
        group.activate(groupProperties);

        assertTrue(group.getDescription().isPresent());

        assertEquals(group.getName(), "group1");
        assertEquals(group.getDescription().get(), "group1");
        assertEquals(group.isEnabled(), true);
        assertEquals(group.getStrategy().get(), "strategy1");
    }

    @Test
    public void testStrategyMissing() {
        groupProperties = Maps.newHashMap();
        groupProperties.put("name", "group1");
        groupProperties.put("description", "My Group");
        groupProperties.put("enabled", true);

        final ConfiguredFeatureGroup group = new ConfiguredFeatureGroup();
        group.activate(groupProperties);

        assertFalse(group.getStrategy().isPresent());

        assertEquals(group.getName(), "group1");
        assertEquals(group.isEnabled(), true);
        assertEquals(group.getDescription().get(), "My Group");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNameNull() {
        groupProperties = Maps.newHashMap();
        groupProperties.put("name", null);
        groupProperties.put("description", "My Group");
        groupProperties.put("enabled", true);
        groupProperties.put("strategy", "strategy1");

        final ConfiguredFeatureGroup group = new ConfiguredFeatureGroup();
        group.activate(groupProperties);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNameEmpty() {
        groupProperties = Maps.newHashMap();
        groupProperties.put("name", "");
        groupProperties.put("description", "My Group");
        groupProperties.put("enabled", true);
        groupProperties.put("strategy", "strategy1");

        final ConfiguredFeatureGroup group = new ConfiguredFeatureGroup();
        group.activate(groupProperties);
    }

    @Test
    public void testToString() {
        groupProperties = Maps.newHashMap();
        groupProperties.put("name", "group1");
        groupProperties.put("description", "My Group");
        groupProperties.put("enabled", true);
        groupProperties.put("strategy", "strategy1");

        final ConfiguredFeatureGroup group = new ConfiguredFeatureGroup();
        group.activate(groupProperties);

        assertEquals(group.toString(),
                "ConfiguredFeatureGroup{Name=group1, Description=My Group, Strategy=strategy1, Enabled=true}");
    }

    @Test
    public void testEmptyToNull() {
        groupProperties = Maps.newHashMap();
        groupProperties.put("name", "group1");
        groupProperties.put("description", "My Group");
        groupProperties.put("enabled", true);
        groupProperties.put("strategy", "");

        final ConfiguredFeatureGroup group = new ConfiguredFeatureGroup();
        group.activate(groupProperties);

        assertFalse(group.getStrategy().isPresent());
    }

}
