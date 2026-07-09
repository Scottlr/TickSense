package com.ticksense.activities.hunllef;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.activities.boss.ObserveOnlyBossStrategy;
import com.ticksense.core.ActivityType;

public final class HunllefModule extends SimpleActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.HUNLLEF, "Hunllef", 36, 0.75D, true);

    public HunllefModule()
    {
        super(ActivityDescriptor.reportsDisabled(
            DEFINITION,
            () -> true,
            () -> new ObserveOnlyBossStrategy(DEFINITION, HunllefIds::isCrystallineHunllef),
            "Hunllef reports remain disabled until debug fixtures verify mechanics"));
    }
}
