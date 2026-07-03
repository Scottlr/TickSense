package com.ticksense.activities.construction;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.core.ActivityType;

public final class ConstructionModule implements ActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.CONSTRUCTION, "Construction", 20, 0.75D, false);

    @Override
    public ActivityDefinition definition()
    {
        return DEFINITION;
    }

    @Override
    public boolean isEnabled()
    {
        return ConstructionIds.verificationDecision().allowsStrategyEnablement();
    }

    @Override
    public ActivityStrategy createStrategy()
    {
        return new ConstructionStrategy();
    }

    @Override
    public ReportBuilder reportBuilder()
    {
        return new ConstructionAnalyzer()::buildReport;
    }
}
