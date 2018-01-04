/*******************************************************************************
 * Copyright (c) 2017-2018 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.provider;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Test;

public final class ManagerHelperTest {

    @Test(expected = InvocationTargetException.class)
    public void testHelperInstantiation() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        final Constructor<ManagerHelper> constructor = ManagerHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void testAsList() {
        final List<String> list1 = ManagerHelper.asList(null);

        assertNotNull(list1);
        assertTrue(list1.isEmpty());

        final List<String> list2 = ManagerHelper.asList(new String[] { "a" });

        assertNotNull(list2);
        assertEquals("a", list2.get(0));
    }

}
