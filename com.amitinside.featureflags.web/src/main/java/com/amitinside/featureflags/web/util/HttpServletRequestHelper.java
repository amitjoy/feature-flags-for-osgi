/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags.web.util;

import static java.util.Objects.requireNonNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Lists;

public final class HttpServletRequestHelper {

    /** Constructor */
    private HttpServletRequestHelper() {
        throw new IllegalAccessError("Non-Instantiable");
    }

    /**
     * This exists because HttpServletRequest.getParameterMap() returns an String[] instead
     */
    public static Map<String, String> getParameterMap(final HttpServletRequest request) {
        requireNonNull(request, "Servlet Request cannot be null");
        final Map<String, String> m = new LinkedHashMap<>();
        for (final Enumeration<?> e = request.getParameterNames(); e.hasMoreElements();) {
            final String name = (String) e.nextElement();
            final String value = request.getParameter(name);
            m.put(name, value);
        }
        return m;
    }

    public static Map<String, String> getHeaderMap(final HttpServletRequest request) {
        requireNonNull(request, "Servlet Request cannot be null");
        final Map<String, String> m = new LinkedHashMap<>();
        for (final Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements();) {
            final String name = (String) e.nextElement();
            final String value = request.getHeader(name);
            m.put(name, value);
        }
        return m;
    }

    public static Map<String, String> getCookieMap(final HttpServletRequest request) {
        requireNonNull(request, "Servlet Request cannot be null");
        final Map<String, String> m = new LinkedHashMap<>();
        final Cookie[] ca = request.getCookies();
        if (ca != null) {
            for (final Cookie c : ca) {
                final String name = c.getName();
                final String value = c.getValue();
                m.put(name, value);
            }
        }
        return m;
    }

    public static List<String> parseFullUrl(final HttpServletRequest request) {
        requireNonNull(request, "Servlet Request cannot be null");
        final String pathAfterContext = request.getRequestURI()
                .substring(request.getContextPath().length() + request.getServletPath().length() + 1);
        final List<String> parts = Lists.newArrayList();
        for (final String val : pathAfterContext.split("/")) {
            try {
                parts.add(URLDecoder.decode(val, "UTF-8"));
            } catch (final UnsupportedEncodingException e) {
                return parts;
            }
        }
        final String query = request.getQueryString();
        if (query != null) {
            for (final String val : query.split("&")) {
                try {
                    parts.add(URLDecoder.decode(val, "UTF-8"));
                } catch (final UnsupportedEncodingException e) {
                    return parts;
                }
            }
        }
        return parts;
    }
}