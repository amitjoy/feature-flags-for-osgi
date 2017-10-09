<img width="666" alt="logo" src="https://user-images.githubusercontent.com/13380182/31361797-71ae770e-ad55-11e7-902c-a9ebf4c1fa89.png">

## Why? [![start with what and why](https://img.shields.io/badge/start%20with-why%3F-brightgreen.svg?style=flat)](http://featureflags.io/feature-flags/)

This is an implementation of the Feature Toggles pattern for OSGi. Feature Toggles are a very common agile development practices in the context of continuous deployment and delivery. The basic idea is to associate a toggle with each new feature you are working on. This allows you to enable or disable these features at application runtime, even for individual users.

---

“A feature toggle, (also feature switch, feature flag, feature flipper, conditional feature, etc.) is a technique in software development that attempts to provide an alternative to maintaining multiple source-code branches (known as feature branches).  Continuous release and continuous deployment provide developers with rapid feedback about their coding. This requires the integration of their code changes as early as possible. Feature branches introduce a bypass to this process. Feature toggles bring developers back to the track, but the execution paths of their features are still “dead” if a toggle is “off”. But the effort is low to enable the new execution paths just by setting a toggle to on.”

---

### Dependencies

This uses a small number of popular projects to work properly:

* SLF4J
* Google Guava
* GSON

### Installation

Just install this bundle including all the dependencies it requires.

### Contribution [![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/amitjoy/feature-flags-osgi/issues)

Want to contribute? Great! Check out [Contribution Guide](https://github.com/amitjoy/feature-flags-osgi/blob/master/CONTRIBUTING.md)

#### Building for source

1. Add `SLF4J`, `Google Guava` and `GSON` to your Eclipse IDE Installation's Target Platform
2. Import this project

### License

EPL-1.0
 
### Usage

1. Create a `features.json` in your bundle's root directory
2. The features must be specified in `features.json` in the following way

```json
[
   {
       "name": "feature1",
       "description": "My Feature 1",
       "enabled": false
   },
   {
       "name": "feature2",
       "description": "My Feature 2",
       "strategy": "MyStrategy1"
   },
   {
       "name": "feature3",
       "description": "My Feature 3",
       "enabled": false,
       "strategy": "MyStrategy2",
       "properties": {
           "p1": 1,
           "p2": "test",
           "p3": [1, 2, 3, 4]
       }
   },
 ]
```
3. This will create `Feature` service instances that will be configured with OSGi configuration whose factory PID is `com.amitinside.featureflags.feature`. You can add extra properties to your feature as shown in the last feature example. These properties will be added as your feature's service properties.
4. In your DS Component, use `FeatureService` interface to check if the feature is enabled

```java
private FeatureService featureService;

public void myMethod() {
 if (featureService.isEnabled("feature2")) {
    // do this
 } else {
   // do something else
 }
}

@Reference
void setFeatureService(FeatureService featureService) {
  this.featureService = featureService;
}
    
void unsetFeatureService(FeatureService featureService) {
  this.featureService = null;
}
```
5. Instead of providing `features.json`, you can also implement `Feature` interface and expose it as an OSGi service
6. The strategy must be privided by implementing `ActivationStrategy` interface and exposing as an OSGi service

----------------------- ------------------------------------

If a strategy is provided for one or more features, the strategy will be used to determine which feature(s) will be active in the runtime. If you don't provide any strategy, the `enabled` (`Feature#isEnabled(Boolean)` method) flag will be used for enablement of the feature. That is, a strategy always overrides any value explicitly set to `enabled` flag.

`Examples of strategy`: `IP Based Strategy` in which based on specific IP Addresses of some features would be enabled or `Time Based Strategy` in which some group of features are enabled at a certain time of a day.

----------------------------------------------------------------
