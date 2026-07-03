package com.ticksense.activities.hunllef;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.activities.DisabledActivityStrategy;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.core.ActivityType;

public final class CorruptedGauntletModule implements ActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.CORRUPTED_GAUNTLET, "Corrupted Gauntlet", 37, 0.75D, true);

    @Override
    public ActivityDefinition definition()
    {
        return DEFINITION;
    }

    @Override
    public boolean isEnabled()
    {
        return false;
    }

    @Override
    public ActivityStrategy createStrategy()
    {
        return new DisabledActivityStrategy(DEFINITION);
    }

    @Override
    public ReportBuilder reportBuilder()
    {
        return (session, activityData, opportunityMarkers) ->
        {
            throw new IllegalArgumentException("Corrupted Gauntlet reports remain disabled until debug fixtures verify mechanics");
        };
    }
}
