package com.ticksense.activities.inferno;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.core.ActivityType;

public final class InfernoModule extends SimpleActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.INFERNO, "Inferno", 35, 0.75D, true);

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
