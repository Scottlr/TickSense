package com.ticksense.activities.araxxor;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.core.ActivityType;

public final class AraxxorModule extends SimpleActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.ARAXXOR, "Araxxor", 40, 0.75D, true);

    public AraxxorModule()
    {
        super(ActivityDescriptor.reportsDisabled(
            DEFINITION,
            () -> AraxxorVerificationDecision.current().allowsNormalStrategyEnablement()
                && AraxxorIds.hasVerifiedRegionIds(),
            AraxxorStrategy::new,
            "Araxxor reports remain disabled until verification is complete"));
    }
}
