package com.ticksense.activities;

import static org.junit.Assert.assertEquals;

import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import com.ticksense.core.FinishReason;
import com.ticksense.telemetry.TelemetryEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;

public class ActivityRegistryTest
{
    @Test
    public void returnsStrategiesInPriorityOrder()
    {
        final ActivityStrategy low = strategy(ActivityType.GEM_MINING, "Gem Mining", 10);
        final ActivityStrategy high = strategy(ActivityType.ARAXXOR, "Araxxor", 100);
        final ActivityStrategy medium = strategy(ActivityType.CONSTRUCTION, "Construction", 50);

        final ActivityRegistry registry = ActivityRegistry.builder()
            .register(low)
            .registerFactory(() -> Arrays.asList(medium, high))
            .build();

        assertEquals(Arrays.asList(high, medium, low), registry.getStrategies());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsDuplicateActivityType()
    {
        ActivityRegistry.builder()
            .register(strategy(ActivityType.GEM_MINING, "Gem Mining", 10))
            .register(strategy(ActivityType.GEM_MINING, "Gem Mining Duplicate", 9))
            .build();
    }

    private static ActivityStrategy strategy(ActivityType type, String displayName, int priority)
    {
        return new StubStrategy(new ActivityDefinition(type, displayName, priority, 0.75D, type == ActivityType.ARAXXOR));
    }

    private static final class StubStrategy implements ActivityStrategy
    {
        private final ActivityDefinition definition;

        private StubStrategy(ActivityDefinition definition)
        {
            this.definition = definition;
        }

        @Override
        public ActivityDefinition getDefinition()
        {
            return definition;
        }

        @Override
        public ActivityCandidate evaluateActivation(ActivityContext context, TelemetryEvent event)
        {
            return new ActivityCandidate(ActivityId.of("candidate-" + definition.getActivityType().name()), definition.getActivityType(), 0.8D,
                Collections.singletonList("evidence"), event.getTime(), false, "");
        }

        @Override
        public void onStart(ActivityContext context, ActivitySession session)
        {
        }

        @Override
        public void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event, OpportunitySink sink)
        {
        }

        @Override
        public Optional<FinishReason> evaluateTermination(ActivityContext context, ActivitySession session, TelemetryEvent event)
        {
            return Optional.empty();
        }

        @Override
        public ActivityReportData buildActivityData(ActivityContext context, ActivitySession session)
        {
            return new ActivityReportData(session.getActivityId(), definition.getActivityType(), Collections.emptyMap());
        }
    }
}
