package com.ticksense.activities.gemmining;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.core.ActivityType;

public final class GemMiningModule implements ActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.GEM_MINING, "Gem Mining", 25, 0.75D, false);

    @Override
    public ActivityDefinition definition()
    {
        return DEFINITION;
    }

    @Override
    public boolean isEnabled()
    {
        return GemMiningIds.verificationDecision().allowsStrategyEnablement();
    }

    @Override
    public ActivityStrategy createStrategy()
    {
        return new GemMiningStrategy();
    }

    @Override
    public ReportBuilder reportBuilder()
    {
        return new GemMiningAnalyzer()::buildReport;
    }
}
