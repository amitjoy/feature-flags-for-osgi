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

import static com.amitinside.featureflags.Constants.*;
import static com.amitinside.featureflags.internal.FeatureBootstrapper.RESOURCE;
import static com.amitinside.featureflags.internal.TestHelper.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.BundleTracker;

import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.feature.group.FeatureGroup;
import com.amitinside.featureflags.storage.StorageService;
import com.google.common.collect.Multimap;
import com.google.common.io.Resources;

@SuppressWarnings({ "unchecked", "rawtypes" })
@RunWith(MockitoJUnitRunner.class)
public final class BootstrapperTest {

    @Mock
    private BundleContext context;
    @Mock
    private StorageService storageService;
    @Mock
    private Bundle bundle;
    @Mock
    private BundleEvent bundleEvent;
    @Mock
    private ServiceReference reference;
    @Mock
    private ConfigurationAdmin configurationAdmin;

    private FeatureBootstrapper bootstrapper;
    private FeatureManager manager;

    @Before
    public void init() {
        bootstrapper = new FeatureBootstrapper();
        manager = new FeatureManager();
    }

    @Test
    public void testStorageNonNull() throws ClassNotFoundException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        bootstrapper.activate(context);
        final Class<?> clazz = Class.forName(FeatureBootstrapper.class.getName());
        final Field storage = clazz.getDeclaredField("storageService");
        storage.setAccessible(true);
        final Object service = storage.get(bootstrapper);
        assertTrue(service != null);
        assertTrue(service instanceof DefaultStorage);
    }

    @Test
    public void testBundleTrackerNonNull() throws ClassNotFoundException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        bootstrapper.activate(context);
        final Class<?> clazz = Class.forName(FeatureBootstrapper.class.getName());
        final Field trackerField = clazz.getDeclaredField("bundleTracker");
        trackerField.setAccessible(true);
        Object tracker = trackerField.get(bootstrapper);
        assertTrue(tracker != null);
        assertTrue(tracker instanceof BundleTracker);

        bootstrapper.deactivate(context);
        tracker = trackerField.get(bootstrapper);
        assertTrue(tracker == null);
    }

    @Test
    public void testStorageNonNullWithCustomService() throws ClassNotFoundException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {
        bootstrapper.activate(context);
        bootstrapper.setStorageService(storageService);
        final Class<?> clazz = Class.forName(FeatureBootstrapper.class.getName());
        final Field storage = clazz.getDeclaredField("storageService");
        storage.setAccessible(true);
        final Object service = storage.get(bootstrapper);
        assertTrue(service != null);
        assertFalse(service instanceof DefaultStorage);
    }

    @Test
    public void testBundleAdding() throws ClassNotFoundException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        manager.setConfigurationAdmin(configurationAdmin);
        bootstrapper.activate(context);
        bootstrapper.setFeatureService(manager);
        bootstrapper.setStorageService(storageService);

        final URL path = Resources.getResource("features.json");
        doReturn(path).when(bundle).getEntry(RESOURCE);
        doReturn(Optional.empty()).when(storageService).get("my.feature");

        bootstrapper.addingBundle(bundle, bundleEvent);

        final Class<?> clazz = Class.forName(FeatureBootstrapper.class.getName());
        final Field allFeatures = clazz.getDeclaredField("allFeatures");
        allFeatures.setAccessible(true);
        final Multimap<Bundle, String> allFeatureInstances = (Multimap<Bundle, String>) allFeatures.get(bootstrapper);
        assertEquals(allFeatureInstances.size(), 1);

        final Field allGroups = clazz.getDeclaredField("allFeatureGroups");
        allGroups.setAccessible(true);
        allGroups.get(bootstrapper);
        final Multimap<Bundle, String> allGroupInstances = (Multimap<Bundle, String>) allGroups.get(bootstrapper);
        assertEquals(allGroupInstances.get(bundle).size(), 2);

        bootstrapper.unsetFeatureService(manager);
        bootstrapper.unsetStorageService(storageService);
    }

    @Test
    public void testBundleAddingWithServiceProperties() throws ClassNotFoundException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        manager.setConfigurationAdmin(configurationAdmin);
        bootstrapper.activate(context);
        bootstrapper.setFeatureService(manager);
        bootstrapper.setStorageService(storageService);

        final URL path = Resources.getResource("featuresWithServiceProperties.json");
        doReturn(path).when(bundle).getEntry(RESOURCE);

        bootstrapper.addingBundle(bundle, bundleEvent);

        final Class<?> clazz = Class.forName(FeatureBootstrapper.class.getName());
        final Field allFeatures = clazz.getDeclaredField("allFeatures");
        allFeatures.setAccessible(true);
        final Multimap<Bundle, String> allFeatureInstances = (Multimap<Bundle, String>) allFeatures.get(bootstrapper);
        assertEquals(allFeatureInstances.size(), 1);

        final Field allGroups = clazz.getDeclaredField("allFeatureGroups");
        allGroups.setAccessible(true);
        allGroups.get(bootstrapper);
        final Multimap<Bundle, String> allGroupInstances = (Multimap<Bundle, String>) allGroups.get(bootstrapper);
        assertEquals(allGroupInstances.get(bundle).size(), 2);

        bootstrapper.unsetFeatureService(manager);
        bootstrapper.unsetStorageService(storageService);
    }

    @Test
    public void testBundleAddingWhenResourceNotPresent() throws ClassNotFoundException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        manager.setConfigurationAdmin(configurationAdmin);
        bootstrapper.activate(context);
        bootstrapper.setFeatureService(manager);
        bootstrapper.setStorageService(storageService);

        bootstrapper.addingBundle(bundle, bundleEvent);

        final Class<?> clazz = Class.forName(FeatureBootstrapper.class.getName());
        final Field allFeatures = clazz.getDeclaredField("allFeatures");
        allFeatures.setAccessible(true);
        final Multimap<Bundle, String> allFeatureInstances = (Multimap<Bundle, String>) allFeatures.get(bootstrapper);
        assertEquals(allFeatureInstances.size(), 0);

        final Field allGroups = clazz.getDeclaredField("allFeatureGroups");
        allGroups.setAccessible(true);
        allGroups.get(bootstrapper);
        final Multimap<Bundle, String> allGroupInstances = (Multimap<Bundle, String>) allGroups.get(bootstrapper);
        assertEquals(allGroupInstances.get(bundle).size(), 0);

        bootstrapper.unsetFeatureService(manager);
        bootstrapper.unsetStorageService(storageService);
    }

    @Test
    public void testBundleAddingButFeaturesNotSpecified() throws ClassNotFoundException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        manager.setConfigurationAdmin(configurationAdmin);
        bootstrapper.activate(context);
        bootstrapper.setFeatureService(manager);
        bootstrapper.setStorageService(storageService);

        final URL path = Resources.getResource("featuresEmpty.json");
        doReturn(path).when(bundle).getEntry(RESOURCE);

        bootstrapper.addingBundle(bundle, bundleEvent);

        final Class<?> clazz = Class.forName(FeatureBootstrapper.class.getName());
        final Field allFeatures = clazz.getDeclaredField("allFeatures");
        allFeatures.setAccessible(true);
        final Multimap<Bundle, String> allFeatureInstances = (Multimap<Bundle, String>) allFeatures.get(bootstrapper);
        assertEquals(allFeatureInstances.size(), 0);

        final Field allGroups = clazz.getDeclaredField("allFeatureGroups");
        allGroups.setAccessible(true);
        allGroups.get(bootstrapper);
        final Multimap<Bundle, String> allGroupInstances = (Multimap<Bundle, String>) allGroups.get(bootstrapper);
        assertEquals(allGroupInstances.get(bundle).size(), 0);

        bootstrapper.unsetFeatureService(manager);
        bootstrapper.unsetStorageService(storageService);
    }

    @Test
    public void testBundleAddingWhenFeaturesAlreadyPresent() throws ClassNotFoundException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        manager.setConfigurationAdmin(configurationAdmin);
        bootstrapper.activate(context);
        bootstrapper.setFeatureService(manager);

        final StorageService storage = new DefaultStorage();
        storage.put("my.feature", "dummyValue1");
        storage.put("MyFeatureGroup1", "dummyValue2");
        storage.put("MyFeatureGroup2", "dummyValue3");
        bootstrapper.setStorageService(storage);

        final URL path = Resources.getResource("features.json");
        doReturn(path).when(bundle).getEntry(RESOURCE);

        bootstrapper.addingBundle(bundle, bundleEvent);

        final Class<?> clazz = Class.forName(FeatureBootstrapper.class.getName());
        final Field allFeatures = clazz.getDeclaredField("allFeatures");
        allFeatures.setAccessible(true);
        final Multimap<Bundle, String> allFeatureInstances = (Multimap<Bundle, String>) allFeatures.get(bootstrapper);
        assertEquals(allFeatureInstances.size(), 0);

        final Field allGroups = clazz.getDeclaredField("allFeatureGroups");
        allGroups.setAccessible(true);
        allGroups.get(bootstrapper);
        final Multimap<Bundle, String> allGroupInstances = (Multimap<Bundle, String>) allGroups.get(bootstrapper);
        assertEquals(allGroupInstances.get(bundle).size(), 0);

        bootstrapper.unsetFeatureService(manager);
        bootstrapper.unsetStorageService(storageService);
    }

    @Test
    public void testBundleAddingWithIOException1() throws ClassNotFoundException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException, IOException {
        manager.setConfigurationAdmin(configurationAdmin);
        bootstrapper.activate(context);
        bootstrapper.setFeatureService(manager);
        bootstrapper.setStorageService(storageService);

        final URL path = Resources.getResource("features.json");
        doReturn(path).when(bundle).getEntry(RESOURCE);
        doReturn(Optional.empty()).when(storageService).get("my.feature");
        doThrow(IOException.class).when(configurationAdmin).createFactoryConfiguration(FEATURE_FACTORY_PID);
        doThrow(IOException.class).when(configurationAdmin).createFactoryConfiguration(FEATURE_GROUP_FACTORY_PID);

        bootstrapper.addingBundle(bundle, bundleEvent);

        final Class<?> clazz = Class.forName(FeatureBootstrapper.class.getName());
        final Field allFeatures = clazz.getDeclaredField("allFeatures");
        allFeatures.setAccessible(true);
        final Multimap<Bundle, String> allFeatureInstances = (Multimap<Bundle, String>) allFeatures.get(bootstrapper);
        assertEquals(allFeatureInstances.size(), 0);

        final Field allGroups = clazz.getDeclaredField("allFeatureGroups");
        allGroups.setAccessible(true);
        allGroups.get(bootstrapper);
        final Multimap<Bundle, String> allGroupInstances = (Multimap<Bundle, String>) allGroups.get(bootstrapper);
        assertEquals(allGroupInstances.get(bundle).size(), 0);

        bootstrapper.unsetFeatureService(manager);
        bootstrapper.unsetStorageService(storageService);
    }

    @Test
    public void testBundleAddingWithIOException2() throws ClassNotFoundException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException, IOException {
        manager.setConfigurationAdmin(configurationAdmin);
        bootstrapper.activate(context);
        bootstrapper.setFeatureService(manager);
        bootstrapper.setStorageService(storageService);

        doThrow(IOException.class).when(bundle).getEntry(RESOURCE);

        bootstrapper.addingBundle(bundle, bundleEvent);

        final Class<?> clazz = Class.forName(FeatureBootstrapper.class.getName());
        final Field allFeatures = clazz.getDeclaredField("allFeatures");
        allFeatures.setAccessible(true);
        final Multimap<Bundle, String> allFeatureInstances = (Multimap<Bundle, String>) allFeatures.get(bootstrapper);
        assertEquals(allFeatureInstances.size(), 0);

        final Field allGroups = clazz.getDeclaredField("allFeatureGroups");
        allGroups.setAccessible(true);
        allGroups.get(bootstrapper);
        final Multimap<Bundle, String> allGroupInstances = (Multimap<Bundle, String>) allGroups.get(bootstrapper);
        assertEquals(allGroupInstances.get(bundle).size(), 0);

        bootstrapper.unsetFeatureService(manager);
        bootstrapper.unsetStorageService(storageService);
    }

    @Test
    public void testBundleAddingModifiedRemoved() throws ClassNotFoundException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        manager.setConfigurationAdmin(configurationAdmin);
        bootstrapper.activate(context);
        bootstrapper.setFeatureService(manager);
        bootstrapper.setStorageService(storageService);

        final URL path = Resources.getResource("features.json");
        doReturn(path).when(bundle).getEntry(RESOURCE);
        doReturn(Optional.empty()).when(storageService).get("my.feature");

        bootstrapper.addingBundle(bundle, bundleEvent);

        final Class<?> clazz = Class.forName(FeatureBootstrapper.class.getName());
        final Field allFeatures = clazz.getDeclaredField("allFeatures");
        allFeatures.setAccessible(true);
        Multimap<Bundle, String> allFeatureInstances = (Multimap<Bundle, String>) allFeatures.get(bootstrapper);
        assertEquals(allFeatureInstances.size(), 1);

        final Field allGroups = clazz.getDeclaredField("allFeatureGroups");
        allGroups.setAccessible(true);
        allGroups.get(bootstrapper);
        Multimap<Bundle, String> allGroupInstances = (Multimap<Bundle, String>) allGroups.get(bootstrapper);
        assertEquals(allGroupInstances.get(bundle).size(), 2);

        bootstrapper.unsetFeatureService(manager);
        bootstrapper.unsetStorageService(storageService);

        bootstrapper.modifiedBundle(bundle, bundleEvent, feature);
        allFeatureInstances = (Multimap<Bundle, String>) allFeatures.get(bootstrapper);
        allGroupInstances = (Multimap<Bundle, String>) allGroups.get(bootstrapper);

        assertEquals(allFeatureInstances.size(), 1);
        assertEquals(allGroupInstances.get(bundle).size(), 2);

        bootstrapper.removedBundle(bundle, bundleEvent, feature);
        allFeatureInstances = (Multimap<Bundle, String>) allFeatures.get(bootstrapper);
        allGroupInstances = (Multimap<Bundle, String>) allGroups.get(bootstrapper);

        assertEquals(allFeatureInstances.size(), 1);
        assertEquals(allGroupInstances.get(bundle).size(), 2);

    }

    @Test
    public void testConfigListenerForFeatureGroup() {
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, group);
        manager = new FeatureManager();
        configurationAdmin.addListener(manager);

        final StorageService storage = new DefaultStorage();
        bootstrapper.setStorageService(storage);

        doReturn(group).when(context).getService(reference);

        final Map<String, Object> props = createServiceProperties(2, 5, "group1");
        manager.bindFeatureGroup(group, props);
        manager.bindConfigurationListener(bootstrapper);
        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);
        manager.enableGroup("group1");

        assertEquals("group1", storage.get("group1").get());

        manager.unbindFeatureGroup(group, props);
        manager.unbindConfigurationListener(bootstrapper);
        manager.unsetConfigurationAdmin(configurationAdmin);

        bootstrapper.unsetStorageService(storage);
    }

}
