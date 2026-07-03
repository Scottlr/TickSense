package com.ticksense.activities.scurrius;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.activities.DisabledActivityStrategy;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.core.ActivityType;

public final class ScurriusModule implements ActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.SCURRIUS, "Scurrius", 35, 0.75D, true);

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
            throw new IllegalArgumentException("Scurrius reports remain disabled until debug fixtures verify mechanics");
        };
    }
}
