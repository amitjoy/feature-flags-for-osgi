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

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public final class FeatureServiceTest {

    @Test
    public void testA() {
        final FeatureManager f = new FeatureManager();
        assertNotNull(f);
    }

}
