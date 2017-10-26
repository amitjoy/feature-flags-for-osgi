/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.web.servlets;

import static javax.servlet.http.HttpServletResponse.*;
import static org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.web.FeatureFlagsServlet;
import com.amitinside.featureflags.web.util.RequestHelper;
import com.amitinside.featureflags.web.util.RequestHelper.FeatureData;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

@Component(name = "FeaturesByStrategyServlet", immediate = true)
public final class FeaturesByStrategyServlet extends HttpServlet implements FeatureFlagsServlet {

    private static final String ALIAS = "featuresByStrategy";
    private static final long serialVersionUID = 7683703693369965631L;

    /** Logger Instance */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FeatureService featureService;
    private final Gson gson = new Gson();
    private final Map<Feature, Map<String, Object>> featureProperties = Maps.newHashMap();

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) {
        final List<String> uris = RequestHelper.parseFullUrl(req);
        if (uris.size() == 2 && uris.get(0).equalsIgnoreCase(ALIAS)) {
            //@formatter:off
            final List<FeatureData> data = featureService.getFeaturesByStrategy(uris.get(1))
                                                     .map(f -> RequestHelper.mapToFeatureData(f, featureProperties))
                                                     .collect(Collectors.toList());
            //@formatter:on
            final String json = gson.toJson(new DataHolder(data));
            if (json == null || json.equalsIgnoreCase("null")) {
                resp.setStatus(SC_NO_CONTENT);
                return;
            }
            try (final PrintWriter writer = resp.getWriter()) {
                resp.setStatus(SC_OK);
                writer.write(json);
            } catch (final IOException e) {
                logger.error("{}", e.getMessage(), e);
                resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            }
        }
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

    /**
     * {@link Feature} service binding callback
     */
    @Reference(cardinality = MULTIPLE, policy = DYNAMIC)
    protected void bindFeature(final Feature feature, final Map<String, Object> props) {
        featureProperties.put(feature, props);
    }

    /**
     * {@link Feature} service unbinding callback
     */
    protected void unbindFeature(final Feature feature, final Map<String, Object> props) {
        featureProperties.remove(feature);
    }

    private static final class DataHolder {
        private final List<FeatureData> features;

        public DataHolder(final List<FeatureData> features) {
            this.features = features;
        }

        @SuppressWarnings("unused")
        public List<FeatureData> getFeatures() {
            return features;
        }
    }

    @Override
    public String getAlias() {
        return "/" + ALIAS;
    }
}
