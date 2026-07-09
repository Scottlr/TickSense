package com.ticksense.activities.hunllef;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.activities.boss.ObserveOnlyBossStrategy;
import com.ticksense.core.ActivityType;

public final class HunllefModule extends SimpleActivityModule
{
    private static final int ARBITRATION_PRIORITY = 36;
    private static final double ACTIVATION_THRESHOLD = 0.75D;

    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.HUNLLEF, "Hunllef", ARBITRATION_PRIORITY, ACTIVATION_THRESHOLD, true);

    public HunllefModule()
    {
        super(ActivityDescriptor.reportsDisabled(
            DEFINITION,
            () -> true,
            () -> new ObserveOnlyBossStrategy(DEFINITION, HunllefIds::isCrystallineHunllef),
            "Hunllef reports remain disabled until debug fixtures verify mechanics"));
    }
}
