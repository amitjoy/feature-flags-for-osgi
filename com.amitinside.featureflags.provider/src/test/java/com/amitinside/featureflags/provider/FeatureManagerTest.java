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

import static com.amitinside.featureflags.Constants.*;
import static com.amitinside.featureflags.provider.TestHelper.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;

import com.amitinside.featureflags.Configurable;
import com.amitinside.featureflags.ConfigurationEvent;
import com.amitinside.featureflags.ConfigurationEvent.Type;
import com.amitinside.featureflags.Strategizable;
import com.amitinside.featureflags.StrategizableFactory;
import com.amitinside.featureflags.StrategyFactory;
import com.amitinside.featureflags.StrategyFactory.StrategyType;
import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.feature.group.FeatureGroup;
import com.amitinside.featureflags.listener.ConfigurationListener;
import com.amitinside.featureflags.strategy.ActivationStrategy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings({ "unchecked", "rawtypes" })
@RunWith(MockitoJUnitRunner.class)
public final class FeatureManagerTest {

    private FeatureManagerProvider manager;

    @Mock
    private BundleContext context;
    @Mock
    private ServiceReference reference;
    @Mock
    private Strategizable strategizable;
    @Mock
    private ConfigurationAdmin configurationAdmin;
    @Mock
    private org.osgi.service.cm.ConfigurationEvent event;

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
    public void testIsEnabledWhenFeatureDoesNotBelongToGroupButStrategy() throws InvalidSyntaxException {
        final Feature feature = createFeature("feature1", "My Feature 1", true, (String) null, "strategy1");

        final ActivationStrategy strategy = createStrategy("strategy1", false, "My Strategy 1");

        manager.bindFeature(feature, createServiceProperties(2, 5, "pid1"));
        manager.bindStrategy(strategy, createServiceProperties(2, 5, "pid2"));
        manager.activate(context);

        doReturn(new ServiceReference[] { reference }).when(context).getServiceReferences(Feature.class.getName(),
                null);
        doReturn(feature).when(context).getService(reference);
        final List<String> propKeys = Lists.newArrayList("service.id", "service.ranking", "service.pid");
        doReturn(propKeys.toArray(new String[0])).when(reference).getPropertyKeys();
        doReturn(5).when(reference).getProperty("service.id");
        doReturn(2).when(reference).getProperty("service.ranking");
        doReturn("pid1").when(reference).getProperty("service.pid");

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
    public void testIsEnabledWhenFeatureDoesBelongToGroupButNoActualGroupIsAvailable() {
        final Feature feature = createFeature("feature1", "My Feature 1", true, "group1", "strategy1");

        manager.bindFeature(feature, createServiceProperties(2, 5, "pid1"));

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
    public void testIsEnabledWhenFeatureDoesBelongToGroupButNoStrategyButGroupHasAStrategy()
            throws InvalidSyntaxException {
        final Feature feature1 = createFeature("feature1", "My Feature 1", false, "group1", null);
        final Feature feature2 = createFeature("feature2", "My Feature 2", false, "group1", null);

        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        final ActivationStrategy strategy = createStrategy("strategy1", true, "My Strategy 1");

        manager.bindFeature(feature1, createServiceProperties(2, 5, "pid1"));
        manager.bindFeature(feature2, createServiceProperties(2, 5, "pid2"));
        manager.bindFeatureGroup(group, createServiceProperties(2, 5, "pid3"));
        manager.bindStrategy(strategy, createServiceProperties(2, 5, "pid4"));
        manager.activate(context);

        doReturn(new ServiceReference[] { reference }).when(context).getServiceReferences(FeatureGroup.class.getName(),
                null);
        doReturn(group).when(context).getService(reference);
        final List<String> propKeys = Lists.newArrayList("service.id", "service.ranking", "service.pid");
        doReturn(propKeys.toArray(new String[0])).when(reference).getPropertyKeys();
        doReturn(5).when(reference).getProperty("service.id");
        doReturn(2).when(reference).getProperty("service.ranking");
        doReturn("pid3").when(reference).getProperty("service.pid");

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
        final StrategizableFactory factory = StrategizableFactory.make(null, c -> c.withDescription("")
                                                         .withStrategy("")
                                                         .withGroups(Lists.newArrayList())
                                                         .withProperties(Maps.newHashMap())
                                                         .withEnabled(false)
                                                         .build());
        //@formatter:on
        manager.createFeature(factory);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentUpdateFeature1() throws IOException {
        manager.updateFeature(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentUpdateFeature2() throws IOException {
        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make(null, c -> c.withDescription("")
                                                         .withStrategy("")
                                                         .withGroups(Lists.newArrayList())
                                                         .withProperties(Maps.newHashMap())
                                                         .withEnabled(false)
                                                         .build());
        //@formatter:on
        manager.updateFeature(factory);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentUpdateFeatureGroup1() throws IOException {
        manager.updateGroup(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentUpdateFeatureGroup2() throws IOException {
        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make(null, c -> c.withDescription("")
                                                         .withStrategy("")
                                                         .withProperties(Maps.newHashMap())
                                                         .withEnabled(false)
                                                         .build());
        //@formatter:on
        manager.updateGroup(factory);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentCreateFeatureGroup1() throws IOException {
        manager.createGroup(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentCreateFeatureGroup2() throws IOException {
        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make(null, c -> c.withDescription("")
                                                         .withStrategy("")
                                                         .withProperties(Maps.newHashMap())
                                                         .withEnabled(false)
                                                         .build());
        //@formatter:on
        manager.createGroup(factory);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentCreateStrategy1() throws IOException {
        manager.createPropertyBasedStrategy(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentCreateStrategy2() throws IOException {
        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make(null, StrategyType.SERVICE_PROPERTY, c ->
                                                                    c.withDescription("dummy")
                                                                     .withKey("key")
                                                                     .withValue("val")
                                                                     .build());
        //@formatter:on
        manager.createPropertyBasedStrategy(factory);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentUpdateStrategy1() throws IOException {
        manager.updatePropertyBasedStrategy(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentUpdateStrategy2() throws IOException {
        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make(null, StrategyType.SERVICE_PROPERTY, c ->
                                                                    c.withDescription("dummy")
                                                                     .withKey("key")
                                                                     .withValue("val")
                                                                     .build());
        //@formatter:on
        manager.updatePropertyBasedStrategy(factory);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentRemoveStrategy() throws IOException {
        manager.removePropertyBasedStrategy(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentRemoveFeature() throws IOException {
        manager.removeFeature(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgumentRemoveFeatureGroup() throws IOException {
        manager.removeGroup(null);
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
        manager = new FeatureManagerProvider();
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
        manager = new FeatureManagerProvider();
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        manager.bindFeature(feature, createServiceProperties(2, 5, ""));
        manager.setConfigurationAdmin(configurationAdmin);
        assertFalse(manager.enableFeature("feature1"));
        manager.unsetConfigurationAdmin(configurationAdmin);
    }

    @Test
    public void testConfigAdminWhenPIDEmpty2() {
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        manager = new FeatureManagerProvider();
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, group);

        manager.bindFeatureGroup(group, createServiceProperties(2, 5, ""));
        manager.setConfigurationAdmin(configurationAdmin);
        assertFalse(manager.enableGroup("group1"));
        manager.unsetConfigurationAdmin(configurationAdmin);
    }

    @Test
    public void testConfigListenerForFeatureWhenConfigUpdated() throws InvalidSyntaxException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        manager = new FeatureManagerProvider();
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

        doReturn(new ServiceReference[] { reference }).when(context).getServiceReferences(Feature.class.getName(),
                null);
        doReturn(feature).when(context).getService(reference);
        final List<String> propKeys = Lists.newArrayList("service.id", "service.ranking", "service.pid");
        doReturn(propKeys.toArray(new String[0])).when(reference).getPropertyKeys();
        doReturn(5).when(reference).getProperty("service.id");
        doReturn(2).when(reference).getProperty("service.ranking");
        doReturn("feature1").when(reference).getProperty("service.pid");

        manager.enableFeature("feature1");
        manager.unbindConfigurationListener(listener);
        manager.unsetConfigurationAdmin(configurationAdmin);

        assertTrue(manager.getFeature("feature1").get().isEnabled());
    }

    @Test
    public void testConfigListenerForFeatureWhenConfigDeleted() throws InvalidSyntaxException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        manager = new FeatureManagerProvider();
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);
        configurationAdmin.addListener(manager);

        manager.bindFeature(feature, createServiceProperties(2, 5, "feature1"));
        manager.setConfigurationAdmin(configurationAdmin);
        final ConfigurationListener listener = new ConfigurationListener() {
            @Override
            public void accept(final ConfigurationEvent event) {
                assertEquals(event.getType(), Type.DELETED);
            }
        };
        manager.bindConfigurationListener(listener);
        manager.activate(context);

        doReturn(new ServiceReference[] { reference }).when(context).getServiceReferences(Feature.class.getName(),
                null);
        doReturn(feature).when(context).getService(reference);
        final List<String> propKeys = Lists.newArrayList("service.id", "service.ranking", "service.pid");
        doReturn(propKeys.toArray(new String[0])).when(reference).getPropertyKeys();
        doReturn(5).when(reference).getProperty("service.id");
        doReturn(2).when(reference).getProperty("service.ranking");
        doReturn("feature1").when(reference).getProperty("service.pid");

        manager.removeFeature("feature1");
        manager.unbindConfigurationListener(listener);
        manager.unsetConfigurationAdmin(configurationAdmin);

        assertFalse(manager.getFeature("feature1").get().isEnabled());
    }

    @Test
    public void testConfigListenerForFeatureGroup() throws InvalidSyntaxException {
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        manager = new FeatureManagerProvider();
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

        doReturn(new ServiceReference[] { reference }).when(context).getServiceReferences(FeatureGroup.class.getName(),
                null);
        doReturn(group).when(context).getService(reference);
        final List<String> propKeys = Lists.newArrayList("service.id", "service.ranking", "service.pid");
        doReturn(propKeys.toArray(new String[0])).when(reference).getPropertyKeys();
        doReturn(5).when(reference).getProperty("service.id");
        doReturn(2).when(reference).getProperty("service.ranking");
        doReturn("group1").when(reference).getProperty("service.pid");

        manager.enableGroup("group1");
        manager.unbindConfigurationListener(listener);
        manager.unsetConfigurationAdmin(configurationAdmin);

        assertTrue(manager.getGroup("group1").get().isEnabled());
    }

    @Test
    public void testConfigListenerForStrategy() throws InvalidSyntaxException {
        final ActivationStrategy strategy = createActivationStrategyCustom("strategy1", "My Strategy 1", false);
        manager = new FeatureManagerProvider();
        final ConfigurationAdminMock configurationAdmin = new ConfigurationAdminMock(manager, reference, strategy);
        configurationAdmin.addListener(manager);

        final Map<String, Object> props = createServiceProperties(3, 5, "pid1");
        manager.bindStrategy(strategy, props);
        manager.setConfigurationAdmin(configurationAdmin);
        final ConfigurationListener listener = new ConfigurationListener() {
            @Override
            public void accept(final ConfigurationEvent event) {
                assertEquals(event.getType(), Type.DELETED);
            }
        };
        manager.bindConfigurationListener(listener);
        manager.activate(context);

        doReturn(new ServiceReference[] { reference }).when(context)
                .getServiceReferences(ActivationStrategy.class.getName(), null);
        doReturn(strategy).when(context).getService(reference);
        final List<String> propKeys = Lists.newArrayList("service.id", "service.ranking", "service.pid");
        doReturn(propKeys.toArray(new String[0])).when(reference).getPropertyKeys();
        doReturn(5).when(reference).getProperty("service.id");
        doReturn(3).when(reference).getProperty("service.ranking");
        doReturn("strategy1").when(reference).getProperty("service.pid");

        manager.removePropertyBasedStrategy("strategy1");
        manager.unbindConfigurationListener(listener);
        manager.unsetConfigurationAdmin(configurationAdmin);
        manager.unbindStrategy(strategy, props);

        assertFalse(manager.getStrategy("strategy1").isPresent());
    }

    @Test
    public void testConfigurationEventMethod() {
        manager.activate(context);
        manager.configurationEvent(event);
        verify(event, times(0)).getType();
    }

    @Test
    public void testGetEventMethodForStrategizableButNotFeatureOrFeatureGroup() {
        manager = new FeatureManagerProvider();
        try {
            final Method method = manager.getClass().getDeclaredMethod("getEvent", Configurable.class, int.class);
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
        manager = new FeatureManagerProvider();

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

        final Class clazz = Class.forName("com.amitinside.featureflags.provider.FeatureManagerProvider$Description");
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

        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        final Map<String, Object> props = Maps.newHashMap();
        props.put("p", "test");

        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make("feature1", c -> c.withDescription("My Feature 1")
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
        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        final Map<String, Object> props = Maps.newHashMap();
        props.put("p", "test");
        doThrow(IOException.class).when(configurationAdmin).createFactoryConfiguration(FEATURE_FACTORY_PID);

        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make("feature1", c -> c.withDescription("My Feature 1")
                                                               .withStrategy("strategy1")
                                                               .withGroups(Lists.newArrayList("group1"))
                                                               .withProperties(props)
                                                               .withEnabled(false)
                                                               .build());
        //@formatter:on
        assertFalse(manager.createFeature(factory).isPresent());
    }

    @Test
    public void testUpdateFeature() throws IOException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        configurationAdmin = new ConfigurationAdminMock(manager, reference, feature);

        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        final Map<String, Object> props = Maps.newHashMap();
        props.put("p", "test");

        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make("feature1", c -> c.withDescription("My Feature 1")
                                                               .withStrategy("strategy1")
                                                               .withGroups(Lists.newArrayList("group1"))
                                                               .withProperties(props)
                                                               .withEnabled(false)
                                                               .build());
        //@formatter:on
        assertTrue(manager.updateFeature(factory));
    }

    @Test
    public void testUpdateFeatureIOException() throws IOException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        final Map<String, Object> serviceProps = createServiceProperties(3, 5, "myPid");

        manager.setConfigurationAdmin(configurationAdmin);
        manager.bindFeature(feature, serviceProps);
        manager.activate(context);

        final Map<String, Object> props = Maps.newHashMap();
        props.put("p", "test");
        doThrow(IOException.class).when(configurationAdmin).getConfiguration("myPid");

        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make("feature1", c -> c.withDescription("My Feature 1")
                                                               .withStrategy("strategy1")
                                                               .withGroups(Lists.newArrayList("group1"))
                                                               .withProperties(props)
                                                               .withEnabled(false)
                                                               .build());
        //@formatter:on
        assertFalse(manager.updateFeature(factory));
    }

    @Test
    public void testUpdateFeatureConfigurationNull() throws IOException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        final Map<String, Object> serviceProps = createServiceProperties(3, 5, "myPid");

        manager.setConfigurationAdmin(configurationAdmin);
        manager.bindFeature(feature, serviceProps);
        manager.activate(context);

        final Map<String, Object> props = Maps.newHashMap();
        props.put("p", "test");
        doReturn(null).when(configurationAdmin).getConfiguration("myPid");

        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make("feature1", c -> c.withDescription("My Feature 1")
                .withStrategy("strategy1")
                .withGroups(Lists.newArrayList("group1"))
                .withProperties(props)
                .withEnabled(false)
                .build());
        //@formatter:on
        assertFalse(manager.updateFeature(factory));
    }

    @Test
    public void testCreateFeatureGroup() throws IOException {
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        configurationAdmin = new ConfigurationAdminMock(manager, reference, group);

        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        final Map<String, Object> props = Maps.newHashMap();
        props.put("p", "test");

        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make("group1", c -> c.withDescription("My Group 1")
                                                             .withStrategy("strategy1")
                                                             .withProperties(props)
                                                             .withEnabled(false)
                                                             .build());
        //@formatter:on
        assertTrue(manager.createGroup(factory).isPresent());
    }

    @Test
    public void testCreateFeatureGroupIOException() throws IOException {
        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        final Map<String, Object> props = Maps.newHashMap();
        props.put("p", "test");
        doThrow(IOException.class).when(configurationAdmin).createFactoryConfiguration(FEATURE_GROUP_FACTORY_PID);

        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make("group1", c -> c.withDescription("My Group 1")
                                                             .withStrategy("strategy1")
                                                             .withProperties(props)
                                                             .withEnabled(false)
                                                             .build());
        //@formatter:on
        assertFalse(manager.createGroup(factory).isPresent());
    }

    @Test
    public void testUpdateFeatureGroup() throws IOException {
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        configurationAdmin = new ConfigurationAdminMock(manager, reference, group);

        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        final Map<String, Object> props = Maps.newHashMap();
        props.put("p", "test");

        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make("group1", c -> c.withDescription("My Group 1")
                                                             .withStrategy("strategy1")
                                                             .withProperties(props)
                                                             .withEnabled(false)
                                                             .build());
        //@formatter:on
        assertTrue(manager.updateGroup(factory));
    }

    @Test
    public void testUpdateFeatureGroupIOException() throws IOException {
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        final Map<String, Object> serviceProps = createServiceProperties(3, 5, "myPid");

        manager.setConfigurationAdmin(configurationAdmin);
        manager.bindFeatureGroup(group, serviceProps);
        manager.activate(context);

        final Map<String, Object> props = Maps.newHashMap();
        props.put("p", "test");
        doThrow(IOException.class).when(configurationAdmin).getConfiguration("myPid");

        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make("group1", c -> c.withDescription("My Group 1")
                                                             .withStrategy("strategy1")
                                                             .withProperties(props)
                                                             .withEnabled(false)
                                                             .build());
        //@formatter:on
        assertFalse(manager.updateGroup(factory));
    }

    @Test
    public void testUpdateFeatureGroupConfigurationNull() throws IOException {
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", false, "strategy1");
        final Map<String, Object> serviceProps = createServiceProperties(3, 5, "myPid");

        manager.setConfigurationAdmin(configurationAdmin);
        manager.bindFeatureGroup(group, serviceProps);
        manager.activate(context);

        final Map<String, Object> props = Maps.newHashMap();
        props.put("p", "test");
        doReturn(null).when(configurationAdmin).getConfiguration("myPid");

        //@formatter:off
        final StrategizableFactory factory = StrategizableFactory.make("group1", c -> c.withDescription("My Group 1")
                .withStrategy("strategy1")
                .withProperties(props)
                .withEnabled(false)
                .build());
        //@formatter:on
        assertFalse(manager.updateGroup(factory));
    }

    @Test
    public void testCreateServicePropertyBasedStrategy() throws IOException {
        final ActivationStrategy strategy = createServicePropertyActivationStrategy("strategy1", "My Strategy 1", "key",
                "value");
        configurationAdmin = new ConfigurationAdminMock(manager, reference, strategy);

        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make("ServiceStrategy", StrategyType.SERVICE_PROPERTY,
                                                                             c -> c.withDescription("My Strategy 1")
                                                                                   .withKey("propKey")
                                                                                   .withValue("propValue")
                                                                                   .build());
        //@formatter:on
        assertTrue(manager.createPropertyBasedStrategy(factory).isPresent());
    }

    @Test
    public void testCreateServicePropertyBasedStrategyIOException() throws IOException {
        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        doThrow(IOException.class).when(configurationAdmin).createFactoryConfiguration(STRATEGY_SERVICE_PROPERTY_PID);
        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make("ServiceStrategy", StrategyType.SERVICE_PROPERTY,
                                                                             c -> c.withDescription("My Strategy 1")
                                                                                   .withKey("propKey")
                                                                                   .withValue("propValue")
                                                                                   .build());
        //@formatter:on
        assertFalse(manager.createPropertyBasedStrategy(factory).isPresent());
    }

    @Test
    public void testUpdateServicePropertyBasedStrategy() throws IOException {
        final ActivationStrategy strategy = createServicePropertyActivationStrategy("strategy1", "My Strategy 1", "key",
                "value");
        configurationAdmin = new ConfigurationAdminMock(manager, reference, strategy);

        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make("ServiceStrategy", StrategyType.SERVICE_PROPERTY,
                                                                             c -> c.withDescription("My Strategy 1")
                                                                                   .withKey("propKey")
                                                                                   .withValue("propValue")
                                                                                   .build());
        //@formatter:on
        assertTrue(manager.updatePropertyBasedStrategy(factory));
    }

    @Test
    public void testUpdateServicePropertyBasedStrategyIOException() throws IOException {
        final ActivationStrategy strategy = createServicePropertyActivationStrategy("ServiceStrategy", "My Strategy 1",
                "key", "value");
        final Map<String, Object> serviceProps = createServiceProperties(3, 5, "myPid");

        manager.bindStrategy(strategy, serviceProps);
        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        doThrow(IOException.class).when(configurationAdmin).getConfiguration("myPid");
        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make("ServiceStrategy", StrategyType.SERVICE_PROPERTY,
                                                                            c -> c.withDescription("My Strategy 1")
                                                                            .withKey("propKey")
                                                                            .withValue("propValue")
                                                                            .build());
        //@formatter:on
        assertFalse(manager.updatePropertyBasedStrategy(factory));
    }

    @Test
    public void testUpdateServicePropertyBasedStrategyConfigurationNull() throws IOException {
        final ActivationStrategy strategy = createServicePropertyActivationStrategy("ServiceStrategy", "My Strategy 1",
                "key", "value");
        final Map<String, Object> serviceProps = createServiceProperties(3, 5, "myPid");

        manager.bindStrategy(strategy, serviceProps);
        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        doReturn(null).when(configurationAdmin).getConfiguration("myPid");
        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make("ServiceStrategy", StrategyType.SERVICE_PROPERTY,
                c -> c.withDescription("My Strategy 1")
                .withKey("propKey")
                .withValue("propValue")
                .build());
        //@formatter:on
        assertFalse(manager.updatePropertyBasedStrategy(factory));
    }

    @Test
    public void testCreateSystemPropertyBasedStrategy() throws IOException {
        final ActivationStrategy strategy = createSystemPropertyActivationStrategy("SystemStrategy", "My Strategy 1",
                "key", "value");
        configurationAdmin = new ConfigurationAdminMock(manager, reference, strategy);

        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make("SystemStrategy", StrategyType.SYSTEM_PROPERTY,
                                                                            c -> c.withDescription("My Strategy 1")
                                                                                  .withKey("propKey")
                                                                                  .withValue("propValue")
                                                                                  .build());
        //@formatter:on
        assertTrue(manager.createPropertyBasedStrategy(factory).isPresent());
    }

    @Test
    public void testCreateSystemPropertyBasedStrategyIOException() throws IOException {
        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        doThrow(IOException.class).when(configurationAdmin).createFactoryConfiguration(STRATEGY_SYSTEM_PROPERTY_PID);
        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make("SystemStrategy", StrategyType.SYSTEM_PROPERTY,
                                                                            c -> c.withDescription("My Strategy 1")
                                                                            .withKey("propKey")
                                                                            .withValue("propValue")
                                                                            .build());
        //@formatter:on
        assertFalse(manager.createPropertyBasedStrategy(factory).isPresent());
    }

    @Test
    public void testUpdateSystemPropertyBasedStrategy() throws IOException {
        final ActivationStrategy strategy = createServicePropertyActivationStrategy("strategy1", "My Strategy 1", "key",
                "value");
        configurationAdmin = new ConfigurationAdminMock(manager, reference, strategy);

        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make("ServiceStrategy", StrategyType.SYSTEM_PROPERTY,
                                                                             c -> c.withDescription("My Strategy 1")
                                                                                   .withKey("propKey")
                                                                                   .withValue("propValue")
                                                                                   .build());
        //@formatter:on
        assertTrue(manager.updatePropertyBasedStrategy(factory));
    }

    @Test
    public void testUpdateSystemPropertyBasedStrategyIOException() throws IOException {
        final ActivationStrategy strategy = createServicePropertyActivationStrategy("SystemStrategy", "My Strategy 1",
                "key", "value");
        final Map<String, Object> serviceProps = createServiceProperties(3, 5, "myPid");

        manager.bindStrategy(strategy, serviceProps);
        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        doThrow(IOException.class).when(configurationAdmin).getConfiguration("myPid");
        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make("SystemStrategy", StrategyType.SYSTEM_PROPERTY,
                                                                            c -> c.withDescription("My Strategy 1")
                                                                            .withKey("propKey")
                                                                            .withValue("propValue")
                                                                            .build());
        //@formatter:on
        assertFalse(manager.updatePropertyBasedStrategy(factory));
    }

    @Test
    public void testUpdateSystemPropertyBasedStrategyConfigurationNull() throws IOException {
        final ActivationStrategy strategy = createServicePropertyActivationStrategy("SystemStrategy", "My Strategy 1",
                "key", "value");
        final Map<String, Object> serviceProps = createServiceProperties(3, 5, "myPid");

        manager.bindStrategy(strategy, serviceProps);
        manager.setConfigurationAdmin(configurationAdmin);
        manager.activate(context);

        doReturn(null).when(configurationAdmin).getConfiguration("myPid");
        //@formatter:off
        final StrategyFactory factory = StrategyFactory.make("SystemStrategy", StrategyType.SYSTEM_PROPERTY,
                c -> c.withDescription("My Strategy 1")
                .withKey("propKey")
                .withValue("propValue")
                .build());
        //@formatter:on
        assertFalse(manager.updatePropertyBasedStrategy(factory));
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
    public void testRemoveFeatureConfigurationNull() throws IOException {
        final Feature feature = createFeature("feature1", "My Feature 1", false, "group1", "strategy1");
        final Map<String, Object> props = createServiceProperties(3, 5, "myPid");

        manager.bindFeature(feature, props);
        manager.setConfigurationAdmin(configurationAdmin);

        assertEquals(manager.getFeatures().count(), 1);
        doReturn(null).when(configurationAdmin).getConfiguration("myPid");

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

    @Test
    public void testRemovePropertyBasedStrategy() throws IOException {
        final ActivationStrategy strategy = createServicePropertyActivationStrategy("strategy1", "My Strategy", "key",
                "value");
        configurationAdmin = new ConfigurationAdminMock(manager, reference, strategy);

        final Map<String, Object> props = createServiceProperties(3, 5, "myPid");
        manager.bindStrategy(strategy, props);
        manager.setConfigurationAdmin(configurationAdmin);

        assertEquals(manager.getStrategies().count(), 1);

        manager.removePropertyBasedStrategy("strategy1");
        manager.unbindStrategy(strategy, props);

        assertEquals(manager.getStrategies().count(), 0);
    }

}