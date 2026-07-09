package com.ticksense.activities.hunllef;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.activities.boss.ObserveOnlyBossStrategy;
import com.ticksense.core.ActivityType;

public final class CorruptedGauntletModule extends SimpleActivityModule
{
    private static final int ARBITRATION_PRIORITY = 37;
    private static final double ACTIVATION_THRESHOLD = 0.75D;

    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.CORRUPTED_GAUNTLET, "Corrupted Gauntlet", ARBITRATION_PRIORITY, ACTIVATION_THRESHOLD, true);

    public CorruptedGauntletModule()
    {
        super(ActivityDescriptor.reportsDisabled(
            DEFINITION,
            () -> true,
            () -> new ObserveOnlyBossStrategy(DEFINITION, HunllefIds::isCorruptedHunllef),
            "Corrupted Gauntlet reports remain disabled until debug fixtures verify mechanics"));
    }
}
