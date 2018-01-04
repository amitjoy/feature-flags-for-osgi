/*******************************************************************************
 * Copyright (c) 2017-2018 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.example;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(name = "ExampleFeatureFlagOSGiR4WithXML", immediate = true)
public final class ExampleFeatureFlagOSGiR4WithXML {

    private boolean isFeatureEnabled;

    @Activate
    protected void activate(final Map<String, Object> properties) {
        modified(properties);
    }

    @Modified
    protected void modified(final Map<String, Object> properties) {
        final Object value = properties.get("osgi.feature.myfeature");
        if (value instanceof Boolean) {
            isFeatureEnabled = Boolean.valueOf(value.toString());
        }
        doStuff();
    }

    private void doStuff() {
        if (isFeatureEnabled) {
            System.out.println("[R4] Example Feature with XML is >>Enabled<<");
        } else {
            System.out.println("[R4] Example Feature with XML is >>Disabled<<");
        }
    }
}
