package com.ticksense.activities.phantommuspah;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.activities.boss.ObserveOnlyBossModule;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.core.ActivityType;

public final class PhantomMuspahModule implements ActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.PHANTOM_MUSPAH, "Phantom Muspah", 36, 0.75D, true);
    private static final ObserveOnlyBossModule DELEGATE = new ObserveOnlyBossModule(
        DEFINITION,
        PhantomMuspahIds::isBossNpcId,
        "Phantom Muspah reports remain disabled until debug fixtures verify mechanics");

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
