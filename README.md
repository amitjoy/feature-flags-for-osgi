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
2. OSGi R6+

----------------------------------------------------------------

### Dependencies

This project comprises four bundles - 

1. `com.amitinside.featureflags.api` - The core feature flags API
2. `com.amitinside.featureflags.provider` - The core feature flags implementation
3. `com.amitinside.featureflags.rest` - REST Services to manage features
4. `com.amitinside.featureflags.example` - Example project showing how to use core feature flags in codebase

The core implementation bundle does require few open source libraries that are listed below.

1. SLF4J 1.7+ (MIT)
2. Google Guava 15+ (Apache 2.0)

As Test Dependencies, it uses the following test libraries:

1. JUnit 4.12 (EPL 1.0)
3. Mockito Core 2.10 (MIT)

The bundle comprising REST services requires:

1. OSGi enRoute REST Provider (Implementation of proposed OSGi REST Specification)
2. OSGi enRoute DTOs (Implementation of proposed OSGi DTO Service) 

----------------------------------------------------------------

### Installation

1. `com.amitinside.featureflags.api`
2. `com.amitinside.featureflags.provider`
3. `com.amitinside.featureflags.rest`
4. `com.amitinside.featureflags.example`

You don't need to install all four bundles. To use feature flags in OSGi environment, you could only use the API and provider bundles.

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

### License

This project is licensed under EPL-1.0 [![License](http://img.shields.io/badge/license-EPL-blue.svg)](http://www.eclipse.org/legal/epl-v10.html)

----------------------------------------------------------------

### Usage

1. In your DS Component, add an attribute definition to the existing object class definition

```java
@ObjectClassDefinition(id = "feature.flag.example")
@interface MyConfig {
   @AttributeDefinition(description = "My Feature Description")
   boolean osgi_feature_myfeature() default true;
}
```
You can check the property value as specified in the atrtribute definition. For more information, have a look at the example project.

--------------------------------------------------------------
