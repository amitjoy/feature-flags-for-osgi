package com.amitinside.featureflags.example;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.amitinside.featureflags.example.ExampleFeatureFlag.MyConfig;

@Component
@Designate(ocd = MyConfig.class)
public final class ExampleFeatureFlag {

    private MyConfig config;

    @ObjectClassDefinition
    @interface MyConfig {
        @AttributeDefinition(name = "My First Feature", description = "My Feature Description")
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
            System.out.println("Example Feature is >>Enabled<<");
        } else {
            System.out.println("Example Feature is >>Disabled<<");
        }
    }
}
