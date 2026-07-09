package com.ticksense.activities.scurrius;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.activities.boss.ObserveOnlyBossStrategy;
import com.ticksense.core.ActivityType;

public final class ScurriusModule extends SimpleActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.SCURRIUS, "Scurrius", 35, 0.75D, true);

    public ScurriusModule()
    {
        super(ActivityDescriptor.reportsDisabled(
            DEFINITION,
            () -> true,
            () -> new ObserveOnlyBossStrategy(DEFINITION, ScurriusIds::isBossNpcId),
            "Scurrius reports remain disabled until debug fixtures verify mechanics"));
    }
}
