package com.ticksense.activities.scurrius;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.activities.boss.ObserveOnlyBossModule;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.core.ActivityType;

public final class ScurriusModule implements ActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.SCURRIUS, "Scurrius", 35, 0.75D, true);
    private static final ObserveOnlyBossModule DELEGATE = new ObserveOnlyBossModule(
        DEFINITION,
        ScurriusIds::isBossNpcId,
        "Scurrius reports remain disabled until debug fixtures verify mechanics");

    @Override
    public ActivityDefinition definition()
    {
        return DEFINITION;
    }

    @Override
    public boolean isEnabled()
    {
        return DELEGATE.isEnabled();
    }

    @Override
    public ActivityStrategy createStrategy()
    {
        return DELEGATE.createStrategy();
    }

    @Override
    public ReportBuilder reportBuilder()
    {
        return DELEGATE.reportBuilder();
    }
}
