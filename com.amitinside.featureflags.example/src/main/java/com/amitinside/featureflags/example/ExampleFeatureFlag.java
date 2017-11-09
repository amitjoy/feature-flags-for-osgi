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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.amitinside.featureflags.example.ExampleFeatureFlag.MyConfig;
import com.amitinside.featureflags.util.Configurable;

@Designate(ocd = MyConfig.class)
@Component(name = "ExampleFeatureFlag", immediate = true)
public final class ExampleFeatureFlag {

    private MyConfig config;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @ObjectClassDefinition(id = "feature.flag.example")
    @interface MyConfig {
        @AttributeDefinition(description = "My Feature Description")
        boolean osgi_feature_myfeature() default true;
    }

    @Activate
    protected void activate(final Map<String, Object> properties) {
        modified(properties);
        doPeriodicStuff();
    }

    @Modified
    protected void modified(final Map<String, Object> properties) {
        config = Configurable.createConfigurable(MyConfig.class, properties);
    }

    private void doPeriodicStuff() {
        executor.scheduleWithFixedDelay(() -> {
            if (config.osgi_feature_myfeature()) {
                System.out.println("Example Feature Config >>Enabled<<");
                return;
            } else {
                System.out.println("Example Feature Config >>Disabled<<");
            }
        }, 0, 15, TimeUnit.SECONDS);
    }

}
