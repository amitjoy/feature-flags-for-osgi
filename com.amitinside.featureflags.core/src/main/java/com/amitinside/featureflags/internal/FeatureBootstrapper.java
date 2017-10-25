/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.internal;

import static com.amitinside.featureflags.ConfigurationEvent.Type.UPDATED;
import static com.google.common.base.Charsets.UTF_8;
import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.framework.Constants.SERVICE_PID;
import static org.osgi.service.component.annotations.ReferenceCardinality.OPTIONAL;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.ConfigurationEvent;
import com.amitinside.featureflags.Factory;
import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.listener.ConfigurationListener;
import com.amitinside.featureflags.storage.StorageService;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Resources;
import com.google.gson.Gson;

/**
 * {@link FeatureBootstrapper} is used to track all installed bundles. It looks for
 * {@code features.json} in the bundle and if found, it registers the specified features
 * as services whose factory PID is {@code com.amitinside.featureflags.feature} and it also
 * registers feature groups as OSGi service whose factory PID is
 * {@code com.amitinside.featureflags.feature.group}.
 * <br>
 * <br>
 * The features are specified in the resource file in the following way:
 *
 * <pre>
 * {
 *  "features":[
 *     {
 *        "name":"feature1",
 *        "description":"My Feature 1",
 *        "enabled":false,
 *        "groups":["MyFeatureGroup1"]
 *     },
 *     {
 *        "name":"feature2",
 *        "description":"My Feature 2",
 *        "strategy":"MyStrategy1"
 *     },
 *     {
 *        "name":"feature3",
 *        "description":"My Feature 3",
 *        "enabled":false,
 *        "groups":["MyFeatureGroup1", "MyFeatureGroup2"]
 *     },
 *     {
 *        "name":"feature4",
 *        "description":"My Feature 4",
 *        "enabled":false,
 *        "strategy":"MyStrategy2",
 *        "properties":{
 *           "p1":1,
 *           "p2":"test1",
 *           "p3":"test2"
 *        }
 *     }
 *  ],
 *  "groups":[
 *     {
 *        "name":"MyFeatureGroup1",
 *        "description":"My Favorite Group",
 *        "enabled":true,
 *        "strategy":"MyStrategy2"
 *     },
 *     {
 *        "name":"MyFeatureGroup2",
 *        "description":"I don't like this Group",
 *        "enabled":false,
 *        "properties":{
 *           "p4":1,
 *           "p5":"test1",
 *           "p6":"test2"
 *        }
 *     }
 *  ]
 * }
 * </pre>
 *
 * <b>N.B:</b> You can also add extra properties to your feature as shown in the above example.
 * These properties will be added as your feature's service properties. You can also create feature
 * groups by specifying groups in the JSON resource.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Component(service = FeatureBootstrapper.class, name = "FeatureBootstrapper", immediate = true)
public final class FeatureBootstrapper implements BundleTrackerCustomizer, ConfigurationListener {

    /** JSON Resource */
    public static final String RESOURCE = "/features.json";

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private BundleTracker bundleTracker;
    private FeatureService featureService;
    private StorageService storageService;
    private final Gson gson = new Gson();

    // configuration PIDs associated with the bundle instance that contains the features
    private final Multimap<Bundle, String> allFeatures = ArrayListMultimap.create();

    // configuration PIDs associated with the bundle instance that contains the feature groups
    private final Multimap<Bundle, String> allFeatureGroups = ArrayListMultimap.create();

    @Activate
    protected void activate(final BundleContext context) {
        bundleTracker = new BundleTracker(context, ACTIVE, this);
        bundleTracker.open();
        if (storageService == null) {
            storageService = new DefaultStorage();
        }
    }

    @Deactivate
    protected void deactivate(final BundleContext context) {
        if (bundleTracker != null) {
            bundleTracker.close();
            bundleTracker = null;
        }
    }

    /**
     * {@link FeatureService} service binding callback
     */
    @Reference
    protected void setFeatureService(final FeatureService featureService) {
        this.featureService = featureService;
    }

    /**
     * {@link FeatureService} service unbinding callback
     */
    protected void unsetFeatureService(final FeatureService featureService) {
        this.featureService = null;
    }

    /**
     * {@link StorageService} service binding callback
     */
    @Reference(cardinality = OPTIONAL)
    protected void setStorageService(final StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * {@link StorageService} service unbinding callback
     */
    protected void unsetStorageService(final StorageService storageService) {
        this.storageService = null;
    }

    @Override
    public Object addingBundle(final Bundle bundle, final BundleEvent event) {
        final Data data = getData(bundle);
        if (data != null) {
            addFeatures(bundle, data);
            addGroups(bundle, data);
        }
        return bundle;
    }

    @Override
    public void modifiedBundle(final Bundle bundle, final BundleEvent event, final Object object) {
        // do not remove any registered feature or feature group
    }

    @Override
    public void removedBundle(final Bundle bundle, final BundleEvent event, final Object object) {
        // do not remove any registered feature or feature group
    }

    @Override
    public void accept(final ConfigurationEvent event) {
        final String name = event.getReference().getName();
        final String servicePid = (String) event.getProperties().get(SERVICE_PID);
        if (event.getType() == UPDATED) {
            storageService.put(name, servicePid);
        } else {
            storageService.remove(name);
        }
    }

    private void addFeatures(final Bundle bundle, final Data data) {
        final List<Feature> features = data.getFeatures();
        for (final Feature feature : features) {
            final Optional<String> pid = registerFeature(feature);
            if (pid.isPresent()) {
                allFeatures.put(bundle, pid.get());
            }
        }
    }

    private void addGroups(final Bundle bundle, final Data data) {
        final List<Group> groups = data.getGroups();
        for (final Group group : groups) {
            final Optional<String> pid = registerFeatureGroup(group);
            if (pid.isPresent()) {
                allFeatureGroups.put(bundle, pid.get());
            }
        }
    }

    /**
     * Registers the specified feature properties as a configurable
     * {@link com.amitinside.featureflags.feature.Feature} service
     *
     * @return the registered service PID wrapped in {@link Optional}
     *         instance or empty {@link Optional}
     */
    private Optional<String> registerFeature(final Feature feature) {
        final String name = feature.getName();
        feature.getGroups();
        final Optional<String> value = storageService.get(name);
        if (value.isPresent()) {
            return Optional.empty();
        }
        //@formatter:off
        final Factory factory = Factory.make(name, c -> c.withDescription(feature.getDescription())
                                                         .withStrategy(feature.getStrategy())
                                                         .withGroups(feature.getGroups())
                                                         .withProperties(feature.getProperties())
                                                         .withEnabled(feature.isEnabled())
                                                         .build());
        //@formatter:on
        return featureService.createFeature(factory);
    }

    /**
     * Registers the specified feature group properties as a configurable
     * {@link com.amitinside.featureflags.feature.group.FeatureGroup} service
     *
     * @return the registered service PID wrapped in {@link Optional}
     *         instance or empty {@link Optional}
     */
    private Optional<String> registerFeatureGroup(final Group group) {
        final String name = group.getName();
        final Optional<String> value = storageService.get(name);
        if (value.isPresent()) {
            return Optional.empty();
        }
        //@formatter:off
        final Factory factory = Factory.make(name, c -> c.withDescription(group.getDescription())
                                                        .withStrategy(group.getStrategy())
                                                        .withProperties(group.getProperties())
                                                        .withEnabled(group.isEnabled())
                                                        .build());
        //@formatter:on
        return featureService.createGroup(factory);
    }

    /**
     * Retrieves the data specified in the bundle's {@code feature.json}
     * resource
     *
     * @param bundle the bundle to look in
     * @return the data or {@code null}
     */
    private Data getData(final Bundle bundle) {
        try {
            final URL featuresFileURL = bundle.getEntry(RESOURCE);
            if (featuresFileURL != null) {
                final String resource = Resources.toString(featuresFileURL, UTF_8);
                return gson.fromJson(resource, Data.class);
            }
        } catch (final IOException e) {
            logger.trace("Cannot retrieve feature JSON resource", e);
        }
        return null;
    }

    /**
     * Internal class used to represent JSON data
     */
    private static final class Data {
        private List<Feature> features;
        private List<Group> groups;

        public List<Feature> getFeatures() {
            return features;
        }

        public List<Group> getGroups() {
            return groups;
        }

    }

    /**
     * Internal class used to represent Feature JSON data
     */
    private static final class Feature {
        private String name;
        private String description;
        private String strategy;
        private List<String> groups;
        private boolean enabled;
        private Map<String, Object> properties;

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getStrategy() {
            return strategy;
        }

        public List<String> getGroups() {
            return groups;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    /**
     * Internal class used to represent Feature Group JSON data
     */
    private static final class Group {
        private String name;
        private String description;
        private String strategy;
        private boolean enabled;
        private Map<String, Object> properties;

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getStrategy() {
            return strategy;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }
    }

}
