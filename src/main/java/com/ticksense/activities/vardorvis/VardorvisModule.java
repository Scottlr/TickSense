package com.ticksense.activities.vardorvis;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.core.ActivityType;

public final class VardorvisModule implements ActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.VARDORVIS, "Vardorvis", 30, 0.75D, true);

    @Override
    public ActivityDefinition definition()
    {
        return DEFINITION;
    }

    @Override
    public boolean isEnabled()
    {
        return VardorvisIds.verificationDecision().allowsNormalReports();
    }

    @Override
    public ActivityStrategy createStrategy()
    {
        return new VardorvisStrategy();
    }

    @Override
    public ReportBuilder reportBuilder()
    {
        return new VardorvisAnalyzer()::buildReport;
    }
}
