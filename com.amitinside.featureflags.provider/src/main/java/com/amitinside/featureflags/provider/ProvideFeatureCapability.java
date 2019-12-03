package com.amitinside.featureflags.provider;

import static com.amitinside.featureflags.api.FeatureManager.FEATURE_CAPABILITY_NAME;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;

import org.osgi.annotation.bundle.Capability;

@Retention(CLASS)
@Capability(namespace = "osgi.extender", name = FEATURE_CAPABILITY_NAME, version = "1.0")
public @interface ProvideFeatureCapability {
}
