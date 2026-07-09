package com.ticksense.activities.construction;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.core.ActivityType;

public final class ConstructionModule extends SimpleActivityModule
{
    private static final int ARBITRATION_PRIORITY = 20;
    private static final double ACTIVATION_THRESHOLD = 0.75D;

    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.CONSTRUCTION, "Construction", ARBITRATION_PRIORITY, ACTIVATION_THRESHOLD, false);

    public ConstructionModule()
    {
        super(ActivityDescriptor.reportable(
            DEFINITION,
            () -> ConstructionIds.verificationDecision().allowsStrategyEnablement(),
            ConstructionStrategy::new,
            new ConstructionAnalyzer()::buildReport));
    }
}
