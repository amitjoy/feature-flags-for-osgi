<img width="206" alt="logo" src="https://user-images.githubusercontent.com/13380182/31521441-d679b224-afa9-11e7-960c-e643b7fc45e0.png">

## Why? [![start with what and why](https://img.shields.io/badge/start%20with-why%3F-brightgreen.svg?style=flat)](http://featureflags.io/feature-flags/)

This is an implementation of the Feature Toggles pattern (also known as Feature Flags) for OSGi Service Platform. Feature Flags (also known as Feature Toggles and Feature Controls) is a software development practice that facilitates the easy enablement and disablement of deployed functionalities. Besides, feature flags ease the management of the feature's entire lifecycle. These allow you to manage components and compartmentalize risk. We can also roll out the features to a specific group of users or exclude the group from accessing it, perform A/B test and much more. It’s also way to test how your features perform in the real world and not just in an artificial test environment. Therefore, feature toggle is a widespread agile development practice in the context of continuous deployment and delivery.

----------------------------------------------------------------

**Continuous Integration** ![Build Status](https://travis-ci.org/amitjoy/feature-flags-for-osgi.svg?branch=master)

**Static Code Analysis** [![Codacy Badge](https://api.codacy.com/project/badge/Grade/90918f9f84b64b14ac9ea1ed7f8ac041)](https://www.codacy.com/app/admin_62/feature-flags-for-osgi?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=amitjoy/feature-flags-osgi&amp;utm_campaign=Badge_Grade)

**Code Coverage** [![codecov](https://codecov.io/gh/amitjoy/feature-flags-for-osgi/branch/master/graph/badge.svg)](https://codecov.io/gh/amitjoy/feature-flags-for-osgi) [![Coverage Status](https://coveralls.io/repos/github/amitjoy/feature-flags-for-osgi/badge.svg?branch=master)](https://coveralls.io/github/amitjoy/feature-flags-for-osgi?branch=master)

**Javadoc** [![javadoc](http://javadoc-badge.appspot.com/com.github.michaelruocco/retriable.svg?label=javadoc)](http://amitjoy.github.io/feature-flags-for-osgi/)

----------------------------------------------------------------

### Requirements

1. Java 8+
2. OSGi R4+ (For Core API and Implementation)
3. OSGi R6+ (For REST Services and Web Console)

----------------------------------------------------------------

### Dependencies

This project comprises five bundles - 

1. `com.amitinside.featureflags.api` - The core feature flags API
2. `com.amitinside.featureflags.provider` - The core feature flags implementation
3. `com.amitinside.featureflags.rest` - REST Services to manage features, groups and strategies
4. `com.amitinside.featureflags.console` - Web console to manage features, groups and strategies
5. `com.amitinside.featureflags.example` - Example project showing how to use core feature flags in codebase

The core implementation bundle does require few open source libraries that are listed below.

1. SLF4J 1.7+ (MIT)
2. Google Guava 15+ (Apache 2.0)

As Test Dependencies, it uses the following test libraries:

1. JUnit 4.12 (EPL 1.0)
2. Google Compile Testing 0.12 (Apache 2.0)
3. Mockito Core 2.10 (MIT)

The bundle comprising REST services requires:

1. OSGi enRoute REST Provider (Implementation of proposed OSGi REST Specification)
2. OSGi enRoute DTOs (Implementation of proposed OSGi DTO Service) 

----------------------------------------------------------------

### Installation

1. `com.amitinside.featureflags.api`
2. `com.amitinside.featureflags.provider`
3. `com.amitinside.featureflags.rest`
4. `com.amitinside.featureflags.console`
5. `com.amitinside.featureflags.example`

You don't need to install all five bundles. To use feature flags in OSGi environment, you could only use the API and provider bundles.

-----------------------------------------------------------------

### Contribution [![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/amitjoy/feature-flags-osgi/issues)

Want to contribute? Great! Check out [Contribution Guide](https://github.com/amitjoy/feature-flags-osgi/blob/master/CONTRIBUTING.md)

-----------------------------------------------------------------

#### Project Import

**Import as Maven Project**

Import all the projects as Existing Maven Projects (`File -> Import -> Maven -> Existing Maven Projects`)

----------------------------------------------------------------

#### Building from Source

1. Run `mvn clean install -Dgpg.skip` in the project folder

----------------------------------------------------------------

#### Web Console

1. Checkout this project
2. Build using `mvn clean install -Dgpg.skip`
3. Run packaged Apache Felix OSGi framework. See [How-To](https://github.com/amitjoy/feature-flags-osgi/wiki/Feature-Flags-Web-Administration)
4. Install all the bundles to your OSGi runtime
5. Open browser and access `http://localhost:8080/featureflags/page/index.html`

----------------------------------------------------------------

### License

This project is licensed under EPL-1.0 [![License](http://img.shields.io/badge/license-EPL-blue.svg)](http://www.eclipse.org/legal/epl-v10.html)

----------------------------------------------------------------

### Usage

1. You can use `FeatureManager#createFeature(...)`, `FeatureManager#createGroup(...)` and `FeatureManager#createPropertyBasedStrategy(...)` to create features, groups and property based strategies.

2. In your DS Component, use `FeatureService` to check if the feature is enabled

```java

@Feature
private static final String MY_FEATURE = "Myfeature";

private FeatureManager featureManager;

public void myMethod() {
   if (featureManager.isFeatureEnabled(MY_FEATURE)) {
         // do this
   } else {
         // do something else
   }
}

@Reference
void setFeatureManager(final FeatureManager featureManager) {
   this.featureManager = featureManager;
}
    
void unsetFeatureManager(final FeatureManager featureManager) {
   this.featureManager = null;
}
```
6. You can also implement `Feature` interface and expose it as an OSGi service
7. The strategy must be provided by implementing `ActivationStrategy` interface and exposing as an OSGi service
8. You can also provide a feature group by implementing `FeatureGroup` interface and exposing as an OSGi service

--------------------------------------------------------------

If a strategy is provided for one or more features, the strategy will be used to determine which feature(s) will be active in the runtime. If you don't provide any strategy, the `enabled` property (`Feature#isEnabled()` method) will be used for enablement of the feature. That is, a strategy always overrides any value explicitly set to `enabled` flag.

Apart from this, you can also bundle multiple features into a specific group. Such feature group can even associate a strategy. If a
valid strategy has been associated to a feature group, the strategy will be used to determine the enablements of all the features that belong to this group and if not, the `enabled` property (`FeatureGroup#isEnabled()` method) will be used for enablement of this feature group and this will eventually determine the enablements of all the features belonging to this group.

**N.B:** A feature can also specify multiple feature groups to which it belongs. In such scenario, the active group would be responsible for the enablement of the feature.

The following flowchart shows the control flow for the determination of feature enablement:

![feature-flags](https://user-images.githubusercontent.com/13380182/32149859-65ab9eda-bd0b-11e7-9d63-c332c676f4d5.jpg)

----------------------------------------------------------------

**Strategies Included**:

1. **Service Property Activation Strategy**: This strategy is responsible for checking configured property key and value in the feature or feature group's OSGi service property.
2. **System Property Activation Strategy**: Likewise this strategy checks for specified property key and value in the system configured properties.

----------------------------------------------------------------

**Motivation of Feature Group**: Feature groups are primarily used to enable or disable multiple related features all-together. That's why enablement of any feature group is directly applied to the belonging features. Not all features should belong to feature group and hence a feature can optionally specify the feature group to which it belongs. Please note that use feature groups only if you have multiple features to group and whose enablements and disablements would happen together. If you do have various features whose enablements and disablements are not at all related, **do not specify any feature group** for those features. For more information, have a look at the flowchart as mentioned earlier.
