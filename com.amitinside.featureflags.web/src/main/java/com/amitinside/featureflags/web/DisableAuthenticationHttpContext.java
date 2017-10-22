/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.web;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This extends a {@link HttpContext} to enable servlets and resources
 * to run outside the authorization scope of the default {@link HttpService}
 * Context.
 */
public final class DisableAuthenticationHttpContext implements HttpContext {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Bundle bundle = FrameworkUtil.getBundle(getClass());

    @Override
    public boolean handleSecurity(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        return true;
    }

    @Override
    public URL getResource(final String name) {
        try {
            return bundle.getResource(name);
        } catch (final Exception ex) {
            logger.error("Could not find artifact: {}", name, ex);
            return null;
        }
    }

    @Override
    public String getMimeType(final String name) {
        return null;
    }
}
