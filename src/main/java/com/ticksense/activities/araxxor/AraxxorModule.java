package com.ticksense.activities.araxxor;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.core.ActivityType;

public final class AraxxorModule extends SimpleActivityModule
{
    private static final int ARBITRATION_PRIORITY = 40;
    private static final double ACTIVATION_THRESHOLD = 0.75D;

    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.ARAXXOR, "Araxxor", ARBITRATION_PRIORITY, ACTIVATION_THRESHOLD, true);

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
