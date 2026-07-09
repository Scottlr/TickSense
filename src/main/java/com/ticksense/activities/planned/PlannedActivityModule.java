package com.ticksense.activities.planned;

import com.ticksense.activities.ActivityDefinition;
import com.ticksense.activities.ActivityDescriptor;
import com.ticksense.activities.SimpleActivityModule;

public final class PlannedActivityModule extends SimpleActivityModule
{
    private static final double ACTIVATION_THRESHOLD = 0.75D;
    private static final String DISABLED_MESSAGE =
        "Planned activity reports remain disabled until replay fixtures verify detection, termination, and metrics.";

    public PlannedActivityModule(
        com.ticksense.core.ActivityType activityType,
        String displayName,
        int arbitrationPriority,
        boolean bossActivity)
    {
        this(new ActivityDefinition(activityType, displayName, arbitrationPriority, ACTIVATION_THRESHOLD, bossActivity));
    }

    private PlannedActivityModule(ActivityDefinition definition)
    {
        super(ActivityDescriptor.reportsDisabled(
            definition,
            () -> true,
            () -> new PlannedActivityStrategy(definition),
            DISABLED_MESSAGE));
    }
}
