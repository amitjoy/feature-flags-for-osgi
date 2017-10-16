/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.feature.group.FeatureGroup;

/**
 * A Configuration Event.
 *
 * <p>
 * {@code ConfigurationEvent} objects are delivered to all registered
 * {@code ConfigurationListener} service objects. ConfigurationEvents
 * must be asynchronously delivered in chronological order with respect to each
 * listener.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by feature providers.
 * @noextend This interface is not intended to be extended by feature providers.
 *
 * @see Feature
 * @see FeatureGroup
 * @see Strategizable
 */
public class ConfigurationEvent {

    /**
     * Type of this event.
     *
     * @see #getType
     */
    private final Type type;

    /**
     * The service instance which created this event.
     */
    private final Strategizable service;

    /**
     * The service properties of the associated {@link Strategizable} instance
     */
    private final Map<String, Object> properties;

    /**
     * Constructs a <code>ConfigurationEvent</code> object from the given
     * <code>ServiceReference</code> object, event type, and pids.
     *
     * @param type The event type. See {@link #getType}.
     * @param reference The {@link Strategizable} reference.
     * @param properties The service properties of the associated {@link Strategizable}
     * @throws NullPointerException if any of the specified arguments is {@code null}
     */
    public ConfigurationEvent(final Type type, final Strategizable reference, final Map<String, Object> properties) {
        requireNonNull(type, "Event type cannot be null");
        requireNonNull(reference, "Event associated instance cannot be null");
        requireNonNull(properties, "Service properties cannot be null");

        this.type = type;
        service = reference;
        this.properties = properties;
    }

    /**
     * Return the type of this event.
     *
     * @return The type of this event.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the associated {@link Strategizable} instance
     *
     * @return associated {@link Strategizable} instance
     */
    public Strategizable getReference() {
        return service;
    }

    /**
     * Returns the associated {@link Strategizable} instance's service properties
     *
     * @return service properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    public enum Type {
        DELETED,
        UPDATED
    }
}
