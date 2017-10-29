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

import static com.amitinside.featureflags.annotation.ExpirationType.TIMED;
import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.tools.Diagnostic.Kind.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.amitinside.featureflags.annotation.Feature;

@SupportedAnnotationTypes("com.amitinside.featureflags.annotation.Feature")
@SupportedSourceVersion(RELEASE_8)
public final class FeatureAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final Messager messager = processingEnv.getMessager();
        messager.printMessage(NOTE, "\n\n");
        messager.printMessage(NOTE, "Starting Feature Expiration Evaluatation");
        messager.printMessage(NOTE, "======================================================== ");
        messager.printMessage(NOTE, "Processing(...) in " + this.getClass().getSimpleName());
        messager.printMessage(NOTE, "======================================================== ");

        for (final Element element : roundEnv.getElementsAnnotatedWith(Feature.class)) {
            final Feature feature = element.getAnnotation(Feature.class);
            if (feature.expirationType().equals(TIMED)) {
                try {
                    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(feature.expirationDatePattern());
                    final LocalDate date = LocalDate.parse(feature.expirationDate(), formatter);
                    final LocalDate now = LocalDate.now();
                    if (date.isBefore(now)) {
                        messager.printMessage(WARNING, feature.expirationMessage(), element);
                    }
                } catch (final DateTimeParseException | IllegalArgumentException ex) {
                    messager.printMessage(WARNING, "Cannot parse expiration date", element);
                }
            }
        }
        return true;
    }

}
