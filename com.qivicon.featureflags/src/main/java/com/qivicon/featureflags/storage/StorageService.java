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
package com.qivicon.featureflags.storage;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * {@code StorageService} is primarily used to provide a generic way to storing
 * and retrieving data. This can be implemented and exposed as OSGi
 * services.
 * <p>
 * This interface is intended to be implemented by feature providers.
 * </p>
 */
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
