package com.amitinside.featureflags.annotations;

import static com.amitinside.featureflags.api.FeatureManager.FEATURE_CAPABILITY_NAME;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;
import static org.osgi.namespace.extender.ExtenderNamespace.EXTENDER_NAMESPACE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.osgi.annotation.bundle.Requirement;

/**
 * This annotation can be used to require the Feature Flags extender
 * implementation. It can be used directly, or as a meta-annotation.
 *
 * @since 1.0
 */
@Documented
@Retention(CLASS)
@Target({ TYPE, PACKAGE })
@Requirement(namespace = EXTENDER_NAMESPACE, name = FEATURE_CAPABILITY_NAME, version = "1.0.0")
public @interface RequireFeatureFlags {
    // This is a marker annotation.
}