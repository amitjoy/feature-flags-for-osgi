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

import static com.amitinside.featureflags.internal.Config.*;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.amitinside.featureflags.util.ConfigHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class ConfigTest {

    @Test
    public void testProperties() {
        final Map<String, Object> featureProperties = Maps.newHashMap();
        featureProperties.put("name", "feature1");
        featureProperties.put("description", "My Feature");
        featureProperties.put("enabled", true);
        featureProperties.put("group", "group1");
        featureProperties.put("strategy", "strategy1");

        final Map<Config, Object> parsedProps = ConfigHelper.parseProperties(featureProperties);

        assertEquals(parsedProps.get(NAME), "feature1");
        assertEquals(parsedProps.get(DESCRIPTION), "My Feature");
        assertEquals(parsedProps.get(ENABLED), true);
        assertEquals(parsedProps.get(GROUP), "group1");
        assertEquals(parsedProps.get(STRATEGY), "strategy1");
    }

    @Test(expected = InvocationTargetException.class)
    public void testObjectConstruction() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        final Class<?> clazz = Class.forName(ConfigHelper.class.getName());
        final List<Constructor<?>> constrctors = Lists.newArrayList(clazz.getDeclaredConstructors());
        if (!constrctors.isEmpty()) {
            final Constructor<?> constructor = constrctors.get(0);
            constructor.setAccessible(true);
            constructor.newInstance((Object[]) null);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgument1() {
        ConfigHelper.parseProperties(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgument2() {
        getIfPresent(null);
    }

    @Test
    public void testInvalidArgument() {
        assertFalse(getIfPresent("").isPresent());
    }

    @Test
    public void testGetValue() {
        final Config config = ENABLED;
        assertEquals(config.value(), "enabled");
    }

}
