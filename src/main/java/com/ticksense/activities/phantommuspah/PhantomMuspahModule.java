package com.ticksense.activities.phantommuspah;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.activities.DisabledActivityStrategy;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.core.ActivityType;

public final class PhantomMuspahModule implements ActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.PHANTOM_MUSPAH, "Phantom Muspah", 36, 0.75D, true);

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
            throw new IllegalArgumentException("Phantom Muspah reports remain disabled until debug fixtures verify mechanics");
        };
    }
}
