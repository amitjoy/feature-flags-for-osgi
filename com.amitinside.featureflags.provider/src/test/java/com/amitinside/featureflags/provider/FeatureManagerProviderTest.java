/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.provider;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.service.metatype.ObjectClassDefinition.ALL;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.felix.utils.collections.MapToDictionary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;

import com.amitinside.featureflags.FeatureManager;
import com.amitinside.featureflags.dto.ConfigurationDTO;
import com.amitinside.featureflags.dto.FeatureDTO;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public final class FeatureManagerProviderTest {

    private static final String FEATURE_NAME = "My Feature";
    private static final String FEATURE_DESC = "My First Feature Description";
    private static final String FEATURE_ID = "myfeature";
    @Mock
    private BundleContext bundleContext1;
    @Mock
    private ConfigurationAdmin configurationAdmin;
    @Mock
    private MetaTypeService metaTypeService;
    @Mock
    private Bundle bundle;
    @Mock
    private Bundle systemBundle;
    @Mock
    private BundleContext bundleContext2;
    @Mock
    private MetaTypeInformation metaTypeInfo;
    @Mock
    private ObjectClassDefinition ocd;
    @Mock
    private AttributeDefinition ad;
    @Mock
    private Configuration configuration;
    @Mock
    private ServiceReference reference;

    @Test
    public void testGetFeaturesFromMetatypeXMLDescriptorWithoutDefaultValue() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        final MetaTypeExtender extender = manager.getExtender();
        final String[] pids = new String[] { "a" };
        final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);
        final Map<String, String> headers = ImmutableMap.<String, String> builder()
                .put("Require-Capability", "osgi.extender;filter:=\"(osgi.extender=osgi.feature)\"").build();

        when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
        when(metaTypeInfo.getPids()).thenReturn(pids);
        when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
        when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
        mockADWithoutDefaultValue();
        when(bundle.getHeaders()).thenReturn(new MapToDictionary(headers));
        when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
        when(bundle.getState()).thenReturn(ACTIVE);
        when(bundle.getBundleContext()).thenReturn(bundleContext1);

        extender.addingBundle(bundle, bundleEvent);

        Thread.sleep(1000);
        final ConfigurationDTO config = manager.getConfigurations().collect(Collectors.toList()).get(0);
        FeatureDTO feature = config.features.get(0);

        assertEquals("a", config.pid);
        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertFalse(feature.isEnabled);

        feature = manager.getFeatures("a").findFirst().get();

        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertFalse(feature.isEnabled);

        feature = manager.getFeature("a", FEATURE_ID).get();

        assertEquals(FEATURE_DESC, feature.description);
        assertFalse(feature.isEnabled);

        extender.removedBundle(bundle, bundleEvent, null);
    }

    @Test
    public void testGetFeaturesFromMetatypeXMLDescriptorWithDefaultValue() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        final MetaTypeExtender extender = manager.getExtender();
        final String[] pids = new String[] { "a" };
        final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);
        final Map<String, String> headers = ImmutableMap.<String, String> builder()
                .put("Require-Capability", "osgi.extender;filter:=\"(osgi.extender=osgi.feature)\"").build();

        when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
        when(metaTypeInfo.getPids()).thenReturn(pids);
        when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
        when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
        mockADWithDefaultValue();
        when(bundle.getHeaders()).thenReturn(new MapToDictionary(headers));
        when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
        when(bundle.getState()).thenReturn(ACTIVE);
        when(bundle.getBundleContext()).thenReturn(bundleContext1);

        extender.addingBundle(bundle, bundleEvent);

        Thread.sleep(1000);
        final ConfigurationDTO config = manager.getConfigurations().findAny().get();
        FeatureDTO feature = config.features.get(0);

        assertEquals("a", config.pid);
        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertTrue(feature.isEnabled);

        feature = manager.getFeatures("a").findFirst().get();

        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertTrue(feature.isEnabled);

        feature = manager.getFeature("a", FEATURE_ID).get();

        assertEquals(FEATURE_DESC, feature.description);
        assertTrue(feature.isEnabled);

        manager.unsetConfigurationAdmin(configurationAdmin);
        manager.unsetMetaTypeService(metaTypeService);
        manager.deactivate(bundleContext1);
    }

    @Test
    public void testGetFeaturesFromMetatypeXMLDescriptorWithDefaultValueButOnlyValueProperties() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        final MetaTypeExtender extender = manager.getExtender();
        final String[] pids = new String[] { "a" };
        final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);
        final Map<String, String> headers = ImmutableMap.<String, String> builder()
                .put("Require-Capability", "osgi.extender;filter:=\"(osgi.extender=osgi.feature)\"").build();

        when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
        when(metaTypeInfo.getPids()).thenReturn(pids);
        when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
        when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
        mockADWithDefaultValueButOnlyValueProperties();
        when(bundle.getHeaders()).thenReturn(new MapToDictionary(headers));
        when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
        when(bundle.getState()).thenReturn(ACTIVE);
        when(bundle.getBundleContext()).thenReturn(bundleContext1);

        extender.addingBundle(bundle, bundleEvent);

        Thread.sleep(1000);
        final ConfigurationDTO config = manager.getConfigurations().findAny().get();
        FeatureDTO feature = config.features.get(0);

        assertEquals("a", config.pid);
        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertTrue(feature.isEnabled);
        assertEquals(null, feature.properties);

        feature = manager.getFeatures("a").findFirst().get();

        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertTrue(feature.isEnabled);

        feature = manager.getFeature("a", FEATURE_ID).get();

        assertEquals(FEATURE_DESC, feature.description);
        assertTrue(feature.isEnabled);

        manager.unsetConfigurationAdmin(configurationAdmin);
        manager.unsetMetaTypeService(metaTypeService);
        manager.deactivate(bundleContext1);
    }

    @Test
    public void testGetFeaturesFromMetatypeXMLDescriptorWithDefaultValueAndProperties() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        final MetaTypeExtender extender = manager.getExtender();
        final String[] pids = new String[] { "a" };
        final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);
        final Map<String, String> headers = ImmutableMap.<String, String> builder()
                .put("Require-Capability", "osgi.extender;filter:=\"(osgi.extender=osgi.feature)\"").build();

        when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
        when(metaTypeInfo.getPids()).thenReturn(pids);
        when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
        when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
        mockADWithDefaultValueAndProperties();
        when(bundle.getHeaders()).thenReturn(new MapToDictionary(headers));
        when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
        when(bundle.getState()).thenReturn(ACTIVE);
        when(bundle.getBundleContext()).thenReturn(bundleContext1);

        extender.addingBundle(bundle, bundleEvent);

        Thread.sleep(1000);
        final ConfigurationDTO config = manager.getConfiguration("a").get();
        FeatureDTO feature = config.features.get(0);

        assertEquals("a", config.pid);
        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertTrue(feature.isEnabled);
        assertEquals("{property=value}", feature.properties.toString());

        feature = manager.getFeatures("a").findFirst().get();

        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertTrue(feature.isEnabled);

        feature = manager.getFeature("a", FEATURE_ID).get();

        assertEquals(FEATURE_DESC, feature.description);
        assertTrue(feature.isEnabled);

        manager.unsetConfigurationAdmin(configurationAdmin);
        manager.unsetMetaTypeService(metaTypeService);
        manager.deactivate(bundleContext1);
    }

    @Test
    public void testGetFeaturesFromMetatypeXMLDescriptorWithoutCapability() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        final MetaTypeExtender extender = manager.getExtender();
        final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);

        when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
        when(bundle.getState()).thenReturn(ACTIVE);
        when(bundle.getBundleContext()).thenReturn(bundleContext1);

        extender.addingBundle(bundle, bundleEvent);

        Thread.sleep(1000);
        final List<ConfigurationDTO> configs = manager.getConfigurations().collect(Collectors.toList());
        assertTrue(configs.isEmpty());

        manager.unsetConfigurationAdmin(configurationAdmin);
        manager.unsetMetaTypeService(metaTypeService);
        manager.deactivate(bundleContext1);
    }

    @Test
    public void testGetFeaturesFromMetatypeXMLDescriptorWithCapabilityButWrongNamespace() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        final MetaTypeExtender extender = manager.getExtender();
        final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);
        final Map<String, String> headers = ImmutableMap.<String, String> builder()
                .put("Require-Capability", "osgi.extender1;filter:=\"(osgi.extender=osgi.feature)\"").build();

        when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
        when(bundle.getState()).thenReturn(ACTIVE);
        when(bundle.getBundleContext()).thenReturn(bundleContext1);
        when(bundle.getHeaders()).thenReturn(new MapToDictionary(headers));

        extender.addingBundle(bundle, bundleEvent);

        Thread.sleep(1000);
        final List<ConfigurationDTO> configs = manager.getConfigurations().collect(Collectors.toList());
        assertTrue(configs.isEmpty());

        manager.unsetConfigurationAdmin(configurationAdmin);
        manager.unsetMetaTypeService(metaTypeService);
        manager.deactivate(bundleContext1);
    }

    @Test
    public void testGetFeaturesFromMetatypeXMLDescriptorWithCapabilityButWrongFilter() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        final MetaTypeExtender extender = manager.getExtender();
        final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);
        final Map<String, String> headers = ImmutableMap.<String, String> builder()
                .put("Require-Capability", "osgi.extender;filter:=\"(osgi.extender=osgi.feature1)\"").build();

        when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
        when(bundle.getState()).thenReturn(ACTIVE);
        when(bundle.getBundleContext()).thenReturn(bundleContext1);
        when(bundle.getHeaders()).thenReturn(new MapToDictionary(headers));

        extender.addingBundle(bundle, bundleEvent);

        Thread.sleep(1000);
        final List<ConfigurationDTO> configs = manager.getConfigurations().collect(Collectors.toList());
        assertEquals(true, configs.isEmpty());

        manager.unsetConfigurationAdmin(configurationAdmin);
        manager.unsetMetaTypeService(metaTypeService);
        manager.deactivate(bundleContext1);
    }

    @Test
    public void testGetFeaturesFromMetatypeXMLDescriptorWithoutAnySpecifiedFeatures() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        final MetaTypeExtender extender = manager.getExtender();
        final String[] pids = new String[] { "a" };
        final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);
        final Map<String, String> headers = ImmutableMap.<String, String> builder()
                .put("Require-Capability", "osgi.extender;filter:=\"(osgi.extender=osgi.feature)\"").build();

        when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
        when(metaTypeInfo.getPids()).thenReturn(pids);
        when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
        when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
        mockADWithDefaultValue();
        when(bundle.getHeaders()).thenReturn(new MapToDictionary(headers));
        when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
        when(bundle.getState()).thenReturn(ACTIVE);
        when(bundle.getBundleContext()).thenReturn(bundleContext1);
        when(ad.getID()).thenReturn("somethingElse");

        extender.addingBundle(bundle, bundleEvent);

        final List<ConfigurationDTO> list = manager.getConfigurations().collect(Collectors.toList());
        assertTrue(list.isEmpty());
    }

    @Test
    public void testConfigurationEventUpdated() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        final MetaTypeExtender extender = manager.getExtender();
        final String[] pids = new String[] { "a" };
        final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);
        final Map<String, String> headers = ImmutableMap.<String, String> builder()
                .put("Require-Capability", "osgi.extender;filter:=\"(osgi.extender=osgi.feature)\"").build();

        when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
        when(metaTypeInfo.getPids()).thenReturn(pids);
        when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
        when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
        mockADWithoutDefaultValue();
        when(bundle.getHeaders()).thenReturn(new MapToDictionary(headers));
        when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
        when(bundle.getState()).thenReturn(ACTIVE);
        when(bundle.getBundleContext()).thenReturn(bundleContext1);

        extender.addingBundle(bundle, bundleEvent);

        Thread.sleep(1000);
        final ConfigurationDTO config = manager.getConfigurations().collect(Collectors.toList()).get(0);
        FeatureDTO feature = config.features.get(0);

        assertEquals("a", config.pid);
        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertFalse(feature.isEnabled);

        feature = manager.getFeatures("a").findFirst().get();

        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertFalse(feature.isEnabled);

        feature = manager.getFeature("a", FEATURE_ID).get();

        assertEquals(FEATURE_DESC, feature.description);
        assertFalse(feature.isEnabled);

        final Map<String, Object> properties = ImmutableMap.<String, Object> builder()
                .put("osgi.feature.myfeature", true).build();

        when(configurationAdmin.getConfiguration("a", "?")).thenReturn(configuration);
        when(configuration.getProperties()).thenReturn(new MapToDictionary(properties));

        final ConfigurationEvent configEvent = new ConfigurationEvent(reference, 1, null, "a");
        manager.configurationEvent(configEvent);

        final ConfigurationDTO newConfig = manager.getConfigurations().collect(Collectors.toList()).get(0);
        final FeatureDTO updatedFeature = newConfig.features.get(0);

        assertTrue(updatedFeature.isEnabled);
    }

    @Test
    public void testConfigurationEventDeleted() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        final MetaTypeExtender extender = manager.getExtender();
        final String[] pids = new String[] { "a" };
        final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);
        final Map<String, String> headers = ImmutableMap.<String, String> builder()
                .put("Require-Capability", "osgi.extender;filter:=\"(osgi.extender=osgi.feature)\"").build();

        when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
        when(metaTypeInfo.getPids()).thenReturn(pids);
        when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
        when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
        mockADWithoutDefaultValue();
        when(bundle.getHeaders()).thenReturn(new MapToDictionary(headers));
        when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
        when(bundle.getState()).thenReturn(ACTIVE);
        when(bundle.getBundleContext()).thenReturn(bundleContext1);

        extender.addingBundle(bundle, bundleEvent);

        Thread.sleep(1000);
        final ConfigurationDTO config = manager.getConfigurations().collect(Collectors.toList()).get(0);
        FeatureDTO feature = config.features.get(0);

        assertEquals("a", config.pid);
        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertFalse(feature.isEnabled);

        feature = manager.getFeatures("a").findFirst().get();

        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertFalse(feature.isEnabled);

        feature = manager.getFeature("a", FEATURE_ID).get();

        assertEquals(FEATURE_DESC, feature.description);
        assertFalse(feature.isEnabled);

        final Map<String, Object> properties = ImmutableMap.<String, Object> builder()
                .put("osgi.feature.myfeature", true).build();

        when(configurationAdmin.getConfiguration("a", "?")).thenReturn(configuration);
        when(configuration.getProperties()).thenReturn(new MapToDictionary(properties));

        final ConfigurationEvent configEvent = new ConfigurationEvent(reference, 2, null, "a");
        manager.configurationEvent(configEvent);

        final List<ConfigurationDTO> newConfigs = manager.getConfigurations().collect(Collectors.toList());

        assertTrue(newConfigs.isEmpty());
    }

    @Test
    public void testConfigurationEventUpdatedButIOException() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        final MetaTypeExtender extender = manager.getExtender();
        final String[] pids = new String[] { "a" };
        final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);
        final Map<String, String> headers = ImmutableMap.<String, String> builder()
                .put("Require-Capability", "osgi.extender;filter:=\"(osgi.extender=osgi.feature)\"").build();

        when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
        when(metaTypeInfo.getPids()).thenReturn(pids);
        when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
        when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
        mockADWithoutDefaultValue();
        when(bundle.getHeaders()).thenReturn(new MapToDictionary(headers));
        when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
        when(bundle.getState()).thenReturn(ACTIVE);
        when(bundle.getBundleContext()).thenReturn(bundleContext1);

        extender.addingBundle(bundle, bundleEvent);

        Thread.sleep(1000);
        final ConfigurationDTO config = manager.getConfigurations().collect(Collectors.toList()).get(0);
        FeatureDTO feature = config.features.get(0);

        assertEquals("a", config.pid);
        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertFalse(feature.isEnabled);

        feature = manager.getFeatures("a").findFirst().get();

        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertFalse(feature.isEnabled);

        feature = manager.getFeature("a", FEATURE_ID).get();

        assertEquals(FEATURE_DESC, feature.description);
        assertFalse(feature.isEnabled);

        when(configurationAdmin.getConfiguration("a", "?")).thenThrow(IOException.class);

        final ConfigurationEvent configEvent = new ConfigurationEvent(reference, 1, null, "a");
        manager.configurationEvent(configEvent);

        final ConfigurationDTO newConfig = manager.getConfigurations().collect(Collectors.toList()).get(0);
        final FeatureDTO updatedFeature = newConfig.features.get(0);

        assertFalse(updatedFeature.isEnabled);
    }

    @Test
    public void testUpdateFeatureWithoutException1() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        when(configurationAdmin.getConfiguration("a", "?")).thenReturn(configuration);

        final CompletableFuture<Void> future = manager.updateFeature("a", "myfeature", true);
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    public void testUpdateFeatureWithoutException2() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        when(configurationAdmin.getConfiguration("a", "?")).thenReturn(null);

        final CompletableFuture<Void> future = manager.updateFeature("a", "myfeature", true);
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    public void testUpdateFeatureWithException() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        doThrow(new IOException()).when(configurationAdmin).getConfiguration("a", "?");

        final CompletableFuture<Void> future = manager.updateFeature("a", "myfeature", true);
        assertNotNull(future);
    }

    @Test
    public void testToConfigurationDTO() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        final FeatureManagerProvider manager = new FeatureManagerProvider();
        final Method method = manager.getClass().getDeclaredMethod("toConfigurationDTO", String.class);
        method.setAccessible(true);
        final Object dto = method.invoke(manager, "aaa");
        assertNull(dto);
    }

    @Test
    public void testPreemptiveShutdown() throws Exception {
        final FeatureManagerProvider manager = new FeatureManagerProvider();

        manager.setConfigurationAdmin(configurationAdmin);
        manager.setMetaTypeService(metaTypeService);
        manager.activate(bundleContext1);

        final MetaTypeExtender extender = manager.getExtender();
        extender.error(">>>>ERROR<<<<<", new RuntimeException());
    }

    @Test(expected = NullPointerException.class)
    public void testNPEinGetFeatures() {
        final FeatureManagerProvider manager = new FeatureManagerProvider();
        manager.getFeatures(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNPEinGetConfiguration() {
        final FeatureManagerProvider manager = new FeatureManagerProvider();
        manager.getConfiguration(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNPEinGetFeature() {
        final FeatureManagerProvider manager = new FeatureManagerProvider();
        manager.getFeature(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNPEinUpdateFeature() {
        final FeatureManagerProvider manager = new FeatureManagerProvider();
        manager.updateFeature(null, null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIAEinGetFeatures() {
        final FeatureManagerProvider manager = new FeatureManagerProvider();
        manager.getFeatures("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIAEinGetConfiguration() {
        final FeatureManagerProvider manager = new FeatureManagerProvider();
        manager.getConfiguration("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIAEinGetFeature1() {
        final FeatureManagerProvider manager = new FeatureManagerProvider();
        manager.getFeature("", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIAEinGetFeature2() {
        final FeatureManagerProvider manager = new FeatureManagerProvider();
        manager.getFeature("a", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIAEinUpdateFeature1() {
        final FeatureManagerProvider manager = new FeatureManagerProvider();
        manager.updateFeature("", "", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIAEinUpdateFeature2() {
        final FeatureManagerProvider manager = new FeatureManagerProvider();
        manager.updateFeature("a", "", false);
    }

    private void mockADWithoutDefaultValue() {
        when(ad.getID()).thenReturn(FeatureManager.METATYPE_FEATURE_ID_PREFIX + FEATURE_ID);
        when(ad.getDescription()).thenReturn(FEATURE_DESC);
        when(ad.getName()).thenReturn(FEATURE_NAME);
    }

    private void mockADWithDefaultValue() {
        when(ad.getID()).thenReturn(FeatureManager.METATYPE_FEATURE_ID_PREFIX + FEATURE_ID);
        when(ad.getDescription()).thenReturn(FEATURE_DESC);
        when(ad.getName()).thenReturn(FEATURE_NAME);
        when(ad.getDefaultValue()).thenReturn(new String[] { "true" });
    }

    private void mockADWithDefaultValueAndProperties() {
        when(ad.getID()).thenReturn(FeatureManager.METATYPE_FEATURE_ID_PREFIX + FEATURE_ID);
        when(ad.getDescription()).thenReturn(FEATURE_DESC);
        when(ad.getName()).thenReturn(FEATURE_NAME);
        when(ad.getDefaultValue()).thenReturn(new String[] { "true" });
        when(ad.getOptionLabels()).thenReturn(new String[] { "property" });
        when(ad.getOptionValues()).thenReturn(new String[] { "value" });
    }

    private void mockADWithDefaultValueButOnlyValueProperties() {
        when(ad.getID()).thenReturn(FeatureManager.METATYPE_FEATURE_ID_PREFIX + FEATURE_ID);
        when(ad.getDescription()).thenReturn(FEATURE_DESC);
        when(ad.getName()).thenReturn(FEATURE_NAME);
        when(ad.getDefaultValue()).thenReturn(new String[] { "true" });
        when(ad.getOptionLabels()).thenReturn(new String[] { "property1", "property2" });
        when(ad.getOptionValues()).thenReturn(new String[] { "value" });
    }

}
