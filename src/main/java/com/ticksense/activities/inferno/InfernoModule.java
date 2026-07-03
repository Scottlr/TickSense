package com.ticksense.activities.inferno;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.core.ActivityType;

public final class InfernoModule implements ActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.INFERNO, "Inferno", 35, 0.75D, true);

    @Override
    public ActivityDefinition definition()
    {
        return DEFINITION;
    }

    @Override
    public boolean isEnabled()
    {
        return InfernoIds.verificationDecision().allowsStrategyEnablement()
            && InfernoIds.hasVerifiedRegionIds();
    }

    @Override
    public ActivityStrategy createStrategy()
    {
        return new InfernoStrategy();
    }

    @Override
    public ReportBuilder reportBuilder()
    {
        return new InfernoAnalyzer()::buildReport;
    }
}
