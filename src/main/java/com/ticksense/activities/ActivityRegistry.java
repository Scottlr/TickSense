package com.ticksense.activities;

import com.ticksense.core.ActivityType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ActivityRegistry
{
    private final List<ActivityStrategy> strategies;
    private final Map<ActivityType, ActivityStrategy> strategiesByType;

    private ActivityRegistry(List<ActivityStrategy> strategies)
    {
        this.strategies = ActivityCollections.immutableList(strategies);
        this.strategiesByType = indexByType(strategies);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public List<ActivityStrategy> getStrategies()
    {
        return strategies;
    }

    public Optional<ActivityStrategy> find(ActivityType activityType)
    {
        return Optional.ofNullable(strategiesByType.get(Objects.requireNonNull(activityType, "activityType")));
    }

    private static Map<ActivityType, ActivityStrategy> indexByType(List<ActivityStrategy> orderedStrategies)
    {
        final Map<ActivityType, ActivityStrategy> indexed = new LinkedHashMap<>();
        final EnumSet<ActivityType> seen = EnumSet.noneOf(ActivityType.class);
        for (ActivityStrategy strategy : orderedStrategies)
        {
            final ActivityType type = strategy.getDefinition().getActivityType();
            if (!seen.add(type))
            {
                throw new IllegalArgumentException("Duplicate activity strategy registration for " + type);
            }
            indexed.put(type, strategy);
        }
        return indexed;
    }

    public static final class Builder
    {
        private final List<ActivityStrategy> strategies = new ArrayList<>();

        private Builder()
        {
        }

        public Builder register(ActivityStrategy strategy)
        {
            strategies.add(Objects.requireNonNull(strategy, "strategy"));
            return this;
        }

        public Builder registerAll(Collection<? extends ActivityStrategy> strategies)
        {
            for (ActivityStrategy strategy : Objects.requireNonNull(strategies, "strategies"))
            {
                register(strategy);
            }
            return this;
        }

        public Builder registerFactory(ActivityStrategyFactory factory)
        {
            return registerAll(Objects.requireNonNull(factory, "factory").createStrategies());
        }

        public ActivityRegistry build()
        {
            final List<ActivityStrategy> ordered = new ArrayList<>(strategies);
            ordered.sort(Comparator
                .comparingInt((ActivityStrategy strategy) -> strategy.getDefinition().getPriority()).reversed()
                .thenComparing(strategy -> strategy.getDefinition().getDisplayName())
                .thenComparing(strategy -> strategy.getDefinition().getActivityType().name()));
            return new ActivityRegistry(ordered);
        }
    }
}
