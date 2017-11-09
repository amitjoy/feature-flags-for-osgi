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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.amitinside.featureflags.example.ExampleFeatureFlag.MyConfig;
import com.amitinside.featureflags.util.Configurable;

@Designate(ocd = MyConfig.class)
@Component(name = "ExampleFeatureFlag", immediate = true)
public final class ExampleFeatureFlag extends HttpServlet {

    private static final String ALIAS = "/exampleflag";

    private static final long serialVersionUID = 6674752488720831279L;

    private HttpService httpService;
    private MyConfig config;

    @ObjectClassDefinition(id = "feature.flag.example")
    @interface MyConfig {
        @AttributeDefinition(description = "My Feature Description")
        boolean osgi_feature_myfeature() default true;
    }

    /**
     * Component activation callback
     */
    @Activate
    protected void activate(final Map<String, Object> properties) {
        modified(properties);
        try {
            httpService.registerServlet(ALIAS, this, null, new DisableAuthenticationHttpContext());
        } catch (ServletException | NamespaceException e) {
            // not required
        }
    }

    /**
     * Component activation callback
     */
    @Modified
    protected void modified(final Map<String, Object> properties) {
        config = Configurable.createConfigurable(MyConfig.class, properties);
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

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        if (config.osgi_feature_myfeature()) {
            resp.setStatus(SC_OK);
            return;
        }
        resp.setStatus(SC_FORBIDDEN);
    }

}
