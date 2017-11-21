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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.service.metatype.ObjectClassDefinition.ALL;

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
import org.osgi.service.cm.ConfigurationAdmin;
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

    @Test
    public void testGetFeaturesFromMetatypeXMLDescriptor() throws Exception {
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
        mockAD();
        when(bundle.getHeaders()).thenReturn(new MapToDictionary(headers));
        when(bundleContext1.getBundle(0)).thenReturn(systemBundle);
        when(bundle.getState()).thenReturn(ACTIVE);
        when(bundle.getBundleContext()).thenReturn(bundleContext1);

        extender.addingBundle(bundle, bundleEvent);

        manager.getConfigurations().collect(Collectors.toList());
        final ConfigurationDTO config = manager.getConfigurations().findAny().get();
        FeatureDTO feature = config.features.get(0);

        assertEquals("a", config.pid);
        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertEquals(false, feature.isEnabled);

        feature = manager.getFeatures("a").findFirst().get();

        assertEquals(FEATURE_ID, feature.id);
        assertEquals(FEATURE_DESC, feature.description);
        assertEquals(false, feature.isEnabled);

        feature = manager.getFeature("a", FEATURE_ID).get();

        assertEquals(FEATURE_DESC, feature.description);
        assertEquals(false, feature.isEnabled);
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
    public void testIAEinGetFeature() {
        final FeatureManagerProvider manager = new FeatureManagerProvider();
        manager.getFeature("", "");
    }

    private void mockAD() {
        when(ad.getID()).thenReturn(FeatureManager.METATYPE_FEATURE_ID_PREFIX + FEATURE_ID);
        when(ad.getDescription()).thenReturn(FEATURE_DESC);
        when(ad.getName()).thenReturn(FEATURE_NAME);
    }

}
