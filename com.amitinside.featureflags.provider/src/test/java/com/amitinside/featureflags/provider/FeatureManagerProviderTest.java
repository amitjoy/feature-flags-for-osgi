/*******************************************************************************
 * Copyright (c) 2017-2018 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.service.metatype.ObjectClassDefinition.ALL;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.amitinside.featureflags.dto.FeatureDTO;

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

		when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
		when(metaTypeInfo.getPids()).thenReturn(pids);
		when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
		when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
		mockADWithoutDefaultValue();
		when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
		when(bundle.getState()).thenReturn(ACTIVE);
		when(bundle.getBundleContext()).thenReturn(bundleContext1);

		extender.addingBundle(bundle, bundleEvent);

		Thread.sleep(1000);
		FeatureDTO feature = manager.getFeatures().collect(Collectors.toList()).get(0);

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertFalse(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findFirst().get();

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertFalse(feature.isEnabled);

		feature = manager.getFeatures("myfeature").findAny().get();

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

		when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
		when(metaTypeInfo.getPids()).thenReturn(pids);
		when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
		when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
		mockADWithDefaultValue();
		when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
		when(bundle.getState()).thenReturn(ACTIVE);
		when(bundle.getBundleContext()).thenReturn(bundleContext1);

		extender.addingBundle(bundle, bundleEvent);

		Thread.sleep(1000);
		FeatureDTO feature = manager.getFeatures().collect(Collectors.toList()).get(0);

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findFirst().get();

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findAny().get();

		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		manager.unsetConfigurationAdmin(configurationAdmin);
		manager.unsetMetaTypeService(metaTypeService);
		manager.deactivate(bundleContext1);
	}

	@Test
	public void testGetFeaturesFromMetatypeXMLDescriptorWithoutName() throws Exception {
		final FeatureManagerProvider manager = new FeatureManagerProvider();

		manager.setConfigurationAdmin(configurationAdmin);
		manager.setMetaTypeService(metaTypeService);
		manager.activate(bundleContext1);

		final MetaTypeExtender extender = manager.getExtender();
		final String[] pids = new String[] { "a" };
		final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);

		when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
		when(metaTypeInfo.getPids()).thenReturn(pids);
		when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
		when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
		mockADWithoutName();
		when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
		when(bundle.getState()).thenReturn(ACTIVE);
		when(bundle.getBundleContext()).thenReturn(bundleContext1);

		extender.addingBundle(bundle, bundleEvent);

		Thread.sleep(1000);
		FeatureDTO feature = manager.getFeatures().collect(Collectors.toList()).get(0);

		assertEquals(FEATURE_ID, feature.name);
		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findFirst().get();

		assertEquals(FEATURE_ID, feature.name);
		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findAny().get();

		assertEquals(FEATURE_ID, feature.name);
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

		when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
		when(metaTypeInfo.getPids()).thenReturn(pids);
		when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
		when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
		mockADWithDefaultValueAndProperties();
		when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
		when(bundle.getState()).thenReturn(ACTIVE);
		when(bundle.getBundleContext()).thenReturn(bundleContext1);

		extender.addingBundle(bundle, bundleEvent);

		Thread.sleep(1000);
		FeatureDTO feature = manager.getFeatures().collect(Collectors.toList()).get(0);

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findFirst().get();

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findAny().get();

		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

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

		when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
		when(metaTypeInfo.getPids()).thenReturn(pids);
		when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
		when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
		mockADWithDefaultValue();
		when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
		when(bundle.getState()).thenReturn(ACTIVE);
		when(bundle.getBundleContext()).thenReturn(bundleContext1);
		when(ad.getID()).thenReturn("somethingElse");

		extender.addingBundle(bundle, bundleEvent);

		final List<FeatureDTO> list = manager.getFeatures().collect(Collectors.toList());
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

		when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
		when(metaTypeInfo.getPids()).thenReturn(pids);
		when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
		when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
		mockADWithoutDefaultValue();
		when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
		when(bundle.getState()).thenReturn(ACTIVE);
		when(bundle.getBundleContext()).thenReturn(bundleContext1);

		extender.addingBundle(bundle, bundleEvent);

		Thread.sleep(1000);
		FeatureDTO feature = manager.getFeatures().collect(Collectors.toList()).get(0);

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertFalse(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findFirst().get();

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertFalse(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findAny().get();

		assertEquals(FEATURE_DESC, feature.description);
		assertFalse(feature.isEnabled);

		final Map<String, Object> properties = new HashMap<>();
		properties.put("osgi.feature.myfeature", true);

		when(configurationAdmin.getConfiguration("a", "?")).thenReturn(configuration);
		when(configuration.getProperties()).thenReturn(new MapToDictionary(properties));

		final ConfigurationEvent configEvent = new ConfigurationEvent(reference, 1, null, "a");
		manager.configurationEvent(configEvent);

		final FeatureDTO updatedFeature = manager.getFeatures(FEATURE_ID).findAny().get();

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

		when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
		when(metaTypeInfo.getPids()).thenReturn(pids);
		when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
		when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
		mockADWithoutDefaultValue();
		when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
		when(bundle.getState()).thenReturn(ACTIVE);
		when(bundle.getBundleContext()).thenReturn(bundleContext1);

		extender.addingBundle(bundle, bundleEvent);

		Thread.sleep(1000);
		FeatureDTO feature = manager.getFeatures().collect(Collectors.toList()).get(0);

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertFalse(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findFirst().get();

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertFalse(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findAny().get();

		assertEquals(FEATURE_DESC, feature.description);
		assertFalse(feature.isEnabled);

		final ConfigurationEvent configEvent = new ConfigurationEvent(reference, 2, null, "a");
		manager.configurationEvent(configEvent);

		final List<FeatureDTO> newFeatures = manager.getFeatures().collect(Collectors.toList());

		assertTrue(newFeatures.isEmpty());
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

		when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
		when(metaTypeInfo.getPids()).thenReturn(pids);
		when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
		when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
		mockADWithoutDefaultValue();
		when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
		when(bundle.getState()).thenReturn(ACTIVE);
		when(bundle.getBundleContext()).thenReturn(bundleContext1);

		extender.addingBundle(bundle, bundleEvent);

		Thread.sleep(1000);
		FeatureDTO feature = manager.getFeatures().collect(Collectors.toList()).get(0);

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertFalse(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findFirst().get();

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertFalse(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findAny().get();

		assertEquals(FEATURE_DESC, feature.description);
		assertFalse(feature.isEnabled);

		when(configurationAdmin.getConfiguration("a", "?")).thenThrow(IOException.class);

		final ConfigurationEvent configEvent = new ConfigurationEvent(reference, 1, null, "a");
		manager.configurationEvent(configEvent);

		final FeatureDTO updatedFeature = manager.getFeatures(FEATURE_ID).findAny().get();

		assertFalse(updatedFeature.isEnabled);
	}

	@Test
	public void testUpdateFeature1() throws Exception {
		final FeatureManagerProvider manager = new FeatureManagerProvider();

		manager.setConfigurationAdmin(configurationAdmin);
		manager.setMetaTypeService(metaTypeService);
		manager.activate(bundleContext1);

		final MetaTypeExtender extender = manager.getExtender();
		final String[] pids = new String[] { "a" };
		final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);

		when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
		when(metaTypeInfo.getPids()).thenReturn(pids);
		when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
		when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
		mockADWithDefaultValue();
		when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
		when(bundle.getState()).thenReturn(ACTIVE);
		when(bundle.getBundleContext()).thenReturn(bundleContext1);
		when(configurationAdmin.getConfiguration("a", "?")).thenReturn(configuration);

		extender.addingBundle(bundle, bundleEvent);

		Thread.sleep(1000);
		FeatureDTO feature = manager.getFeatures().collect(Collectors.toList()).get(0);

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findFirst().get();

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findAny().get();

		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		try {
			manager.updateFeature(FEATURE_ID, false);
		} catch (final Exception e) {
			assertFalse(true);
		}

		manager.unsetConfigurationAdmin(configurationAdmin);
		manager.unsetMetaTypeService(metaTypeService);
		manager.deactivate(bundleContext1);
	}

	@Test
	public void testUpdateFeature2() throws Exception {
		final FeatureManagerProvider manager = new FeatureManagerProvider();

		manager.setConfigurationAdmin(configurationAdmin);
		manager.setMetaTypeService(metaTypeService);
		manager.activate(bundleContext1);

		final MetaTypeExtender extender = manager.getExtender();
		final String[] pids = new String[] { "a" };
		final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);

		when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
		when(metaTypeInfo.getPids()).thenReturn(pids);
		when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
		when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
		mockADWithDefaultValue();
		when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
		when(bundle.getState()).thenReturn(ACTIVE);
		when(bundle.getBundleContext()).thenReturn(bundleContext1);
		when(configurationAdmin.getConfiguration("a", "?")).thenReturn(null);

		extender.addingBundle(bundle, bundleEvent);

		Thread.sleep(1000);
		FeatureDTO feature = manager.getFeatures().collect(Collectors.toList()).get(0);

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findFirst().get();

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findAny().get();

		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		try {
			manager.updateFeature(FEATURE_ID, false);
		} catch (final Exception e) {
			assertFalse(true);
		}

		manager.unsetConfigurationAdmin(configurationAdmin);
		manager.unsetMetaTypeService(metaTypeService);
		manager.deactivate(bundleContext1);
	}

	@Test
	public void testUpdateFeature3() throws Exception {
		final FeatureManagerProvider manager = new FeatureManagerProvider();

		manager.setConfigurationAdmin(configurationAdmin);
		manager.setMetaTypeService(metaTypeService);
		manager.activate(bundleContext1);

		final MetaTypeExtender extender = manager.getExtender();
		final String[] pids = new String[] { "a" };
		final BundleEvent bundleEvent = new BundleEvent(BundleEvent.STARTED, bundle);

		when(metaTypeService.getMetaTypeInformation(bundle)).thenReturn(metaTypeInfo);
		when(metaTypeInfo.getPids()).thenReturn(pids);
		when(metaTypeInfo.getObjectClassDefinition("a", null)).thenReturn(ocd);
		when(ocd.getAttributeDefinitions(ALL)).thenReturn(new AttributeDefinition[] { ad });
		mockADWithDefaultValue();
		when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
		when(bundle.getState()).thenReturn(ACTIVE);
		when(bundle.getBundleContext()).thenReturn(bundleContext1);
		when(configurationAdmin.getConfiguration("a", "?")).thenThrow(IOException.class);

		extender.addingBundle(bundle, bundleEvent);

		Thread.sleep(1000);
		FeatureDTO feature = manager.getFeatures().collect(Collectors.toList()).get(0);

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findFirst().get();

		assertEquals(FEATURE_ID, feature.id);
		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		feature = manager.getFeatures(FEATURE_ID).findAny().get();

		assertEquals(FEATURE_DESC, feature.description);
		assertTrue(feature.isEnabled);

		try {
			manager.updateFeature(FEATURE_ID, false);
		} catch (final Exception e) {
			assertFalse(true);
		}

		manager.unsetConfigurationAdmin(configurationAdmin);
		manager.unsetMetaTypeService(metaTypeService);
		manager.deactivate(bundleContext1);
	}

	@Test
	public void testPreemptiveShutdown1() throws Exception {
		final FeatureManagerProvider manager = new FeatureManagerProvider();

		manager.setConfigurationAdmin(configurationAdmin);
		manager.setMetaTypeService(metaTypeService);
		manager.activate(bundleContext1);

		final MetaTypeExtender extender = manager.getExtender();
		extender.error(">>>>ERROR<<<<<", new RuntimeException());
	}

	@Test
	public void testPreemptiveShutdown2() throws Exception {
		final FeatureManagerProvider manager = new FeatureManagerProvider();

		manager.setConfigurationAdmin(configurationAdmin);
		manager.setMetaTypeService(metaTypeService);
		manager.activate(bundleContext1);

		final MetaTypeExtender extender = manager.getExtender();
		extender.warn(bundle, ">>>>WARNING<<<<<", new RuntimeException());
	}

	@Test(expected = NullPointerException.class)
	public void testNPEinGetFeatures() {
		final FeatureManagerProvider manager = new FeatureManagerProvider();
		manager.getFeatures(null);
	}

	@Test(expected = NullPointerException.class)
	public void testNPEinGetFeature() {
		final FeatureManagerProvider manager = new FeatureManagerProvider();
		manager.getFeatures(null);
	}

	@Test(expected = NullPointerException.class)
	public void testNPEinUpdateFeature() {
		final FeatureManagerProvider manager = new FeatureManagerProvider();
		manager.updateFeature(null, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIAEinGetFeatures() {
		final FeatureManagerProvider manager = new FeatureManagerProvider();
		manager.getFeatures("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIAEinUpdateFeature() {
		final FeatureManagerProvider manager = new FeatureManagerProvider();
		manager.updateFeature("", false);
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

	private void mockADWithoutName() {
		when(ad.getID()).thenReturn(FeatureManager.METATYPE_FEATURE_ID_PREFIX + FEATURE_ID);
		when(ad.getDescription()).thenReturn(FEATURE_DESC);
		when(ad.getDefaultValue()).thenReturn(new String[] { "true" });
	}

	private void mockADWithDefaultValueAndProperties() {
		when(ad.getID()).thenReturn(FeatureManager.METATYPE_FEATURE_ID_PREFIX + FEATURE_ID);
		when(ad.getDescription()).thenReturn(FEATURE_DESC);
		when(ad.getName()).thenReturn(FEATURE_NAME);
		when(ad.getDefaultValue()).thenReturn(new String[] { "true" });
	}

}
