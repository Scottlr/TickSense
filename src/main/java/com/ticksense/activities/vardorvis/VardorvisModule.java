package com.ticksense.activities.vardorvis;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;
import com.ticksense.core.ActivityType;

public final class VardorvisModule extends SimpleActivityModule
{
    private static final int ARBITRATION_PRIORITY = 30;
    private static final double ACTIVATION_THRESHOLD = 0.75D;

    static final ActivityDefinition DEFINITION =
        new ActivityDefinition(ActivityType.VARDORVIS, "Vardorvis", ARBITRATION_PRIORITY, ACTIVATION_THRESHOLD, true);

    public VardorvisModule()
    {
        super(ActivityDescriptor.reportable(
            DEFINITION,
            () -> VardorvisIds.verificationDecision().allowsNormalReports(),
            VardorvisStrategy::new,
            new VardorvisAnalyzer()::buildReport));
    }
}
