/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.storage;

import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * {@code StorageService} is primarily used to provide a generic way of storing
 * and retrieving data. This can be implemented and exposed as OSGi service.
 * This is primarily used by the JSON Resource processor.
 * <p>
 * The JSON Resources are read as soon as the containing bundle is in ACTIVE state
 * and the features and groups are created as services thereafter. And when these
 * JSON resource containing bundles get uninstalled, the registered services must
 * not be removed from the runtime. By nature these persistence of these configured
 * services are managed by OSGi runtime but we require to persist the names of these
 * services so that by restart of these bundles, the services will not be registered
 * again. That is why, to persist already registered service names, {@code StorageService}
 * is used. This interface is left as simple as possible so that anyone can provide
 * their own implementation of managing persistence. There is also a default
 * implementation of {@code StorageService} that uses an in-memory storage structure.
 * </p>
 *
 * <p>
 * This interface is intended to be implemented by feature providers.
 * </p>
 */
@ConsumerType
public interface StorageService {

    /**
     * Returns the value associated with the specified {@code key} in this
     * node. Returns the specified default if there is no value associated with
     * the {@code key}, or the backing store is inaccessible.
     *
     * @param key key whose associated value is to be returned.
     * @param def the value to be returned in the event that this node has no
     *            value associated with {@code key} or the backing store is
     *            inaccessible.
     * @return the value associated with {@code key} wrapped in {@link Optional},
     *         or empty {@link Optional}
     * @throws NullPointerException if {@code key} is {@code null}.
     */
    public Optional<String> get(String key);

    /**
     * Associates the specified value with the specified {@code key} in this node.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @throws NullPointerException if {@code key} or {@code value} is
     *             {@code null}.
     */
    public void put(String key, String value);

    /**
     * Removes the value associated with the specified {@code key} in this
     * node, if any.
     *
     * @param key key whose mapping is to be removed from this node.
     * @see #get(String,String)
     */
    public void remove(String key);

    /**
     * Removes all of the properties (key-value associations).
     *
     * @see #remove(String)
     */
    public void clear();

    /**
     * Returns all of the keys that have an associated value in this node. (The
     * returned array will be of size zero if this node has no preferences and
     * not <code>null</code>!)
     *
     * @return an array of the keys that have an associated value in this node.
     */
    public Stream<String> keys();

}
