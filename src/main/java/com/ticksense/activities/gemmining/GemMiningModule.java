package com.ticksense.activities.gemmining;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.core.ActivityType;

public final class GemMiningModule extends SimpleActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.GEM_MINING, "Gem Mining", 25, 0.75D, false);

    public GemMiningModule()
    {
        super(ActivityDescriptor.reportable(
            DEFINITION,
            () -> GemMiningIds.verificationDecision().allowsStrategyEnablement(),
            GemMiningStrategy::new,
            new GemMiningAnalyzer()::buildReport));
    }
}
