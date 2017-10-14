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

import static com.qivicon.featureflags.internal.Config.*;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.qivicon.featureflags.internal.Config;
import com.qivicon.featureflags.util.ConfigHelper;

public final class ConfigTest {

    private Map<String, Object> featureProperties;

    @Test
    public void testProperties() {
        featureProperties = Maps.newHashMap();
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

}
