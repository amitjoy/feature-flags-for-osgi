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
import com.qivicon.featureflags.internal.ConfiguredFeatureGroup;

public final class FeatureGroupTest {

    private Map<String, Object> groupProperties;

    @Test
    public void testAllPropertiesAvailability() {
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
        groupProperties.put("group", "group1");

        final ConfiguredFeatureGroup group = new ConfiguredFeatureGroup();
        group.activate(groupProperties);

        assertFalse(group.getStrategy().isPresent());

        assertEquals(group.getName(), "group1");
        assertEquals(group.isEnabled(), true);
        assertEquals(group.getDescription().get(), "My Group");
    }

}
