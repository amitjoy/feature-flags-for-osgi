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

import static com.amitinside.featureflags.internal.TestHelper.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.feature.group.FeatureGroup;
import com.amitinside.featureflags.util.ServiceHelper;
import com.google.common.collect.Lists;

@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(MockitoJUnitRunner.class)
public final class ServiceHelperTest {

    @Mock
    private BundleContext context;
    @Mock
    private ServiceReference reference;

    @Test(expected = InvocationTargetException.class)
    public void testObjectConstruction() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        final Class<?> clazz = Class.forName(ServiceHelper.class.getName());
        final List<Constructor<?>> constrctors = Lists.newArrayList(clazz.getDeclaredConstructors());
        if (!constrctors.isEmpty()) {
            final Constructor<?> constructor = constrctors.get(0);
            constructor.setAccessible(true);
            constructor.newInstance((Object[]) null);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgument1() {
        ServiceHelper.getServiceProperties(null, null, null, null);
    }

    @Test
    public void testServicePropertiesWhenInstanceAvailable() throws InvalidSyntaxException {
        final Feature feature = createFeature("feature1", "My Feature 1", true, "group1", "strategy1");

        doReturn(new ServiceReference[] { reference }).when(context).getServiceReferences(Feature.class.getName(),
                null);
        doReturn(feature).when(context).getService(reference);
        final List<String> propKeys = Lists.newArrayList("service.id", "service.ranking", "service.pid");
        doReturn(propKeys.toArray(new String[0])).when(reference).getPropertyKeys();
        doReturn(5).when(reference).getProperty("service.id");
        doReturn(2).when(reference).getProperty("service.ranking");
        doReturn("pid").when(reference).getProperty("service.pid");

        final Map<String, Object> props = ServiceHelper.getServiceProperties(context, feature, Feature.class, null);
        assertEquals("pid", props.get("service.pid"));
        assertEquals(5, props.get("service.id"));
        assertEquals(2, props.get("service.ranking"));
    }

    @Test
    public void testServicePropertiesWhenInstanceUnavailable1() throws InvalidSyntaxException {
        final Feature feature = createFeature("feature1", "My Feature 1", true, "group1", "strategy1");
        final FeatureGroup group = createFeatureGroup("group1", "My Group 1", true, "strategy1");

        doReturn(new ServiceReference[] { reference }).when(context).getServiceReferences(Feature.class.getName(),
                null);
        doReturn(group).when(context).getService(reference);

        final Map<String, Object> props = ServiceHelper.getServiceProperties(context, feature, Feature.class, null);
        assertTrue(props.isEmpty());
    }

    @Test
    public void testServicePropertiesWhenInstanceUnavailable2() throws InvalidSyntaxException {
        final Feature feature = createFeature("feature1", "My Feature 1", true, "group1", "strategy1");

        doReturn(new ServiceReference[0]).when(context).getServiceReferences(Feature.class.getName(), null);

        final Map<String, Object> props = ServiceHelper.getServiceProperties(context, feature, Feature.class, null);
        assertTrue(props.isEmpty());
    }

    @Test
    public void testServicePropertiesInvalidSyntaxException() throws InvalidSyntaxException {
        final Feature feature = createFeature("feature1", "My Feature 1", true, "group1", "strategy1");

        doThrow(InvalidSyntaxException.class).when(context).getServiceReferences(Feature.class.getName(), null);

        final Map<String, Object> props = ServiceHelper.getServiceProperties(context, feature, Feature.class, null);
        assertTrue(props.isEmpty());
    }

}
