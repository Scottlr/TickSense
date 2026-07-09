package com.ticksense.activities.inferno;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.core.ActivityType;

public final class InfernoModule extends SimpleActivityModule
{
    private static final int ARBITRATION_PRIORITY = 35;
    private static final double ACTIVATION_THRESHOLD = 0.75D;

    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.INFERNO, "Inferno", ARBITRATION_PRIORITY, ACTIVATION_THRESHOLD, true);

    public InfernoModule()
    {
        super(ActivityDescriptor.reportable(
            DEFINITION,
            () -> InfernoIds.verificationDecision().allowsStrategyEnablement()
                && InfernoIds.hasVerifiedRegionIds(),
            InfernoStrategy::new,
            new InfernoAnalyzer()::buildReport));
    }
}
