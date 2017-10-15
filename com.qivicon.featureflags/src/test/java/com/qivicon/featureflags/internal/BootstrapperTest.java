/*******************************************************************************
 * Copyright (c) 2017 QIVICON
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Amit Kumar Mondal
 *
 *******************************************************************************/
package com.qivicon.featureflags.internal;

import static com.qivicon.featureflags.internal.TestHelper.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

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

import com.google.common.collect.Multimap;
import com.google.common.io.Resources;
import com.qivicon.featureflags.feature.Feature;
import com.qivicon.featureflags.feature.group.FeatureGroup;
import com.qivicon.featureflags.storage.StorageService;

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

        bootstrapper.activate(context);
        bootstrapper.setConfigurationAdmin(configurationAdmin);
        bootstrapper.setStorageService(storageService);

        final URL path = Resources.getResource("features.json");
        doReturn(path).when(bundle).getEntry("/features.json");
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

        bootstrapper.unsetConfigurationAdmin(configurationAdmin);
        bootstrapper.unsetStorageService(storageService);
    }

    @Test
    public void testBundleAddingWithServiceProperties() throws ClassNotFoundException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        bootstrapper.activate(context);
        bootstrapper.setConfigurationAdmin(configurationAdmin);
        bootstrapper.setStorageService(storageService);

        final URL path = Resources.getResource("featuresWithServiceProperties.json");
        doReturn(path).when(bundle).getEntry("/features.json");

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

        bootstrapper.unsetConfigurationAdmin(configurationAdmin);
        bootstrapper.unsetStorageService(storageService);
    }

    @Test
    public void testBundleAddingWhenResourceNotPresent() throws ClassNotFoundException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        bootstrapper.activate(context);
        bootstrapper.setConfigurationAdmin(configurationAdmin);
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

        bootstrapper.unsetConfigurationAdmin(configurationAdmin);
        bootstrapper.unsetStorageService(storageService);
    }

    @Test
    public void testBundleAddingButFeaturesNotSpecified() throws ClassNotFoundException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        bootstrapper.activate(context);
        bootstrapper.setConfigurationAdmin(configurationAdmin);
        bootstrapper.setStorageService(storageService);

        final URL path = Resources.getResource("featuresEmpty.json");
        doReturn(path).when(bundle).getEntry("/features.json");

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

        bootstrapper.unsetConfigurationAdmin(configurationAdmin);
        bootstrapper.unsetStorageService(storageService);
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
