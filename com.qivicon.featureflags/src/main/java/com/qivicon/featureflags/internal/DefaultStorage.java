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

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import com.qivicon.featureflags.storage.StorageService;

/**
 * The in-memory default storage implementation
 */
public final class DefaultStorage implements StorageService {

    private final Map<String, String> storage = Maps.newHashMap();

    @Override
    public Optional<String> get(final String key) {
        requireNonNull(key, "Key cannot be null");
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public void put(final String key, final String value) {
        requireNonNull(key, "Key cannot be null");
        requireNonNull(value, "Value cannot be null");

        storage.put(key, value);
    }

    @Override
    public void remove(final String key) {
        requireNonNull(key, "Key cannot be null");
        storage.remove(key);
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public Stream<String> keys() {
        return storage.keySet().stream();
    }

}
