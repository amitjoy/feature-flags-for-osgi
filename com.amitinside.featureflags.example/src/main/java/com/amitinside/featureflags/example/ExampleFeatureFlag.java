/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.example;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.annotation.Feature;

@Component(name = "ExampleFeatureFlag", immediate = true)
public final class ExampleFeatureFlag extends HttpServlet {

    private static final long serialVersionUID = 6674752488720831279L;
    private static final String ALIAS = "/exampleflag";

    @Feature
    private static final String FEATURE_ID = "feature.example";

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private HttpService httpService;
    private FeatureService featureService;

    /**
     * Component activation callback
     */
    @Activate
    protected void activate() {
        try {
            httpService.registerServlet(ALIAS, this, null, new DisableAuthenticationHttpContext());
        } catch (ServletException | NamespaceException e) {
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

    /**
     * {@link FeatureService} service binding callback
     */
    @Reference
    protected void setFeatureService(final FeatureService featureService) {
        this.featureService = featureService;
    }

    /**
     * {@link FeatureService} service unbinding callback
     */
    protected void unsetFeatureService(final FeatureService featureService) {
        this.featureService = null;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        if (featureService.isFeatureEnabled(FEATURE_ID)) {
            resp.setStatus(SC_OK);
            return;
        }
        resp.setStatus(SC_FORBIDDEN);
    }

}
