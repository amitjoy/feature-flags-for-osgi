/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.featureflags;

import java.util.Optional;
import java.util.stream.Stream;

import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.feature.group.FeatureGroup;
import com.amitinside.featureflags.strategy.ActivationStrategy;
import com.google.common.base.Strings;

/**
 * The {@link FeatureService} service is the applications access point to the Feature
 * Flag functionality. It can be used to query the available features.
 *
 * @noimplement This interface is not intended to be implemented by feature providers.
 * @noextend This interface is not intended to be extended by feature providers.
 *
 * @see Feature
 * @see FeatureGroup
 * @see ActivationStrategy
 *
 * @ThreadSafe
 */
public interface FeatureService {

    /**
     * Retrieve all (known) {@link Feature}s.
     * <p>
     * {@link Feature}s are known if they are registered as {@link Feature} services or
     * are configured with OSGi configuration whose factory PID is
     * {@code com.amitinside.featureflags.feature}.
     * </p>
     *
     * @return The known {@link Feature}s
     */
    Stream<Feature> getFeatures();

    /**
     * Retrieve all (known) {@link ActivationStrategy} instances.
     * <p>
     * {@link ActivationStrategy}s are known if they are registered as {@link ActivationStrategy}
     * services
     * </p>
     *
     * @return The known {@link ActivationStrategy} instances
     */
    Stream<ActivationStrategy> getStrategies();

    /**
     * Retrieve all (known) {@link FeatureGroup} instances.
     * <p>
     * {@link FeatureGroup}s are known if they are registered as {@link FeatureGroup}
     * services
     * </p>
     *
     * @return The known {@link FeatureGroup} instances
     */
    Stream<FeatureGroup> getGroups();

    /**
     * Returns the feature with the given name.
     * <p>
     * Features are known if they are registered as {@link Feature} services or
     * are configured with OSGi configuration whose factory PID is
     * {@code com.amitinside.featureflags.feature}.
     * </p>
     *
     * @param name The name of the feature.
     * @return The feature wrapped in {@link Optional} or empty {@link Optional} instance
     *         if not known or the name is an empty string or {@code null}.
     * @throws NullPointerException if the specified argument {@code name} is {@code null}
     */
    Optional<Feature> getFeature(String name);

    /**
     * Returns the strategy with the given name.
     * <p>
     * {@link ActivationStrategy}s are known if they are registered as {@link ActivationStrategy}
     * services
     * </p>
     *
     * @param name The name of the strategy.
     * @return The strategy wrapped in {@link Optional} or empty {@link Optional} instance
     *         if not known or the name is an empty string or {@code null}.
     * @throws NullPointerException if the specified argument {@code name} is {@code null}
     */
    Optional<ActivationStrategy> getStrategy(String name);

    /**
     * Returns the group with the given name.
     * <p>
     * {@link FeatureGroup}s are known if they are registered as {@link FeatureGroup}
     * services
     * </p>
     *
     * @param name The name of the group.
     * @return The strategy wrapped in {@link Optional} or empty {@link Optional} instance
     *         if not known or the name is an empty string or {@code null}.
     * @throws NullPointerException if the specified argument {@code name} is {@code null}
     */
    Optional<FeatureGroup> getGroup(String name);

    /**
     * Creates a feature with the provided configuration.
     *
     * @param featureFactory The factory to be used to create the feature. (cannot be {@code null})
     * @return configuration PID as created wrapped in {@link Optional} or empty {@link Optional}
     *         instance if feature does not get created due to failure
     *
     * @throws NullPointerException if the specified argument {@code featureFactory} is {@code null}
     *             or the factory has been configured with a {@code null} name
     */
    Optional<String> createFeature(StrategizableFactory featureFactory);

    /**
     * Creates a group with the provided configuration.
     *
     * @param groupFactory The factory to be used to create the group. (cannot be {@code null})
     * @return configuration PID as created wrapped in {@link Optional} or empty {@link Optional}
     *         instance if feature group does not get created due to failure
     *
     * @throws NullPointerException if the specified argument {@code groupFactory} is {@code null}
     *             or the factory has been configured with a {@code null} name
     */
    Optional<String> createGroup(StrategizableFactory groupFactory);

    /**
     * Creates a property based strategy.
     *
     * @param factory The factory to be used to create the property based strategy. (cannot be {@code null})
     * @return configuration PID as created wrapped in {@link Optional} or empty {@link Optional}
     *         instance if strategy does not get created due to failure
     *
     * @throws NullPointerException if the specified argument {@code factory} is {@code null}
     *             or the factory has been configured with a {@code null} name
     */
    Optional<String> createPropertyBasedStrategy(StrategyFactory factory);

    /**
     * Removes a feature.
     *
     * @param name The name of the feature. (cannot be {@code null})
     * @return {@code true} if the named feature is known and removed by this operation.
     *         Specifically {@code false} is also returned if the named feature
     *         is not known or the operation failed to remove the feature
     *
     * @throws NullPointerException if the specified argument {@code name} is {@code null}
     */
    boolean removeFeature(String name);

    /**
     * Removes a group.
     *
     * @param name The name of the group. (cannot be {@code null})
     * @return {@code true} if the named group is known and removed by this operation.
     *         Specifically {@code false} is also returned if the named group
     *         is not known or the operation failed to remove the group
     *
     * @throws NullPointerException if the specified argument {@code name} is {@code null}
     */
    boolean removeGroup(String name);

    /**
     * Removes a property based strategy.
     *
     * @param name The name of the property based strategy. (cannot be {@code null})
     * @return {@code true} if the named strategy is known and removed by this operation.
     *         Specifically {@code false} is also returned if the named strategy
     *         is not known or the operation failed to remove the strategy
     *
     * @throws NullPointerException if the specified argument {@code name} is {@code null}
     */
    boolean removePropertyBasedStrategy(String name);

    /**
     * Updates a feature with the provided configuration.
     *
     * @param featureFactory The factory to be used to update the feature. (cannot be {@code null})
     * @return {@code true} if the named feature is known and updated by this operation.
     *         Specifically {@code false} is also returned if the named feature
     *         is not known or the operation failed to update the feature
     *
     * @throws NullPointerException if the specified argument {@code featureFactory} is {@code null}
     *             or the factory has been configured with a {@code null} name
     */
    boolean updateFeature(StrategizableFactory featureFactory);

    /**
     * Updates a group with the provided configuration.
     *
     * @param groupFactory The factory to be used to update the group. (cannot be {@code null})
     * @return {@code true} if the named group is known and updated by this operation.
     *         Specifically {@code false} is also returned if the named group
     *         is not known or the operation failed to update the group
     *
     * @throws NullPointerException if the specified argument {@code groupFactory} is {@code null}
     *             or the factory has been configured with a {@code null} name
     */
    boolean updateGroup(StrategizableFactory groupFactory);

    /**
     * Updates a property based strategy.
     *
     * @param strategyFactory The factory to be used to update the property based strategy. (cannot be {@code null})
     * @return {@code true} if the named strategy is known and updated by this operation.
     *         Specifically {@code false} is also returned if the named strategy
     *         is not known or the operation failed to update the strategy
     *
     * @throws NullPointerException if the specified argument {@code strategyFactory} is {@code null}
     *             or the factory has been configured with a {@code null} name
     */
    boolean updatePropertyBasedStrategy(StrategyFactory strategyFactory);

    /**
     * Returns {@code true} if a feature with the given name is known and
     * enabled under the feature associated strategy. The feature can belong
     * to a feature group and the groups can also specify its enablement. In
     * this case, the feature groups enablement configuration will be considered
     * as the belonging feature's configuration.
     * <p>
     * Features are known if they are registered as {@link Feature} services or
     * are configured with OSGi configuration whose factory PID is
     * {@code com.amitinside.featureflags.feature}.
     * </p>
     * <p>
     * If a feature belongs to a feature group, the feature groups configuration would
     * be considered as the feature's configuration. If not, the activation or the enablement
     * would be validated against a valid activation strategy if specified, otherwise
     * the explicitly declared enabled flag in the feature would be used.
     * </p>
     * <p>
     * If a feature belongs to multiple groups, then the enablement would be determined by the
     * active group. If there are multiple groups enabled, then the feature would by default
     * be enabled.
     * </p>
     *
     * @see <a href=
     *      "https://user-images.githubusercontent.com/13380182/32149859-65ab9eda-bd0b-11e7-9d63-c332c676f4d5.jpg">Control
     *      flow for the determination of feature enablement</a>
     *
     * @param name The name of the feature to check for enablement.
     * @return {@code true} if the named feature is known and enabled.
     *         Specifically {@code false} is also returned if the named feature
     *         is not known.
     * @throws NullPointerException if the specified argument {@code name} is {@code null}
     */
    boolean isFeatureEnabled(String name);

    /**
     * Returns {@code true} if a feature group with the given name is known and
     * enabled under the associated strategy.
     * <p>
     * Feature Groups are known if they are registered as {@link FeatureGroup} services
     * or are configured with OSGi configuration whose factory PID is
     * {@code com.amitinside.featureflags.feature.group}.
     * </p>
     * The activation or the enablement of a feature group would be validated against a
     * valid activation strategy if specified, otherwise he explicitly declared enabled flag
     * in the feature group would be used.
     *
     * @see <a href=
     *      "https://user-images.githubusercontent.com/13380182/32149859-65ab9eda-bd0b-11e7-9d63-c332c676f4d5.jpg">Control
     *      flow for the determination of feature enablement</a>
     *
     * @param name The name of the feature group to check for enablement.
     * @return {@code true} if the named feature group is known and enabled.
     *         Specifically {@code false} is also returned if the named feature group
     *         is not known.
     * @throws NullPointerException if the specified argument {@code name} is {@code null}
     */
    boolean isGroupEnabled(String name);

    /**
     * Changes the specified feature's enabled property to {@code true}.
     * <p>
     * If the feature belongs to a feature group, enabling the feature explicitly
     * would not be taken into consideration as its enablement solely depends on
     * the feature group. Likewise, if the feature doesn't belong to a feature
     * goupd rather it associates a strategy, its enablements would be solely
     * dependent on the associated strategy.
     * </p>
     *
     * @see <a href=
     *      "https://user-images.githubusercontent.com/13380182/32149859-65ab9eda-bd0b-11e7-9d63-c332c676f4d5.jpg">Control
     *      flow for the determination of feature enablement</a>
     *
     * @param name the name of the feature
     * @return {@code true} if the named feature is known and the operation succeeded.
     *         Specifically {@code false} is also returned if the named feature
     *         is not known or the operation is unsuccessful
     * @throws NullPointerException if the specified argument {@code name} is {@code null}
     */
    boolean enableFeature(String name);

    /**
     * Changes the specified feature's enabled property to {@code false}.
     * <p>
     * If the feature belongs to a feature group, enabling the feature explicitly
     * would not be taken into consideration as its enablement solely depends on
     * the feature group. Likewise, if the feature doesn't belong to a feature
     * group rather it associates a strategy, its enablement would be solely
     * dependent on the associated strategy.
     * </p>
     *
     * @see <a href=
     *      "https://user-images.githubusercontent.com/13380182/32149859-65ab9eda-bd0b-11e7-9d63-c332c676f4d5.jpg">Control
     *      flow for the determination of feature enablement</a>
     *
     * @param name the name of the feature
     * @return {@code true} if the named feature is known and the operation succeeded.
     *         Specifically {@code false} is also returned if the named feature
     *         is not known or the operation is unsuccessful
     * @throws NullPointerException if the specified argument {@code name} is {@code null}
     */
    boolean disableFeature(String name);

    /**
     * Changes the specified feature group's enabled property to {@code true}.
     * <p>
     * If the feature group associates a strategy, its enablement would be solely
     * dependent on the associated strategy.
     * </p>
     *
     * @see <a href=
     *      "https://user-images.githubusercontent.com/13380182/32149859-65ab9eda-bd0b-11e7-9d63-c332c676f4d5.jpg">Control
     *      flow for the determination of feature enablement</a>
     *
     * @param name the name of the feature group
     * @return {@code true} if the named feature is known and the operation succeeded.
     *         Specifically {@code false} is also returned if the named feature
     *         is not known or the operation is unsuccessful
     * @throws NullPointerException if the specified argument {@code name} is {@code null}
     */
    boolean enableGroup(String name);

    /**
     * Changes the specified feature group's enabled property to {@code false}.
     * <p>
     * If the feature group associates a strategy, its enablement would be solely
     * dependent on the associated strategy.
     * </p>
     *
     * @see <a href=
     *      "https://user-images.githubusercontent.com/13380182/32149859-65ab9eda-bd0b-11e7-9d63-c332c676f4d5.jpg">Control
     *      flow for the determination of feature enablement</a>
     *
     * @param name the name of the feature group
     * @return {@code true} if the named feature group is known and the operation succeeded.
     *         Specifically {@code false} is also returned if the named feature group
     *         is not known or the operation is unsuccessful
     * @throws NullPointerException if the specified argument {@code name} is {@code null}
     */
    boolean disableGroup(String name);

    /*
     * ========================================================
     * Default Utility Methods (Not required to be implemented)
     * ========================================================
     */

    /**
     * Retrieve all (known) {@link Feature}s associated with the specified group
     *
     * @param name the group name
     * @return The known {@link Feature}s
     */
    default Stream<Feature> getFeaturesByGroup(final String name) {
        return Strings.isNullOrEmpty(name) ? Stream.empty()
                : getFeatures().filter(f -> f.getGroups().anyMatch(g -> g.equalsIgnoreCase(name)));
    }

    /**
     * Retrieve all (known) {@link Feature}s associated with the specified strategy
     *
     * @param name the strategy name
     * @return The known {@link Feature}s
     */
    default Stream<Feature> getFeaturesByStrategy(final String name) {
        return Strings.isNullOrEmpty(name) ? Stream.empty()
                : getFeatures().filter(f -> f.getStrategy().orElse("").equalsIgnoreCase(name));
    }

    /**
     * Retrieve all (known) {@link FeatureGroup}s associated with the specified strategy
     *
     * @param name the strategy name
     * @return The known {@link FeatureGroup}s
     */
    default Stream<FeatureGroup> getGroupsByStrategy(final String name) {
        return Strings.isNullOrEmpty(name) ? Stream.empty()
                : getGroups().filter(f -> f.getStrategy().orElse("").equalsIgnoreCase(name));
    }

}