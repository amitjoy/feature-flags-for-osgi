<img width="206" alt="logo" src="https://user-images.githubusercontent.com/13380182/31521441-d679b224-afa9-11e7-960c-e643b7fc45e0.png">

## Why? [![start with what and why](https://img.shields.io/badge/start%20with-why%3F-brightgreen.svg?style=flat)](http://featureflags.io/feature-flags/)

This is an implementation of the Feature Toggles pattern (also known as Feature Flags) for OSGi. Feature Toggles are a very common agile development practices in the context of continuous deployment and delivery. The basic idea is to associate a toggle with each new feature you are working on. This allows you to enable or disable these features at application runtime, even for individual users.

------------------------------------------------

“A feature toggle, (also feature switch, feature flag, feature flipper, conditional feature, etc.) is a technique in software development that attempts to provide an alternative to maintaining multiple source-code branches (known as feature branches).  Continuous release and continuous deployment provide developers with rapid feedback about their coding. This requires the integration of their code changes as early as possible. Feature branches introduce a bypass to this process. Feature toggles bring developers back to the track, but the execution paths of their features are still “dead” if a toggle is “off”. But the effort is low to enable the new execution paths just by setting a toggle to on.”

-------------------------------------------------

**Continuous Integration** ![Build Status](https://travis-ci.org/amitjoy/feature-flags-osgi.svg?branch=master)

**Static Code Analysis** [![Codacy Badge](https://api.codacy.com/project/badge/Grade/90918f9f84b64b14ac9ea1ed7f8ac041)](https://www.codacy.com/app/admin_62/feature-flags-osgi?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=amitjoy/feature-flags-osgi&amp;utm_campaign=Badge_Grade)

**Code Coverage** [![codecov](https://codecov.io/gh/amitjoy/feature-flags-osgi/branch/master/graph/badge.svg)](https://codecov.io/gh/amitjoy/feature-flags-osgi)

**Javadoc** [![javadoc](http://javadoc-badge.appspot.com/com.github.michaelruocco/template-populator.svg?label=javadoc)](http://amitjoy.github.io/feature-flags-osgi/)

### Dependencies

This requires a small number of dependencies to work properly:

* Java 8+
* OSGi R4+
* SLF4J 1.7.2+
* Google Guava 15+
* GSON 2.2.5+

### Installation

Just install this bundle including all the dependencies it requires.

### Contribution [![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/amitjoy/feature-flags-osgi/issues)

Want to contribute? Great! Check out [Contribution Guide](https://github.com/amitjoy/feature-flags-osgi/blob/master/CONTRIBUTING.md)

#### Project Import

**Import as Eclipse Project**

1. Add `SLF4J`, `Google Guava` and `GSON` to your Eclipse IDE Installation's Target Platform
2. Import this project as Existing Project (`File -> Import -> General -> Existing Project into Workspace`)

**Import as Maven Project**

1. You can also import the project as Existing Maven Project (`File -> Import -> Maven -> Existing Maven Projects`)

#### Building from Source

1. Run `mvn clean install -Dgpg.skip` in the project folder

### License

This project is licensed under EPL-1.0

### Usage

1. Create a `features.json` in your bundle's root directory
2. The features can be specified in `features.json` in the following way

```json
{
  "features":[
     {
        "name":"feature1",
        "description":"My Feature 1",
        "enabled":false,
        "group":"MyFeatureGroup1"
     },
     {
        "name":"feature2",
        "description":"My Feature 2",
        "strategy":"MyStrategy1"
     },
     {
        "name":"feature3",
        "description":"My Feature 3",
        "enabled":false,
        "group":"MyFeatureGroup1"
     },
     {
        "name":"feature4",
        "description":"My Feature 4",
        "enabled":false,
        "strategy":"MyStrategy2",
        "properties":{
           "p1":1,
           "p2":"test1",
           "p3":"test2"
        }
     }
  ],
  "groups":[
     {
        "name":"MyFeatureGroup1",
        "description":"My Favorite Group",
        "enabled":true,
        "strategy":"MyStrategy2"
     },
     {
        "name":"MyFeatureGroup2",
        "description":"I don't like this Group",
        "enabled":false
     }
  ]
}
```
3. This will create `Feature` service instance(s) that will be configured with OSGi configuration whose factory PID is `com.qivicon.featureflags.feature`. You can add extra properties to your feature as shown in the last feature example. These properties will be added as your feature's service properties. You can also create feature groups by specifying groups in the JSON resource. If you specify groups in JSON resource, `FeatureGroup` service instance(s) will be created and configured with this provided configuration whose factory PID will be `com.qivicon.featureflags.feature.group`.

4. In your DS Component, use `FeatureService` to check if the feature is enabled

```java
private FeatureService featureService;

public void myMethod() {
   if (featureService.isFeatureEnabled("feature2")) {
         // do this
   } else {
         // do something else
   }
}

@Reference
void setFeatureService(final FeatureService featureService) {
   this.featureService = featureService;
}
    
void unsetFeatureService(final FeatureService featureService) {
   this.featureService = null;
}
```
5. Instead of providing `features.json`, you can also implement `Feature` interface and expose it as an OSGi service
6. The strategy must be privided by implementing `ActivationStrategy` interface and exposing as an OSGi service
7. You can also provide a feature group by implementing `FeatureGroup` interface and exposing as an OSGi service

--------------------------------------------------------------

If a strategy is provided for one or more features, the strategy will be used to determine which feature(s) will be active in the runtime. If you don't provide any strategy, the `enabled` property (`Feature#isEnabled()` method) will be used for enablement of the feature. That is, a strategy always overrides any value explicitly set to `enabled` flag.

Apart from this, you can also bundle multiple features into a specific group. Such feature group can also associate a strategy. If a
valid strategy has been associated to a feature group, the strategy will be used to determine the enablements of all the features that belong to this group and if not, the `enabled` property (`FeatureGroup#isEnabled()` method) will be used for enablement of this feature group and this will eventually determine the enablements of all the features belonging to this group.

**N.B** A feature can also specify multiple feature groups to which it belongs. In such scenario, the active group would be responsible for the enablement of the feature.

The following flowchart shows the control flow for the determination of feature enablement:

![feature-flags](https://user-images.githubusercontent.com/13380182/31471988-37e71132-aeec-11e7-8f14-45230c69b713.png)

**Examples of strategies**: `IP Based Strategy` by which some features would be enabled based on specific IP Addresses or `Time Based Strategy` by which a group of features are enabled at a certain time of a day.

**Motivation of Feature Group**: Feature groups are primarily used to enable or disable multiple related features all-together. That's why enablement of any feature group is directly applied to the belonging features. Not all features should belong to feature group and hence a feature can optionally specify the feature group to which it belongs. Please note that use feature groups only if you have multiple features to group together and whose enablements and disablements would happen together. If you do have multiple features whose enablements and disablements are not at all related, **do not specify any feature group** for those features. For more information, have a look at the aforementioned flowchart.

----------------------------------------------------------------
