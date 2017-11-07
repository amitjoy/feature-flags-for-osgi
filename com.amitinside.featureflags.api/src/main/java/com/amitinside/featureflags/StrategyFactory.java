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

import java.util.Optional;
import java.util.function.Function;

import org.osgi.annotation.versioning.ProviderType;

import com.amitinside.featureflags.feature.Feature;
import com.amitinside.featureflags.feature.group.FeatureGroup;

/**
 * StrategyFactory is used to create configuration factories for {@code SystemPropertyActivationStrategy}
 * and {@code ServicePropertyActivationStrategy}.
 * This is primarily used by {@link FeatureManager} to create {@code SystemPropertyActivationStrategy} and
 * {@code ServicePropertyActivationStrategy} configuration instances.
 *
 * <pre>
 * final StrategyFactory factory = StrategyFactory.make("MyServiceStrategy", StrategyType.SERVICE_PROPERTY, 
 *                                                                   s -> s.withDescription("My Strategy 1")
 *                                                                         .withKey("propKey")
 *                                                                         .withValue("propValue")
 *                                                                         .build());
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
public class StrategyFactory {

    private final String name;
    private final String description;
    private final String key;
    private final String value;
    private final StrategyType type;

    /**
     * Constructs a Factory instance
     *
     * @param builder the {@link Builder} instance
     *
     * @throws NullPointerException if the {@link Builder} is {@code null} or specified {@code name} or
     *             specified {@code type} in {@link Builder} is null
     */
    private StrategyFactory(final Builder builder) {
        requireNonNull(builder, "Builder cannot be null");
        name = requireNonNull(builder.name, "Name cannot be null");
        description = builder.description;
        key = builder.key;
        value = builder.value;
        type = builder.type;
    }

    private static class Builder
            implements BuilderWithDescription, BuilderWithKey, BuilderWithValue, FinalStep, FactoryFinalizationStep {
        private final String name;
        private String description;
        private String key;
        private String value;
        private final StrategyType type;

        public Builder(final String name, final StrategyType type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public Builder withDescription(final String description) {
            this.description = description;
            return this;
        }

        @Override
        public Builder withKey(final String key) {
            this.key = key;
            return this;
        }

        @Override
        public Builder withValue(final String value) {
            this.value = value;
            return this;
        }

        @Override
        public StrategyFactory create() {
            return new StrategyFactory(this);
        }

        @Override
        public FactoryFinalizationStep build() {
            return this;
        }
    }

    /**
     * Returns configured name (cannot be {@code null})
     */
    public String getName() {
        return name;
    }

    /**
     * Returns configured name (cannot be {@code null})
     */
    public StrategyType getType() {
        return type;
    }

    /**
     * Returns configured description
     */
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    /**
     * Returns configured property key
     */
    public Optional<String> getKey() {
        return Optional.ofNullable(key);
    }

    /**
     * Returns configured property value
     */
    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }

    /**
     * Creates a {@link StrategyFactory} instance
     *
     * @param name the name of the instance
     * @param type the type of the strategy
     * @param configuration the function representing the instance configuration
     * @return the {@link StrategyFactory} instance
     * @throws NullPointerException if any of the specified arguments is {@code null}
     */
    public static StrategyFactory make(final String name, final StrategyType type,
            final Function<BuilderWithDescription, FactoryFinalizationStep> configuration) {
        requireNonNull(configuration, "Function cannot be null");
        return configuration.andThen(FactoryFinalizationStep::create).apply(new Builder(name, type));
    }

    public static interface BuilderWithDescription {
        public BuilderWithKey withDescription(String description);
    }

    public static interface BuilderWithKey {
        public BuilderWithValue withKey(String key);
    }

    public static interface BuilderWithValue {
        public FinalStep withValue(String value);
    }

    public static interface FinalStep {
        public FactoryFinalizationStep build();
    }

    public static interface FactoryFinalizationStep {
        public StrategyFactory create();
    }

    public enum StrategyType {
        SERVICE_PROPERTY,
        SYSTEM_PROPERTY
    }

}
