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

import static org.junit.Assert.*;

import java.net.URL;

import javax.tools.JavaFileObject;

import org.junit.Test;

import com.google.common.io.Resources;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;

public final class AnnotationProcessorTest {

    @Test
    public void testFeatureAnnotationNoExpiry() {
        final URL path = Resources.getResource("FeatureWithoutExpiry.java");
        final JavaFileObject fileObject = JavaFileObjects.forResource(path);
        final Compilation compilation = com.google.testing.compile.Compiler.javac()
                .withProcessors(new FeatureAnnotationProcessor()).compile(fileObject);
        assertTrue(compilation.warnings().isEmpty());
    }

    @Test
    public void testFeatureAnnotationWithExpiryButDateParseException() {
        final URL path = Resources.getResource("FeatureWithExpiryButParseException.java");
        final JavaFileObject fileObject = JavaFileObjects.forResource(path);
        final Compilation compilation = com.google.testing.compile.Compiler.javac()
                .withProcessors(new FeatureAnnotationProcessor()).compile(fileObject);
        assertFalse(compilation.warnings().isEmpty());
    }

    @Test
    public void testFeatureAnnotationWithExpiry() {
        final URL path = Resources.getResource("FeatureWithExpiry.java");
        final JavaFileObject fileObject = JavaFileObjects.forResource(path);
        final Compilation compilation = com.google.testing.compile.Compiler.javac()
                .withProcessors(new FeatureAnnotationProcessor()).compile(fileObject);
        assertFalse(compilation.warnings().isEmpty());
    }

    @Test
    public void testFeatureAnnotationWithExpiryButNotExpired() {
        final URL path = Resources.getResource("FeatureWithExpiryButNotExpired.java");
        final JavaFileObject fileObject = JavaFileObjects.forResource(path);
        final Compilation compilation = com.google.testing.compile.Compiler.javac()
                .withProcessors(new FeatureAnnotationProcessor()).compile(fileObject);
        assertTrue(compilation.warnings().isEmpty());
    }

}
