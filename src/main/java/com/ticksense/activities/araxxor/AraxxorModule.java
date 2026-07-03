package com.ticksense.activities.araxxor;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.core.ActivityType;

public final class AraxxorModule implements ActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.ARAXXOR, "Araxxor", 40, 0.75D, true);

    @Override
    public ActivityDefinition definition()
    {
        return DEFINITION;
    }

    @Override
    public boolean isEnabled()
    {
        return AraxxorVerificationDecision.current().allowsNormalStrategyEnablement()
            && AraxxorIds.hasVerifiedRegionIds();
    }

    @Override
    public ActivityStrategy createStrategy()
    {
        return new AraxxorStrategy();
    }

    @Override
    public ReportBuilder reportBuilder()
    {
        return (session, activityData, opportunityMarkers) ->
        {
            throw new IllegalArgumentException("Araxxor reports remain disabled until verification is complete");
        };
    }
}
