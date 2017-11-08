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

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.osgi.annotation.versioning.ProviderType;

import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.feature.group.FeatureGroup;

/**
 * {@link StrategizableFactory} is used to create configuration factories for {@link Feature} or {@link FeatureGroup}.
 * This is primarily used by {@link FeatureManager} to create {@link Feature} and {@link FeatureGroup}
 * configuration instances.
 *
 * <pre>
 * final StrategizableFactory groupFactory = StrategizableFactory.make("group1", g -> g.withDescription("My Group 1")
 *                                                               .withStrategy("strategy1")
 *                                                               .withProperties(props)
 *                                                               .withEnabled(false)
 *                                                               .build());
 *                                                           
 * final StrategizableFactory featureFactory = StrategizableFactory.make("feature1", f -> f.withDescription("My Feature 1")
 *                                                                 .withStrategy("strategy1")
 *                                                                 .withGroups(Lists.newArrayList("group1"))
 *                                                                 .withProperties(props)
 *                                                                 .withEnabled(false)
 *                                                                 .build());
 * </pre>
 *
 * @noimplement This interface is not intended to be implemented by feature providers.
 * @noextend This interface is not intended to be extended by feature providers.
 *
 * @see Feature
 * @see FeatureGroup
 * @see Strategizable
 *
 * @ThreadSafe
 * @Immutable
 */
@ProviderType
public class StrategizableFactory {

    private final String name;
    private final String description;
    private final String strategy;
    private final boolean isEnabled;
    private final List<String> groups;
    private final Map<String, Object> properties;

    /**
     * Constructs a Factory instance
     *
     * @param builder the {@link Builder} instance
     *
     * @throws NullPointerException NullPointerException if the {@link Builder}
     *             or specified {@code name} is {@code null}
     */
    private StrategizableFactory(final Builder builder) {
        requireNonNull(builder, "Builder cannot be null");
        name = requireNonNull(builder.name, "Name cannot be null");
        description = builder.description;
        strategy = builder.strategy;
        groups = builder.groups;
        properties = builder.properties;
        isEnabled = builder.isEnabled;
    }

    private static class Builder implements BuilderWithDescription, BuilderWithStrategy, BuilderWithProperties,
            BuilderWithEnabled, BuilderWithGroups, FinalStep, FactoryFinalizationStep {
        private final String name;
        private String description;
        private String strategy;
        private Map<String, Object> properties;
        private boolean isEnabled;
        private List<String> groups;

        public Builder(final String name) {
            this.name = name;
        }

        @Override
        public Builder withDescription(final String description) {
            this.description = description;
            return this;
        }

        @Override
        public Builder withStrategy(final String strategy) {
            this.strategy = strategy;
            return this;
        }

        @Override
        public Builder withProperties(final Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        @Override
        public Builder withEnabled(final boolean isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }

        @Override
        public StrategizableFactory create() {
            return new StrategizableFactory(this);
        }

        @Override
        public Builder withGroups(final List<String> groups) {
            this.groups = groups;
            return this;
        }

        @Override
        public FactoryFinalizationStep build() {
            return this;
        }
    }

    /**
     * Returns configured enabled property (can be {@code null})
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Returns configured name (can be {@code null})
     */
    public String getName() {
        return name;
    }

    /**
     * Returns configured description
     */
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    /**
     * Returns configured strategy
     */
    public Optional<String> getStrategy() {
        return Optional.ofNullable(strategy);
    }

    /**
     * Returns configured groups
     *
     * return a list of group names or an empty list
     */
    public List<String> getGroups() {
        return groups == null ? Collections.emptyList() : Collections.unmodifiableList(groups);
    }

    /**
     * Creates a {@link StrategizableFactory} instance
     *
     * @param name the name of the instance
     * @param configuration the function representing the instance configuration
     * @return the {@link StrategizableFactory} instance
     * @throws NullPointerException if any of the specified arguments is {@code null}
     */
    public static StrategizableFactory make(final String name,
            final Function<BuilderWithDescription, FactoryFinalizationStep> configuration) {
        requireNonNull(configuration, "Function cannot be null");
        return configuration.andThen(FactoryFinalizationStep::create).apply(new Builder(name));
    }

    /**
     * Returns configured service properties (cannot be {@code null})
     *
     * return the specified service properties or an empty map
     */
    public Map<String, Object> getProperties() {
        return properties == null ? Collections.emptyMap() : Collections.unmodifiableMap(properties);
    }

    public static interface BuilderWithDescription {
        public BuilderWithStrategy withDescription(String description);
    }

    public static interface BuilderWithStrategy {
        public BuilderWithGroups withStrategy(String strategy);
    }

    public static interface BuilderWithGroups {
        public BuilderWithProperties withGroups(List<String> groups);

        public BuilderWithEnabled withProperties(Map<String, Object> properties);
    }

    public static interface BuilderWithProperties {
        public BuilderWithEnabled withProperties(Map<String, Object> properties);
    }

    public static interface BuilderWithEnabled {
        public FinalStep withEnabled(boolean isEnabled);
    }

    public static interface FinalStep {
        public FactoryFinalizationStep build();
    }

    public static interface FactoryFinalizationStep {
        public StrategizableFactory create();
    }

}
