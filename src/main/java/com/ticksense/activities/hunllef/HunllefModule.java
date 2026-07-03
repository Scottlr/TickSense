package com.ticksense.activities.hunllef;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.activities.boss.ObserveOnlyBossModule;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.core.ActivityType;

public final class HunllefModule implements ActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.HUNLLEF, "Hunllef", 36, 0.75D, true);
    private static final ObserveOnlyBossModule DELEGATE = new ObserveOnlyBossModule(
        DEFINITION,
        HunllefIds::isCrystallineHunllef,
        "Hunllef reports remain disabled until debug fixtures verify mechanics");

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
