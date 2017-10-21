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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.amitinside.featureflags.storage.StorageService;

public final class DefaultStorageTest {

    private final StorageService storage = new DefaultStorage();

    @Test
    public void testStorage() {
        assertEquals(storage.keys().count(), 0);

        storage.put("a", "b");
        assertEquals(storage.get("a").get(), "b");

        storage.remove("a");
        assertEquals(storage.keys().count(), 0);

        storage.put("a", "b");
        storage.put("c", "d");
        assertEquals(storage.keys().count(), 2);

        storage.clear();
        assertEquals(storage.keys().count(), 0);
    }

    @Test(expected = NullPointerException.class)
    public void testGetNullArgument() {
        storage.get(null);
    }

    @Test(expected = NullPointerException.class)
    public void testPutNullArgument() {
        storage.put(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveNullArgument() {
        storage.remove(null);
    }

}
