package com.ticksense.activities.boss;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.analytics.ReportBuilder;
import java.util.Objects;
import java.util.function.IntPredicate;

public final class ObserveOnlyBossModule implements ActivityModule
{
    private final ActivityDefinition definition;
    private final IntPredicate bossNpcMatcher;
    private final String reportDisabledMessage;

    public ObserveOnlyBossModule(
        ActivityDefinition definition,
        IntPredicate bossNpcMatcher,
        String reportDisabledMessage)
    {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.bossNpcMatcher = Objects.requireNonNull(bossNpcMatcher, "bossNpcMatcher");
        this.reportDisabledMessage = Objects.requireNonNull(reportDisabledMessage, "reportDisabledMessage");
    }

    @Override
    public ActivityDefinition definition()
    {
        return definition;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    @Override
    public ActivityStrategy createStrategy()
    {
        return new ObserveOnlyBossStrategy(definition, bossNpcMatcher);
    }

    @Override
    public ReportBuilder reportBuilder()
    {
        return (session, activityData, opportunityMarkers) ->
        {
            throw new IllegalArgumentException(reportDisabledMessage);
        };
    }
}
