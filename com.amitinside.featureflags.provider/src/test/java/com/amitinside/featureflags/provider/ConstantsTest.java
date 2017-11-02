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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Test;

import com.amitinside.featureflags.Constants;
import com.google.common.collect.Lists;

public final class ConstantsTest {

    @Test(expected = InvocationTargetException.class)
    public void testObjectConstruction() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        final Class<?> clazz = Class.forName(Constants.class.getName());
        final List<Constructor<?>> constrctors = Lists.newArrayList(clazz.getDeclaredConstructors());
        if (!constrctors.isEmpty()) {
            final Constructor<?> constructor = constrctors.get(0);
            constructor.setAccessible(true);
            constructor.newInstance((Object[]) null);
        }
    }

}
