/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Amit Kumar Mondal
 *
 *******************************************************************************/
package com.amitinside.featureflags.listener;

import com.amitinside.featureflags.ConfigurationEvent;

/**
 * Listener for Configuration Events. When a {@code ConfigurationEvent} is
 * fired, it is asynchronously delivered to all {@code ConfigurationListener}s.
 * <p>
 * {@code ConfigurationListener} objects are registered with the Framework
 * service registry and are notified with a {@code ConfigurationEvent} object
 * when an event is fired.
 * </p>
 * {@code ConfigurationListener} objects can inspect the received
 * {@code ConfigurationEvent} object to determine several properties.
 * <p>
 * This interface is intended to be implemented by feature providers.
 * </p>
 * 
 * @ThreadSafe
 */
public interface ConfigurationListener {
    /**
     * Receives notification of a Configuration that has changed.
     *
     * @param event The {@code ConfigurationEvent}.
     */
    public void onEvent(ConfigurationEvent event);
}