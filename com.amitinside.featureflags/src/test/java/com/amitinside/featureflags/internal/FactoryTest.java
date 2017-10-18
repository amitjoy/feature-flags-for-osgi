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

import org.junit.Test;

import com.amitinside.featureflags.Factory;

public final class FactoryTest {

    @Test(expected = NullPointerException.class)
    public void testNullFunction() {
        Factory.make("dummy", null);
    }

}
