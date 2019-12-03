<img width="206" alt="logo" src="https://user-images.githubusercontent.com/13380182/31521441-d679b224-afa9-11e7-960c-e643b7fc45e0.png">

## Why? [![start with what and why](https://img.shields.io/badge/start%20with-why%3F-brightgreen.svg?style=flat)](http://featureflags.io/feature-flags/)

This is an implementation of the Feature Toggles pattern (also known as Feature Flags) for OSGi Service Platform. Feature Flags (also known as Feature Toggles and Feature Controls) is a software development practice that facilitates the easy enablement and disablement of deployed functionalities. Besides, feature flags ease the management of the feature's entire lifecycle. These allow you to manage components and compartmentalise risk. We can also roll out the features to a specific group of users or exclude the group from accessing it, perform A/B test and much more. It’s also the way to test how your features function in the real world and not just in an artificial test environment. Therefore, feature toggle is a widespread agile development practice in the context of continuous deployment and delivery.

--------------------------------------------------------------------------------------------------------

**Continuous Integration** ![Build Status](https://travis-ci.org/amitjoy/feature-flags-for-osgi.svg?branch=master)

**Static Code Analysis** [![Codacy Badge](https://api.codacy.com/project/badge/Grade/90918f9f84b64b14ac9ea1ed7f8ac041)](https://www.codacy.com/app/admin_62/feature-flags-for-osgi?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=amitjoy/feature-flags-osgi&amp;utm_campaign=Badge_Grade) [![BCH compliance](https://bettercodehub.com/edge/badge/amitjoy/feature-flags-for-osgi?branch=master)](https://bettercodehub.com/)

**Code Coverage** [![codecov](https://codecov.io/gh/amitjoy/feature-flags-for-osgi/branch/master/graph/badge.svg)](https://codecov.io/gh/amitjoy/feature-flags-for-osgi) [![Coverage Status](https://coveralls.io/repos/github/amitjoy/feature-flags-for-osgi/badge.svg?branch=master)](https://coveralls.io/github/amitjoy/feature-flags-for-osgi?branch=master)

**Javadoc** [![javadoc](http://javadoc-badge.appspot.com/com.tomgibara/github.svg?label=javadoc)](http://amitjoy.github.io/feature-flags-for-osgi/)

--------------------------------------------------------------------------------------------------------

### Requirements

1. Java 8+
2. OSGi R4.3+

--------------------------------------------------------------------------------------------------------

### Dependencies

This project comprises four bundles - 

1. `com.amitinside.featureflags.api` - The core feature flags API
2. `com.amitinside.featureflags.provider` - The core feature flags implementation
3. `com.amitinside.featureflags.example` - Example project showing how to use core feature flags in codebase

As test dependencies, the following test libraries are used:

1. JUnit 4.12
3. Mockito Core 2.10

--------------------------------------------------------------------------------------------------------

### Installation

To use feature flags in OSGi environment, you only need to install `com.amitinside.featureflags.provider`.

-------------------------------------------------------------------------------------------------------

### Contribution [![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/amitjoy/feature-flags-osgi/issues)

Want to contribute? Great! Check out [Contribution Guide](https://github.com/amitjoy/feature-flags-osgi/blob/master/CONTRIBUTING.md)

-------------------------------------------------------------------------------------------------------

#### Project Import

**Import as Eclipse Projects**

1. Install bndtools
2. Import all the projects (`File -> Import -> General -> Existing Projects into Workspace`)

--------------------------------------------------------------------------------------------------------

#### Building from Source

Run `./gradlew clean build` in the project root directory
------------------------------------------------------------------------------------------------

### License

This project is licensed under Apache License [![License](http://img.shields.io/badge/license-Apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

--------------------------------------------------------------------------------------------------------

### Usage

1. In your DS Component, add an attribute definition to the existing or new object class definition.

```java
@ObjectClassDefinition
@interface MyConfig {
      @AttributeDefinition(name = "My First Feature", description = "My Feature Description")
      boolean osgi_feature_myfeature() default true;
}
```

or provide a metatype XML with the required configuration in your bundle's `OSGI-INF/metatype` directory

```xml
<?xml version="1.0" encoding="UTF-8"?>
<MetaData xmlns="http://www.osgi.org/xmlns/metatype/v1.2.0" localization="en_us">
    <OCD id="ExampleFeatureFlagOCD" 
         name="My Feature Configuration">

        <AD id="osgi.feature.myfeature"
            name="My First Feature"
            description="My Feature Description"
            type="Boolean"
            cardinality="0"
            required="true"
            default="true">
        </AD>

    </OCD>
    
    <Designate pid="ExampleFeatureFlagOSGiR4WithXML">
        <Object ocdref="ExampleFeatureFlagOCD"/>
    </Designate>
</MetaData>
```

3. The primary contract of using feature flags in your codebase is to introduce boolean attribute definitions to existing or new object class definitions in metatype. The IDs of the attribute definitions must be **osgi.feature.X** where X is the name of your feature. And don't forget to add the aforementioned requirement capability to your manifest.

##### The primary benefit of this approach is that developers can use feature flags without having any dependency to any external API.

For more information, have a look at the [example project](https://github.com/amitjoy/feature-flags-for-osgi/tree/master/com.amitinside.featureflags.example/src/main/java/com/amitinside/featureflags/example).

---------------------------------------------------------------------------------------------------------
