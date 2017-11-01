/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.rest;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.amitinside.featureflags.FeatureService;
import com.amitinside.featureflags.rest.util.RequestHelper;
import com.amitinside.featureflags.rest.util.RequestHelper.GroupData;
import com.google.gson.Gson;

import osgi.enroute.rest.api.REST;
import osgi.enroute.rest.api.RESTRequest;

@Component(name = "GroupsByStrategyRESTResource", immediate = true)
public final class GroupsByStrategyRESTResource implements REST {

    private FeatureService featureService;
    private final Gson gson = new Gson();

    public String getGroups(final RESTRequest req, final String name) {
        //@formatter:off
        final List<GroupData> data = featureService.getGroupsByStrategy(name)
                                            .map(RequestHelper::mapToGroupData)
                                            .collect(Collectors.toList());
        //@formatter:on
        final String json = gson.toJson(new DataHolder(data));
        checkArgument(json == null || json.equalsIgnoreCase("null"));
        return json;
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

    private static final class DataHolder {
        private final List<GroupData> groups;

        public DataHolder(final List<GroupData> groups) {
            this.groups = groups;
        }

        @SuppressWarnings("unused")
        public List<GroupData> getGroups() {
            return groups;
        }
    }
}
