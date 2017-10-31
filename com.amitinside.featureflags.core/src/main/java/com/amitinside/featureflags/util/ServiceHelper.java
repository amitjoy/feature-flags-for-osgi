/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.util;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Service Helper Class
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class ServiceHelper {

    /** Constructor */
    private ServiceHelper() {
        throw new IllegalAccessError("Non-Instantiable");
    }

    /**
     * Returns the specified instance's OSGi service properties
     *
     * @param context The bundle context instance
     * @param actualServiceInstance The service instance whose service properties will be retrieved.
     * @param serviceClazz The service class to look for in the OSGi service registry.
     * @param filter The filter expression or {@code null} for all services.
     * @return the service properties or empty {@link Map} instance (immutable view)
     *
     * @throws NullPointerException if {@code actualServiceInstance} or {@code serviceClazz} is {@code null}
     */
    public static <S, T> Map<String, Object> getServiceProperties(final BundleContext context,
            final S actualServiceInstance, final Class<T> serviceClazz, final String filter) {
        requireNonNull(context, "Bundle Context cannot be null");
        requireNonNull(actualServiceInstance, "Service Instance cannot be null");
        requireNonNull(serviceClazz, "Service Class cannot be null");
        final List<ServiceReference> refs = Lists.newArrayList();
        final Map<String, Object> props = Maps.newHashMap();
        try {
            final ServiceReference[] references = context.getServiceReferences(serviceClazz.getName(), filter);
            for (final ServiceReference reference : references) {
                final S s = (S) context.getService(reference);
                refs.add(reference);
                if (s == actualServiceInstance) {
                    for (final String key : reference.getPropertyKeys()) {
                        props.put(key, reference.getProperty(key));
                    }
                    return props;
                }
            }
        } catch (final InvalidSyntaxException | IllegalStateException e) {
            // not required
        } finally {
            refs.forEach(context::ungetService);
        }
        return ImmutableMap.copyOf(props);
    }

}
