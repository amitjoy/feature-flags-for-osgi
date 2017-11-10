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

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.amitinside.featureflags.example.ExampleFeatureFlag.MyConfig;

@Designate(ocd = MyConfig.class)
@Component(name = "ExampleFeatureFlag", immediate = true)
public final class ExampleFeatureFlag {

    private volatile MyConfig config;

    @ObjectClassDefinition(id = "feature.flag.example")
    @interface MyConfig {
        @AttributeDefinition(name = "osgi.feature.myfeature", description = "My Feature Description")
        boolean osgi_feature_myfeature() default true;
    }

    @Activate
    protected void activate(final MyConfig config) {
        modified(config);
    }

    @Modified
    protected void modified(final MyConfig config) {
        this.config = config;
        doStuff();
    }

    private void doStuff() {
        if (config.osgi_feature_myfeature()) {
            System.out.println("Example Feature Config >>Enabled<<");
            return;
        } else {
            System.out.println("Example Feature Config >>Disabled<<");
        }
    }
}
