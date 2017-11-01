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

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "FeatureFlagsServletsController", immediate = true)
public final class FeatureFlagsServletsController {

    private static final String ALIAS = "/featureflags";

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private HttpService httpService;

    /**
     * Component activation callback
     */
    @Activate
    protected void activate() {
        try {
            httpService.registerResources(ALIAS, "/files", new DisableAuthenticationHttpContext());
        } catch (final NamespaceException e) {
            logger.error("{}", e.getMessage(), e);
        }
    }

    /**
     * Component activation callback
     */
    @Deactivate
    protected void deactivate() {
        httpService.unregister(ALIAS);
    }

    /**
     * {@link HttpService} service binding callback
     */
    @Reference
    protected void setHttpService(final HttpService httpService) {
        this.httpService = httpService;
    }

    /**
     * {@link HttpService} service unbinding callback
     */
    protected void unsetHttpService(final HttpService httpService) {
        this.httpService = null;
    }

}
