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

import static com.qivicon.featureflags.internal.TestUtil.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.qivicon.featureflags.feature.Feature;
import com.qivicon.featureflags.feature.group.FeatureGroup;
import com.qivicon.featureflags.strategy.ActivationStrategy;

public final class FeatureServiceTest {

    private FeatureManager manager;

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
    public void testIsEnabledWhenFeatureDoesNotBelongToGroup() {
        final Feature feature = createFeature("feature1", "My Feature 1", true, null, "strategy1");

        manager.bindFeature(feature, createServiceProperties(2, 5, "pid1"));

        assertTrue(manager.isFeatureEnabled("feature1"));
        manager.disableFeature("feature1");
        assertFalse(manager.isFeatureEnabled("feature1"));
    }

    @Test
    public void testIsEnabledWhenFeatureDoesNotBelongToGroupButStrategy() {
        final Feature feature = createFeature("feature1", "My Feature 1", true, null, "strategy1");

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

}
