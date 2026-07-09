package com.ticksense.activities.phantommuspah;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.activities.boss.ObserveOnlyBossStrategy;
import com.ticksense.core.ActivityType;

public final class PhantomMuspahModule extends SimpleActivityModule
{
    private static final int ARBITRATION_PRIORITY = 36;
    private static final double ACTIVATION_THRESHOLD = 0.75D;

    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.PHANTOM_MUSPAH, "Phantom Muspah", ARBITRATION_PRIORITY, ACTIVATION_THRESHOLD, true);

    public PhantomMuspahModule()
    {
        super(ActivityDescriptor.reportsDisabled(
            DEFINITION,
            () -> true,
            () -> new ObserveOnlyBossStrategy(DEFINITION, PhantomMuspahIds::isBossNpcId),
            "Phantom Muspah reports remain disabled until debug fixtures verify mechanics"));
    }
}
