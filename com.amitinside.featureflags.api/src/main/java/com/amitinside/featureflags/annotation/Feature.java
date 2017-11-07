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
 * annotation processor would be used to show warning messages during source build time if any such feature
 * is expired.
 *
 * @see ExpirationType
 */
@Retention(SOURCE)
@Target(FIELD)
public @interface Feature {

    /**
     * The expiration type of this feature.
     *
     * <p>
     * If not specified, the expiration type of this feature will be considered
     * as a feature that can never be expired
     * </p>
     *
     * @see ExpirationType
     */
    ExpirationType expirationType() default NEVER;

    /**
     * The expiration date of this feature.
     *
     * <p>
     * If not specified, the expiration date of this Component will be considered
     * as empty string
     * </p>
     */
    String expirationDate() default "";

    /**
     * The expiration date input pattern of this feature.
     *
     * <p>
     * If not specified, the input pattern of this feature's expiration date will be
     * considered as a feature that can never be expired {@code yyyy MM dd}
     * </p>
     */
    String expirationDatePattern() default "yyyy MM dd";

    /**
     * The expiration message this feature. This will be shown by the annotation
     * processor if the annotated feature is expired.
     *
     * <p>
     * If not specified, the expiration message of this feature will be
     * {@code Feature is expired}
     * </p>
     */
    String expirationMessage() default "Feature is expired";
}