package com.ticksense.activities.hunllef;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.activities.boss.ObserveOnlyBossModule;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.core.ActivityType;

public final class CorruptedGauntletModule implements ActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.CORRUPTED_GAUNTLET, "Corrupted Gauntlet", 37, 0.75D, true);
    private static final ObserveOnlyBossModule DELEGATE = new ObserveOnlyBossModule(
        DEFINITION,
        HunllefIds::isCorruptedHunllef,
        "Corrupted Gauntlet reports remain disabled until debug fixtures verify mechanics");

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
