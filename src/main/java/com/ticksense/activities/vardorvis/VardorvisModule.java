package com.ticksense.activities.vardorvis;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.core.ActivityType;

public final class VardorvisModule extends SimpleActivityModule
{
    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.VARDORVIS, "Vardorvis", 30, 0.75D, true);

    public VardorvisModule()
    {
        super(ActivityDescriptor.reportable(
            DEFINITION,
            () -> VardorvisIds.verificationDecision().allowsNormalReports(),
            VardorvisStrategy::new,
            new VardorvisAnalyzer()::buildReport));
    }
}
