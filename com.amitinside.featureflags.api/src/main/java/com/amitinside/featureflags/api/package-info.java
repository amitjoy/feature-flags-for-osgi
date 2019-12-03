/**
 * Provides API for managing feature flags instances
 *
 * <p>
 * Bundles wishing to use this package must list the package in the
 * Import-Package header of the bundle's manifest. This package has two types of
 * users: the consumers that use the API in this package and the providers that
 * implement the API in this package.
 *
 * <p>
 * Example import for consumers using the API in this package:
 * <p>
 * {@code  Import-Package: com.amitinside.featureflags.api;version="[1.0,2.0)"}
 * <p>
 * Example import for providers implementing the API in this package:
 * <p>
 * {@code  Import-Package: com.amitinside.featureflags.api;version="[1.0,1.1)"}
 *
 * @since 1.0
 */
@org.osgi.annotation.bundle.Export
@org.osgi.annotation.versioning.Version("1.0")
package com.amitinside.featureflags.api;