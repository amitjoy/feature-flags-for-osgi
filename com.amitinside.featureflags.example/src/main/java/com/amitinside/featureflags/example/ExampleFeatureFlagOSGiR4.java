/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
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
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.amitinside.featureflags.example.ExampleFeatureFlagOSGiR4.MyConfig;

@Designate(ocd = MyConfig.class)
@Component(name = "ExampleFeatureFlagOSGiR4", immediate = true)
public final class ExampleFeatureFlagOSGiR4 {

    private boolean isFeatureEnabled;

    @ObjectClassDefinition(id = "feature.flag.example2")
    @interface MyConfig {
        @AttributeDefinition(name = "osgi.feature.myfeature", description = "My Feature Description")
        boolean osgi_feature_myfeature() default true;
    }

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
            System.out.println("[R4] Example Feature is >>Enabled<<");
        } else {
            System.out.println("[R4] Example Feature is >>Disabled<<");
        }
    }
}
