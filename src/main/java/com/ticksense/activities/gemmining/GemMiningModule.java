package com.ticksense.activities.gemmining;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.core.ActivityType;

public final class GemMiningModule extends SimpleActivityModule
{
    private static final int ARBITRATION_PRIORITY = 25;
    private static final double ACTIVATION_THRESHOLD = 0.75D;

    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.GEM_MINING, "Gem Mining", ARBITRATION_PRIORITY, ACTIVATION_THRESHOLD, false);

    public GemMiningModule()
    {
        super(ActivityDescriptor.reportable(
            DEFINITION,
            () -> GemMiningIds.verificationDecision().allowsStrategyEnablement(),
            GemMiningStrategy::new,
            new GemMiningAnalyzer()::buildReport));
    }
}
