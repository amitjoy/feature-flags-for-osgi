# Feature Flags for OSGi

This is an implementation of the Feature Toggles pattern for OSGi. Feature Toggles are a very common agile development practices in the context of continuous deployment and delivery. The basic idea is to associate a toggle with each new feature you are working on. This allows you to enable or disable these features at application runtime, even for individual users.

### Dependencies

This uses a small number of popular projects to work properly:

* slf4j
* Google Guava
* GSON

### Installation

Just install this bundle including all the dependencies it requires.

### Development

Want to contribute? Great!

#### Building for source

1. Add slf4j, Google Guava and GSON to your Eclipse IDE Installation's Target Platform
2. Import this project

### TODOs

 - Write Tests

### License

EPL-1.0
 
### Usage

1. Create a *features.json* in your bundle's root directory
2. The features must be specified in *features.json* in the following way

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
       "description": "My Feature 3"
       "enabled": false,
       "strategy": "MyStrategy2"
   },
 ]
```
3. In your DS Component, use FeatureService to check if the feature is enabled

```java
private FeatureService featureService;

public myMethod() {
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
4. Instead of providing *features.json*, you can also implement *Feature* interface and expose it as an OSGi service
5. The strategy must be privided by implementing *ActivationStrategy* interface and exposing as an OSGi service
