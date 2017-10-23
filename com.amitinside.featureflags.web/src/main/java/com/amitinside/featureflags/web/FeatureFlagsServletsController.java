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

import static org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

@Component(name = "FeatureFlagsServletsController", immediate = true)
public final class FeatureFlagsServletsController {

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<FeatureFlagsServlet> servlets = Lists.newCopyOnWriteArrayList();
    private HttpService httpService;

    /**
     * {@link FeatureFlagsServlet} service binding callback
     */
    @Reference(cardinality = MULTIPLE, policy = DYNAMIC)
    protected void bindServlet(final FeatureFlagsServlet servlet) {
        servlets.add(servlet);
        try {
            httpService.registerServlet(servlet.getAlias(), (Servlet) servlet, null,
                    new DisableAuthenticationHttpContext());
        } catch (final ServletException | NamespaceException e) {
            logger.error("{}", e.getMessage(), e);
        }
    }

    /**
     * {@link FeatureFlagsServlet} service unbinding callback
     */
    protected void unbindServlet(final FeatureFlagsServlet servlet) {
        servlets.remove(servlet);
        httpService.unregister(servlet.getAlias());
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