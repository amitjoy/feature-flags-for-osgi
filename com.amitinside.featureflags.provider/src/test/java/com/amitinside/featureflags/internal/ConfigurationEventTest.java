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

import static com.amitinside.featureflags.internal.TestHelper.*;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.amitinside.featureflags.ConfigurationEvent;
import com.amitinside.featureflags.ConfigurationEvent.Type;
import com.amitinside.featureflags.feature.group.FeatureGroup;

public final class ConfigurationEventTest {

    @Test
    public void testEventProperties() {
        final FeatureGroup group = createFeatureGroupCustom("group1", "My Group 1", false, "strategy1");
        final Map<String, Object> props = createServiceProperties(3, 6, "pid2");
        final ConfigurationEvent event = new ConfigurationEvent(Type.UPDATED, group, props);

        assertEquals(event.getType(), Type.UPDATED);
        assertEquals(event.getReference(), group);
        assertEquals(event.getProperties(), props);
    }

    @Test(expected = NullPointerException.class)
    public void testInitializationWhenTypeNull() {
        final FeatureGroup group = createFeatureGroupCustom("group1", "My Group 1", false, "strategy1");
        final Map<String, Object> props = createServiceProperties(3, 6, "pid2");
        final ConfigurationEvent event = new ConfigurationEvent(null, group, props);

        assertEquals(event.getReference(), group);
        assertEquals(event.getProperties(), props);
    }

    @Test(expected = NullPointerException.class)
    public void testInitializationWhenGroupNull() {
        final Map<String, Object> props = createServiceProperties(3, 6, "pid2");
        final ConfigurationEvent event = new ConfigurationEvent(Type.UPDATED, null, props);

        assertEquals(event.getType(), Type.UPDATED);
        assertEquals(event.getProperties(), props);
    }

    @Test(expected = NullPointerException.class)
    public void testInitializationWhenPropertiesNull() {
        final FeatureGroup group = createFeatureGroupCustom("group1", "My Group 1", false, "strategy1");
        final ConfigurationEvent event = new ConfigurationEvent(Type.DELETED, group, null);

        assertEquals(event.getType(), Type.DELETED);
        assertEquals(event.getReference(), group);
    }

}
