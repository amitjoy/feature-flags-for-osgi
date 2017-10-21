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
import static com.amitinside.featureflags.internal.TestHelper.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;

import com.amitinside.featureflags.ConfigurationEvent;
import com.amitinside.featureflags.ConfigurationEvent.Type;
import com.amitinside.featureflags.Factory;
import com.amitinside.featureflags.Strategizable;
import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.feature.group.FeatureGroup;
import com.amitinside.featureflags.listener.ConfigurationListener;
import com.amitinside.featureflags.strategy.ActivationStrategy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings({ "unchecked", "rawtypes" })
@RunWith(MockitoJUnitRunner.class)
public final class FeatureServiceTest {

    private FeatureManager manager;

    @Mock
    private BundleContext context;
    @Mock
    private ServiceReference reference;
    @Mock
    private Strategizable strategizable;
    @Mock
    private ConfigurationAdmin configurationAdmin;

    @Before
    public void init() {
        manager = createFeatureManagerWithCM();
    }

    @Test
    public void testGetFeatures() {
        final Feature feature1 = createFeature("feature1", "My Feature 1", true, "group1", "strategy1");
        final Map<String, Object> props1 = createServiceProperties(2, 5, "pid1");
        manager.bindFeature(feature1, props1);

        final Feature feature2 = createFeature("feature2", "My Feature 2", true, "group2", "strategy2");
        final Map<String, Object> props2 = createServiceProperties(3, 6, "pid2");
        manager.bindFeature(feature2, props2);

        assertEquals(manager.getFeatures().count(), 2);

        manager.unbindFeature(feature1, props1);
        manager.unbindFeature(feature2, props2);

        assertEquals(manager.getFeatures().count(), 0);
    }

    @Test
    public void testGetFeatureGroups() {
        final FeatureGroup group1 = createFeatureGroup("group1", "My Group 1", true, "strategy1");
        final Map<String, Object> props1 = createServiceProperties(2, 5, "pid1");
        manager.bindFeatureGroup(group1, props1);

        final FeatureGroup group2 = createFeatureGroup("group2", "My Group 2", true, "strategy2");
        final Map<String, Object> props2 = createServiceProperties(3, 6, "pid2");
        manager.bindFeatureGroup(group2, props2);

        assertEquals(manager.getGroups().count(), 2);

        manager.unbindFeatureGroup(group1, props1);
        manager.unbindFeatureGroup(group2, props2);

        assertEquals(manager.getGroups().count(), 0);
    }

    @Test
    public void testGetActivationStrategies() {
        final ActivationStrategy strategy1 = createStrategy("strategy1", true, "My Strategy 1");
        final ActivationStrategy strategy2 = createStrategy("strategy2", false, "My Strategy 2");

        final Map<String, Object> props1 = createServiceProperties(2, 5, "pid1");
        final Map<String, Object> props2 = createServiceProperties(3, 6, "pid2");
        manager.bindStrategy(strategy1, props1);
        manager.bindStrategy(strategy2, props2);

        assertEquals(manager.getStrategies().count(), 2);

        manager.unbindStrategy(strategy1, props1);
        manager.unbindStrategy(strategy2, props2);

        assertEquals(manager.getStrategies().count(), 0);
    }

    @Test
    public void testGetFeatureByName() {
        final Feature feature1 = createFeature("feature1", "My Feature 1", true, "group1", "strategy1");
        manager.bindFeature(feature1, createServiceProperties(2, 5, "pid1"));

        final Feature feature2 = createFeature("feature2", "My Feature 2", true, "group2", "strategy2");
        manager.bindFeature(feature2, createServiceProperties(3, 6, "pid2"));

        assertEquals(manager.getFeature("feature1").get(), feature1);
    }

    @Test
    public void testGetFeatureGroupByName() {
        final FeatureGroup group1 = createFeatureGroup("group1", "My Group 1", true, "strategy1");
        manager.bindFeatureGroup(group1, createServiceProperties(2, 5, "pid1"));

        final FeatureGroup group2 = createFeatureGroup("group2", "My Group 2", true, "strategy2");
        manager.bindFeatureGroup(group2, createServiceProperties(3, 6, "pid2"));

        assertEquals(manager.getGroup("group1").get(), group1);
    }

    @Test
    public void testGetStrategyByName() {
        final ActivationStrategy strategy1 = createStrategy("strategy1", true, "My Strategy 1");
        manager.bindStrategy(strategy1, createServiceProperties(2, 5, "pid1"));

        final ActivationStrategy strategy2 = createStrategy("strategy2", true, "My Strategy 2");
        manager.bindStrategy(strategy2, createServiceProperties(3, 6, "pid2"));

        assertEquals(manager.getStrategy("strategy1").get(), strategy1);
    }

    @Test
    public void testGetFeaturesByStrategy() {
        final Feature feature1 = createFeature("feature1", "My Feature 1", true, "group1", "strategy1");
        final Feature feature2 = createFeature("feature2", "My Feature 2", true, "group1", "strategy1");
        final Feature feature3 = createFeature("feature3", "My Feature 3", true, "group2", "strategy3");

        manager.bindFeature(feature1, createServiceProperties(2, 5, "pid1"));
        manager.bindFeature(feature2, createServiceProperties(3, 5, "pid2"));
        manager.bindFeature(feature3, createServiceProperties(4, 5, "pid3"));

        final ActivationStrategy strategy1 = createStrategy("strategy1", true, "My Strategy 1");
        manager.bindStrategy(strategy1, createServiceProperties(2, 5, "pid4"));

        assertEquals(manager.getFeaturesByStrategy("strategy1").count(), 2);

        assertEquals(manager.getFeaturesByStrategy("").count(), 0);
        assertEquals(manager.getFeaturesByStrategy(null).count(), 0);
    }

    @Test
    public void testGetGroupsByStrategy() {
        final FeatureGroup group1 = createFeatureGroup("group1", "My Group 1", true, "strategy1");
        final FeatureGroup group2 = createFeatureGroup("group2", "My Group 2", true, "strategy2");
        final FeatureGroup group3 = createFeatureGroup("group3", "My Group 3", true, "strategy1");

        manager.bindFeatureGroup(group1, createServiceProperties(2, 5, "pid1"));
        manager.bindFeatureGroup(group2, createServiceProperties(3, 6, "pid2"));
        manager.bindFeatureGroup(group3, createServiceProperties(4, 6, "pid3"));

        final ActivationStrategy strategy1 = createStrategy("strategy1", true, "My Strategy 1");
        manager.bindStrategy(strategy1, createServiceProperties(2, 5, "pid4"));

        assertEquals(manager.getGroupsByStrategy("strategy1").count(), 2);

        assertEquals(manager.getGroupsByStrategy("").count(), 0);
        assertEquals(manager.getGroupsByStrategy(null).count(), 0);
    }

    @Test
    public void testEnableFeature() {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");

        manager.bindFeature(feature, createServiceProperties(2, 5, "pid1"));
        manager.enableFeature("feature1");

        assertTrue(manager.getFeature("feature1").get().isEnabled());
    }

    @Test
    public void testDisableFeature() {
        final Feature feature = createFeature("feature1", "My Feature 1", true, "group1", "strategy1");

        manager.bindFeature(feature, createServiceProperties(2, 5, "pid1"));
        manager.disableFeature("feature1");

        assertFalse(manager.getFeature("feature1").get().isEnabled());
    }

    @Test
    public void testEnableFeatureGroup() {
        manager = createFeatureGroupManagerWithCM();
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");

        manager.bindFeatureGroup(group, createServiceProperties(2, 5, "pid1"));
        manager.enableGroup("group1");

        assertTrue(manager.getGroup("group1").get().isEnabled());
    }

    @Test
    public void testDisableFeatureGroup() {
        manager = createFeatureGroupManagerWithCM();
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", true, "strategy1");

        manager.bindFeatureGroup(group, createServiceProperties(2, 5, "pid1"));
        manager.disableGroup("group1");

        assertFalse(manager.getGroup("group1").get().isEnabled());
    }

    @Test
    public void testIsEnabledWhenFeatureInstanceNotPresent() {
        assertFalse(manager.isFeatureEnabled("feature1"));
    }

    @Test
    public void testIsEnabledWhenGroupInstanceNotPresent() {
        assertFalse(manager.isGroupEnabled("group1"));
    }

    @Test
    public void testIsEnabledWhenFeatureDoesNotBelongToGroup() {
        final Feature feature = createFeature("feature1", "My Feature 1", true, (String) null, null);

        manager.bindFeature(feature, createServiceProperties(2, 5, "pid1"));

        assertTrue(manager.isFeatureEnabled("feature1"));
        manager.disableFeature("feature1");
        assertFalse(manager.isFeatureEnabled("feature1"));
    }

    @Test
    public void testIsEnabledWhenFeatureDoesNotBelongToGroupAndStrategyNotPresent() {
        final Feature feature = createFeature("feature1", "My Feature 1", true, (String) null, "strategy");

        manager.bindFeature(feature, createServiceProperties(2, 5, "pid1"));

        assertTrue(manager.isFeatureEnabled("feature1"));
        manager.disableFeature("feature1");
        assertFalse(manager.isFeatureEnabled("feature1"));
    }

    @Test
    public void testIsEnabledWhenFeatureDoesNotBelongToGroupButStrategy() {
        final Feature feature = createFeature("feature1", "My Feature 1", true, (String) null, "strategy1");

        final ActivationStrategy strategy = createStrategy("strategy1", false, "My Strategy 1");

        manager.bindFeature(feature, createServiceProperties(2, 5, "pid1"));
        manager.bindStrategy(strategy, createServiceProperties(2, 5, "pid2"));

        assertFalse(manager.isFeatureEnabled("feature1"));
    }

    @Test
    public void testIsEnabledWhenFeatureDoesBelongToGroupWithoutStrategy() {
        final Feature feature = createFeature("feature1", "My Feature 1", true, "group1", "strategy1");

        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");

        manager.bindFeature(feature, createServiceProperties(2, 5, "pid1"));
        manager.bindFeatureGroup(group, createServiceProperties(2, 5, "pid2"));

        assertFalse(manager.isFeatureEnabled("feature1"));
    }

    @Test
    public void testIsEnabledWhenFeatureDoesBelongToGroupAndHasAStrategy() {
        final Feature feature = createFeature("feature1", "My Feature 1", true, "group1", "strategy1");

        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy2");
        final ActivationStrategy strategy = createStrategy("strategy1", true, "My Strategy 1");

        manager.bindFeature(feature, createServiceProperties(2, 5, "pid1"));
        manager.bindFeatureGroup(group, createServiceProperties(2, 5, "pid2"));
        manager.bindStrategy(strategy, createServiceProperties(2, 5, "pid3"));

        assertFalse(manager.isFeatureEnabled("feature1"));
    }

    @Test
    public void testIsEnabledWhenFeatureDoesBelongToGroupButNoStrategyButGroupHasAStrategy() {
        final Feature feature1 = createFeature("feature1", "My Feature 1", false, "group1", null);
        final Feature feature2 = createFeature("feature2", "My Feature 2", false, "group1", null);

        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        final ActivationStrategy strategy = createStrategy("strategy1", true, "My Strategy 1");

        manager.bindFeature(feature1, createServiceProperties(2, 5, "pid1"));
        manager.bindFeature(feature2, createServiceProperties(2, 5, "pid2"));
        manager.bindFeatureGroup(group, createServiceProperties(2, 5, "pid3"));
        manager.bindStrategy(strategy, createServiceProperties(2, 5, "pid4"));

        assertTrue(manager.isFeatureEnabled("feature1"));
        assertTrue(manager.isFeatureEnabled("feature2"));
    }

    @Test
    public void testIsEnabledByTogglingFeatureGroup() {
        manager = createFeatureGroupManagerWithCM();
        final Feature feature1 = createFeature("feature1", "My Feature 1", true, "group1", null);
        final Feature feature2 = createFeature("feature2", "My Feature 2", true, "group1", null);

        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, null);

        manager.bindFeature(feature1, createServiceProperties(2, 5, "pid1"));
        manager.bindFeature(feature2, createServiceProperties(2, 5, "pid2"));
        manager.bindFeatureGroup(group, createServiceProperties(2, 5, "pid3"));

        assertFalse(manager.isFeatureEnabled("feature1"));
        assertFalse(manager.isFeatureEnabled("feature2"));

        manager.enableGroup("group1");

        assertTrue(manager.isFeatureEnabled("feature1"));
        assertTrue(manager.isFeatureEnabled("feature2"));
        assertTrue(manager.isGroupEnabled("group1"));

        manager.disableGroup("group1");

        assertFalse(manager.isFeatureEnabled("feature1"));
        assertFalse(manager.isFeatureEnabled("feature2"));
        assertFalse(manager.isGroupEnabled("group1"));
    }

    @Test
    public void testIsEnabledWhenFeaturesSpecifiesMultpleFeatures() {
        manager = createFeatureGroupManagerWithCM();
        final Feature feature = createFeature("feature1", "My Feature 1", false, Lists.newArrayList("group1", "group2"),
                null);

        final FeatureGroup group1 = createFeatureGroup("group1", "My Group 1", false, null);
        final FeatureGroup group2 = createFeatureGroup("group2", "My Group 2", true, null);

        manager.bindFeature(feature, createServiceProperties(2, 5, "pid1"));
        manager.bindFeatureGroup(group1, createServiceProperties(2, 5, "pid3"));
        manager.bindFeatureGroup(group2, createServiceProperties(3, 5, "pid4"));

        assertTrue(manager.isFeatureEnabled("feature1"));
    }

    @Test
    public void testGetFeaturesByGroup() {
        final Feature feature1 = createFeature("feature1", "My Feature 1", true, "group1", "strategy1");
        final Feature feature2 = createFeature("feature2", "My Feature 2", true, "group1", "strategy2");
        final Feature feature3 = createFeature("feature3", "My Feature 3", true, "group2", "strategy3");

        manager.bindFeature(feature1, createServiceProperties(2, 5, "pid1"));
        manager.bindFeature(feature2, createServiceProperties(3, 5, "pid2"));
        manager.bindFeature(feature3, createServiceProperties(4, 5, "pid3"));

        final FeatureGroup group1 = createFeatureGroup("group1", "My Group 1", true, "strategy1");
        manager.bindFeatureGroup(group1, createServiceProperties(2, 5, "pid4"));

        assertEquals(manager.getFeaturesByGroup("group1").count(), 2);

        assertEquals(manager.getFeaturesByGroup("").count(), 0);
        assertEquals(manager.getFeaturesByGroup(null).count(), 0);
    }

    @Test
    public void testFeatureNameCollissionsWithDifferentServiceRanking() {
        final Feature feature1 = createFeature("feature1", "My Feature 1", true, "group1", "strategy1");
        final Feature feature2 = createFeature("feature1", "My Feature 2", true, "group1", "strategy2");

        manager.bindFeature(feature1, createServiceProperties(2, 5, "pid1"));
        manager.bindFeature(feature2, createServiceProperties(3, 5, "pid2"));

        assertEquals(manager.getFeature("feature1").get().getDescription().get(), "My Feature 2");
    }

    @Test
    public void testFeatureNameCollissionsWithSameServiceRankingAndDifferentServiceID() {
        final Feature feature1 = createFeature("feature1", "My Feature 1", true, "group1", "strategy1");
        final Feature feature2 = createFeature("feature1", "My Feature 2", true, "group1", "strategy2");

        manager.bindFeature(feature1, createServiceProperties(3, 5, "pid1"));
        manager.bindFeature(feature2, createServiceProperties(3, 6, "pid2"));

        assertEquals(manager.getFeature("feature1").get().getDescription().get(), "My Feature 1");
    }

    @Test
    public void testFeatureGroupNameCollissionsWithDifferentServiceRanking() {
        final FeatureGroup group1 = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        final FeatureGroup group2 = createFeatureGroup("group1", "My Group 2", false, "strategy2");

        manager.bindFeatureGroup(group1, createServiceProperties(3, 5, "pid1"));
        manager.bindFeatureGroup(group2, createServiceProperties(1, 5, "pid2"));

        assertEquals(manager.getGroup("group1").get().getDescription().get(), "My Group 1");
    }

    @Test
    public void testFeatureGroupNameCollissionsWithSameServiceRankingAndDifferentServiceID() {
        final FeatureGroup group1 = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        final FeatureGroup group2 = createFeatureGroup("group1", "My Group 2", false, "strategy2");

        manager.bindFeatureGroup(group1, createServiceProperties(3, 5, "pid1"));
        manager.bindFeatureGroup(group2, createServiceProperties(3, 9, "pid2"));

        assertEquals(manager.getGroup("group1").get().getDescription().get(), "My Group 1");
    }

    @Test
    public void testStrategyNameCollissionsWithDifferentServiceRanking() {
        final ActivationStrategy strategy1 = createStrategy("strategy1", false, "My Strategy 1");
        final ActivationStrategy strategy2 = createStrategy("strategy1", false, "My Strategy 2");

        manager.bindStrategy(strategy1, createServiceProperties(3, 5, "pid1"));
        manager.bindStrategy(strategy2, createServiceProperties(1, 5, "pid2"));

        assertEquals(manager.getStrategy("strategy1").get().getDescription().get(), "My Strategy 1");
    }

    @Test
    public void testStrategyNameCollissionsWithSameServiceRankingAndDifferentServiceID() {
        final ActivationStrategy strategy1 = createStrategy("strategy1", false, "My Strategy 1");
        final ActivationStrategy strategy2 = createStrategy("strategy1", false, "My Strategy 2");

        manager.bindStrategy(strategy1, createServiceProperties(3, 5, "pid1"));
        manager.bindStrategy(strategy2, createServiceProperties(3, 2, "pid2"));

        assertEquals(manager.getStrategy("strategy1").get().getDescription().get(), "My Strategy 2");
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentGetFeature() {
        manager.getFeature(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentGetStrategy() {
        manager.getStrategy(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentGetGroup() {
        manager.getGroup(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentIsFeatureEnabled() {
        manager.isFeatureEnabled(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentIsGroupEnabled() {
        manager.isGroupEnabled(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentEnableFeature() {
        manager.enableFeature(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentDisableFeature() {
        manager.disableFeature(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentEnableGroup() {
        manager.enableGroup(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentDisableGroup() {
        manager.disableGroup(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentCreateFeature1() throws IOException {
        manager.createFeature(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentCreateFeature2() throws IOException {
        //@formatter:off
        final Factory factory = Factory.make(null, c -> c.withDescription("")
                                                         .withStrategy("")
                                                         .withGroups(Lists.newArrayList())
                                                         .withProperties(Maps.newHashMap())
                                                         .withEnabled(false)
                                                         .build());
        //@formatter:on
        manager.createFeature(factory);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentCreateFeatureGroup1() throws IOException {
        manager.createGroup(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentCreateFeatureGroup2() throws IOException {
        //@formatter:off
        final Factory factory = Factory.make(null, c -> c.withDescription("")
                                                         .withStrategy("")
                                                         .withProperties(Maps.newHashMap())
                                                         .withEnabled(false)
                                                         .build());
        //@formatter:on
        manager.createGroup(factory);
    }

    @Test
    public void testNullArgumentGetFeaturesByGroup() {
        assertEquals(manager.getFeaturesByGroup(null).count(), 0);
    }

    @Test
    public void testNullArgumentGetFeaturesByStrategy() {
        assertEquals(manager.getFeaturesByStrategy(null).count(), 0);
    }

    @Test
    public void testNullArgumentGetGroupsByStrategy() {
        assertEquals(manager.getGroupsByStrategy(null).count(), 0);
    }

    @Test
    public void testBindFeatureWithNullName() {
        final Feature feature = createFeatureCustom(null, "My Feature 1", true, "group1", "strategy1");
        manager.bindFeature(feature, createServiceProperties(3, 5, "pid1"));
        assertEquals(manager.getFeatures().count(), 0);
    }

    @Test
    public void testBindFeatureWithEmptyName() {
        final Feature feature = createFeatureCustom("", "My Feature 1", true, "group1", "strategy1");
        manager.bindFeature(feature, createServiceProperties(3, 5, "pid1"));
        assertEquals(manager.getFeatures().count(), 0);
    }

    @Test
    public void testBindFeatureGroupWithNullName() {
        final FeatureGroup group = createFeatureGroupCustom(null, "My Group 1", false, "strategy1");
        manager.bindFeatureGroup(group, createServiceProperties(3, 5, "pid1"));
        assertEquals(manager.getGroups().count(), 0);
    }

    @Test
    public void testBindFeatureGroupWithEmptyName() {
        final FeatureGroup group = createFeatureGroupCustom("", "My Group 1", false, "strategy1");
        manager.bindFeatureGroup(group, createServiceProperties(3, 5, "pid1"));
        assertEquals(manager.getGroups().count(), 0);
    }

    @Test
    public void testBindStrategyWithNullName() {
        final ActivationStrategy strategy = createActivationStrategyCustom(null, "My Strategy 1", false);
        manager.bindStrategy(strategy, createServiceProperties(3, 5, "pid1"));
        assertEquals(manager.getStrategies().count(), 0);
    }

    @Test
    public void testBindStrategyWithEmptyName() {
        final ActivationStrategy strategy = createActivationStrategyCustom("", "My Strategy 1", false);
        manager.bindStrategy(strategy, createServiceProperties(3, 5, "pid1"));
        assertEquals(manager.getStrategies().count(), 0);
    }

    @Test
    public void testConfigAdmin() {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        manager = new FeatureManager();
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        manager.bindFeature(feature, createServiceProperties(2, 5, "feature1"));
        manager.setConfigurationAdmin(configurationAdmin);
        manager.enableFeature("feature1");
        manager.unsetConfigurationAdmin(configurationAdmin);

        assertTrue(manager.getFeature("feature1").get().isEnabled());
    }

    @Test
    public void testConfigAdminWhenPIDEmpty() {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        manager = new FeatureManager();
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        manager.bindFeature(feature, createServiceProperties(2, 5, ""));
        manager.setConfigurationAdmin(configurationAdmin);
        assertFalse(manager.enableFeature("feature1"));
        manager.unsetConfigurationAdmin(configurationAdmin);
    }

    @Test
    public void testConfigAdminWhenPIDEmpty2() {
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        manager = new FeatureManager();
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, group);

        manager.bindFeatureGroup(group, createServiceProperties(2, 5, ""));
        manager.setConfigurationAdmin(configurationAdmin);
        assertFalse(manager.enableGroup("group1"));
        manager.unsetConfigurationAdmin(configurationAdmin);
    }

    @Test
    public void testConfigListenerForFeature() {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        manager = new FeatureManager();
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);
        configurationAdmin.addListener(manager);

        manager.bindFeature(feature, createServiceProperties(2, 5, "feature1"));
        manager.setConfigurationAdmin(configurationAdmin);
        final ConfigurationListener listener = new ConfigurationListener() {
            @Override
            public void accept(final ConfigurationEvent event) {
                assertEquals(event.getType(), Type.UPDATED);
            }
        };
        manager.bindConfigurationListener(listener);
        manager.activate(context);
        doReturn(feature).when(context).getService(reference);
        manager.enableFeature("feature1");
        manager.unbindConfigurationListener(listener);
        manager.unsetConfigurationAdmin(configurationAdmin);

        assertTrue(manager.getFeature("feature1").get().isEnabled());
    }

    @Test
    public void testConfigListenerForFeatureGroup() {
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        manager = new FeatureManager();
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, group);
        configurationAdmin.addListener(manager);

        manager.bindFeatureGroup(group, createServiceProperties(2, 5, "group1"));
        manager.setConfigurationAdmin(configurationAdmin);
        final ConfigurationListener listener = new ConfigurationListener() {
            @Override
            public void accept(final ConfigurationEvent event) {
                assertEquals(event.getType(), Type.UPDATED);
            }
        };
        manager.bindConfigurationListener(listener);
        manager.activate(context);
        doReturn(group).when(context).getService(reference);
        manager.enableGroup("group1");
        manager.unbindConfigurationListener(listener);
        manager.unsetConfigurationAdmin(configurationAdmin);

        assertTrue(manager.getGroup("group1").get().isEnabled());
    }

    @Test
    public void testGetEventMethodForStrategizableButNotFeatureOrFeatureGroup() {
        manager = new FeatureManager();
        try {
            final Method method = manager.getClass().getDeclaredMethod("getEvent", Strategizable.class, int.class);
            method.setAccessible(true);
            final ConfigurationEvent event = (ConfigurationEvent) method.invoke(manager, strategizable, 1);
            assertTrue(event.getProperties().isEmpty());
        } catch (final NoSuchMethodException e) {
            throw new AssertionError(e.getMessage());
        } catch (final IllegalArgumentException e) {
            throw new AssertionError(e.getMessage());
        } catch (final IllegalAccessException e) {
            throw new AssertionError(e.getMessage());
        } catch (final InvocationTargetException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    @Test
    public void testConfigListenerForFeatureGroupWithIOException() throws IOException {
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        manager = new FeatureManager();

        manager.bindFeatureGroup(group, createServiceProperties(2, 5, "group1"));
        manager.setConfigurationAdmin(configurationAdmin);
        final ConfigurationListener listener = new ConfigurationListener() {
            @Override
            public void accept(final ConfigurationEvent event) {
                assertEquals(event.getType(), Type.UPDATED);
            }
        };
        manager.bindConfigurationListener(listener);
        manager.activate(context);

        doThrow(IOException.class).when(configurationAdmin).getConfiguration("group1", "?");

        manager.enableGroup("group1");
        manager.unbindConfigurationListener(listener);
        manager.unsetConfigurationAdmin(configurationAdmin);

        assertFalse(manager.getGroup("group1").get().isEnabled());
    }

    @Test
    public void testServiceDescription() throws ClassNotFoundException, NoSuchMethodException, SecurityException,
            InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final Feature feature1 = createFeature("feature1", "My Feature 2", false, "group1", "strategy1");
        final Feature feature2 = createFeature("feature1", "My Feature 2", false, "group2", "strategy1");
        final Feature feature3 = createFeature("feature2", "My Feature", false, "group", "strategy");
        final Feature feature4 = createFeature("feature3", "My Feature", false, "group", "strategy");
        final Map<String, Object> props1 = createServiceProperties(3, 5, "");
        final Map<String, Object> props2 = createServiceProperties(3, 5, "");
        final Map<String, Object> props3 = createServiceProperties(3, 5, "myPid");
        final Map<String, Object> props4 = createServiceProperties(3, 6, "myPid");

        final Class clazz = Class.forName("com.amitinside.featureflags.internal.FeatureManager$Description");
        final Constructor constructor = clazz.getConstructor(Object.class, Map.class);
        constructor.setAccessible(true);
        final Object instance1 = constructor.newInstance(feature1, props1);
        final Object instance2 = constructor.newInstance(feature2, props2);
        final Object instance3 = constructor.newInstance(feature3, props3);
        final Object instance4 = constructor.newInstance(feature4, props4);

        assertTrue(instance1.equals(instance2));
        assertFalse(instance1.equals(instance4));
        assertFalse(instance1.equals(props1));
        assertEquals(instance1.hashCode(), instance2.hashCode());

        assertEquals(instance3.toString(),
                "Description{Ranking=3, ServiceID=5, Instance=ConfiguredFeature{Name=feature2, Description=My Feature, Strategy=strategy, Groups=[group], Enabled=false}, Properties={service.id=5, service.ranking=3, service.pid=myPid}}");
    }

    @Test
    public void testCreateFeature() throws IOException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        manager.activate(context);
        manager.setConfigurationAdmin(configurationAdmin);

        final Map<String, Object> props = Maps.newHashMap();
        props.put("p", "test");

        //@formatter:off
        final Factory factory = Factory.make("feature1", c -> c.withDescription("My Feature 1")
                                       .withStrategy("strategy1")
                                       .withGroups(Lists.newArrayList("group1"))
                                       .withProperties(props)
                                       .withEnabled(false)
                                       .build());
        //@formatter:on
        assertTrue(manager.createFeature(factory).isPresent());
    }

    @Test
    public void testCreateFeatureIOException() throws IOException {
        manager.activate(context);
        manager.setConfigurationAdmin(configurationAdmin);

        final Map<String, Object> props = Maps.newHashMap();
        props.put("p", "test");
        doThrow(IOException.class).when(configurationAdmin).createFactoryConfiguration(FEATURE_FACTORY_PID);

        //@formatter:off
        final Factory factory = Factory.make("feature1", c -> c.withDescription("My Feature 1")
                                                               .withStrategy("strategy1")
                                                               .withGroups(Lists.newArrayList("group1"))
                                                               .withProperties(props)
                                                               .withEnabled(false)
                                                               .build());
        //@formatter:on
        assertFalse(manager.createFeature(factory).isPresent());
    }

    @Test
    public void testCreateFeatureGroup() throws IOException {
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        configurationAdmin = new ConfigurationAdminMock(manager, reference, group);

        manager.activate(context);
        manager.setConfigurationAdmin(configurationAdmin);

        final Map<String, Object> props = Maps.newHashMap();
        props.put("p", "test");

        //@formatter:off
        final Factory factory = Factory.make("group1", c -> c.withDescription("My Group 1")
                                                               .withStrategy("strategy1")
                                                               .withProperties(props)
                                                               .withEnabled(false)
                                                               .build());
        //@formatter:on
        assertTrue(manager.createGroup(factory).isPresent());
    }

    @Test
    public void testCreateFeatureGroupIOException() throws IOException {
        manager.activate(context);
        manager.setConfigurationAdmin(configurationAdmin);

        final Map<String, Object> props = Maps.newHashMap();
        props.put("p", "test");
        doThrow(IOException.class).when(configurationAdmin).createFactoryConfiguration(FEATURE_GROUP_FACTORY_PID);

        //@formatter:off
        final Factory factory = Factory.make("group1", c -> c.withDescription("My Group 1")
                                                             .withStrategy("strategy1")
                                                             .withProperties(props)
                                                             .withEnabled(false)
                                                             .build());
        //@formatter:on
        assertFalse(manager.createGroup(factory).isPresent());
    }

    @Test
    public void testRemoveFeatureIOException() throws IOException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");

        final Map<String, Object> props = createServiceProperties(3, 5, "myPid");
        manager.bindFeature(feature, props);
        manager.setConfigurationAdmin(configurationAdmin);

        assertEquals(manager.getFeatures().count(), 1);
        doThrow(IOException.class).when(configurationAdmin).getConfiguration("myPid");

        manager.removeFeature("feature1");
        assertEquals(manager.getFeatures().count(), 1);
    }

    @Test
    public void testRemoveFeatureGroupIOException() throws IOException {
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");

        final Map<String, Object> props = createServiceProperties(3, 5, "myPid");
        manager.bindFeatureGroup(group, props);
        manager.setConfigurationAdmin(configurationAdmin);

        assertEquals(manager.getGroups().count(), 1);
        doThrow(IOException.class).when(configurationAdmin).getConfiguration("myPid");

        manager.removeGroup("group1");
        assertEquals(manager.getGroups().count(), 1);
    }

    @Test
    public void testRemoveFeature() throws IOException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        final Map<String, Object> props = createServiceProperties(3, 5, "myPid");
        manager.bindFeature(feature, props);
        manager.setConfigurationAdmin(configurationAdmin);

        assertEquals(manager.getFeatures().count(), 1);

        manager.removeFeature("feature1");
        manager.unbindFeature(feature, props);

        assertEquals(manager.getFeatures().count(), 0);
    }

    @Test
    public void testRemoveFeatureGroup() throws IOException {
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        configurationAdmin = new ConfigurationAdminMock(manager, reference, group);

        final Map<String, Object> props = createServiceProperties(3, 5, "myPid");
        manager.bindFeatureGroup(group, props);
        manager.setConfigurationAdmin(configurationAdmin);

        assertEquals(manager.getGroups().count(), 1);

        manager.removeGroup("group1");
        manager.unbindFeatureGroup(group, props);

        assertEquals(manager.getGroups().count(), 0);
    }

}
