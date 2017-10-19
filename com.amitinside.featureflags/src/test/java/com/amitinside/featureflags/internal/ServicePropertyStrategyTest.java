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

public final class ServicePropertyStrategyTest {

    private Map<String, Object> strategyProperties;

    @Test
    public void testAllPropertiesAvailabilityOnActivate() {
        strategyProperties = Maps.newHashMap();
        strategyProperties.put("name", "strategy1");
        strategyProperties.put("description", "My Strategy");
        strategyProperties.put("property_key", "prop");
        strategyProperties.put("property_value", "val");

        final ServicePropertyActivationStrategy strategy = new ServicePropertyActivationStrategy();
        strategy.activate(strategyProperties);

        assertEquals(strategy.getName(), "strategy1");
        assertEquals(strategy.getDescription().get(), "My Strategy");
    }

    @Test
    public void testAllPropertiesAvailabilityOnUpdate() {
        strategyProperties = Maps.newHashMap();
        strategyProperties.put("name", "strategy1");
        strategyProperties.put("description", "My Strategy");
        strategyProperties.put("property_key", "prop");
        strategyProperties.put("property_value", "val");

        final ServicePropertyActivationStrategy strategy = new ServicePropertyActivationStrategy();
        strategy.updated(strategyProperties);

        assertEquals(strategy.getName(), "strategy1");
        assertEquals(strategy.getDescription().get(), "My Strategy");
    }

    @Test
    public void testDescriptionSetToNameIfMissing() {
        strategyProperties = Maps.newHashMap();
        strategyProperties.put("name", "strategy1");
        strategyProperties.put("description", "My Strategy");
        strategyProperties.put("property_key", "prop");
        strategyProperties.put("property_value", "val");

        final ServicePropertyActivationStrategy strategy = new ServicePropertyActivationStrategy();
        strategy.activate(strategyProperties);

        assertTrue(strategy.getDescription().isPresent());

        assertEquals(strategy.getName(), "strategy1");
        assertEquals(strategy.getDescription().get(), "My Strategy");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNameNull() {
        strategyProperties = Maps.newHashMap();
        strategyProperties.put("name", null);
        strategyProperties.put("description", "My Strategy");
        strategyProperties.put("property_key", "prop");
        strategyProperties.put("property_value", "val");

        final ServicePropertyActivationStrategy strategy = new ServicePropertyActivationStrategy();
        strategy.activate(strategyProperties);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNameEmpty() {
        strategyProperties = Maps.newHashMap();
        strategyProperties.put("name", "");
        strategyProperties.put("description", "My Strategy");
        strategyProperties.put("property_key", "prop");
        strategyProperties.put("property_value", "val");

        final ServicePropertyActivationStrategy strategy = new ServicePropertyActivationStrategy();
        strategy.activate(strategyProperties);
    }

    @Test
    public void testPropertyKeyNull() {
        strategyProperties = Maps.newHashMap();
        strategyProperties.put("name", "strategy1");
        strategyProperties.put("description", "My Strategy");
        strategyProperties.put("property_key", null);
        strategyProperties.put("property_value", "val");

        final ServicePropertyActivationStrategy strategy = new ServicePropertyActivationStrategy();
        strategy.activate(strategyProperties);

        assertFalse(strategy.isEnabled(null, null));
    }

    @Test
    public void testPropertyValueNull() {
        strategyProperties = Maps.newHashMap();
        strategyProperties.put("name", "strategy1");
        strategyProperties.put("description", "My Strategy");
        strategyProperties.put("property_key", "prop");
        strategyProperties.put("property_value", null);

        final ServicePropertyActivationStrategy strategy = new ServicePropertyActivationStrategy();
        strategy.activate(strategyProperties);

        assertFalse(strategy.isEnabled(null, null));
    }

    @Test
    public void testToString() {
        strategyProperties = Maps.newHashMap();
        strategyProperties.put("name", "strategy1");
        strategyProperties.put("description", "My Strategy");
        strategyProperties.put("property_key", "prop");
        strategyProperties.put("property_value", "val");

        final ServicePropertyActivationStrategy strategy = new ServicePropertyActivationStrategy();
        strategy.activate(strategyProperties);

        assertEquals(strategy.toString(),
                "ServicePropertyActivationStrategy{Name=strategy1, Description=My Strategy, Property Key=prop, Property Value=val}");
    }

}
