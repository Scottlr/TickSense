package com.ticksense.activities.construction;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.core.ActivityType;

public final class ConstructionModule extends SimpleActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.CONSTRUCTION, "Construction", 20, 0.75D, false);

    public ConstructionModule()
    {
        super(ActivityDescriptor.reportable(
            DEFINITION,
            () -> ConstructionIds.verificationDecision().allowsStrategyEnablement(),
            ConstructionStrategy::new,
            new ConstructionAnalyzer()::buildReport));
    }
}
