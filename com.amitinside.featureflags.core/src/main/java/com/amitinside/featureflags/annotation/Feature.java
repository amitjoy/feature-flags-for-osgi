/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.annotation;

import static com.amitinside.featureflags.annotation.ExpirationType.NEVER;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is primarily used to annotate field that contains the feature identifier to be used
 * in the source code. This is introduced to ease the effort to clean up obsolete features. The packaged
 * annotation processor would be used to show warning messages if any such feature is expired.
 */
@Retention(SOURCE)
@Target(FIELD)
public @interface Feature {

    ExpirationType expirationType() default NEVER;

    String expirationDate() default "";

    String expirationDatePattern() default "yyyy MM dd";

    String expirationMessage() default "Feature is expired";
}