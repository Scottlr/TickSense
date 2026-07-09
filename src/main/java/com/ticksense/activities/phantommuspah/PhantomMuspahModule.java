package com.ticksense.activities.phantommuspah;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.activities.boss.ObserveOnlyBossStrategy;
import com.ticksense.core.ActivityType;

public final class PhantomMuspahModule extends SimpleActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.PHANTOM_MUSPAH, "Phantom Muspah", 36, 0.75D, true);

    public PhantomMuspahModule()
    {
        super(ActivityDescriptor.reportsDisabled(
            DEFINITION,
            () -> true,
            () -> new ObserveOnlyBossStrategy(DEFINITION, PhantomMuspahIds::isBossNpcId),
            "Phantom Muspah reports remain disabled until debug fixtures verify mechanics"));
    }
}
