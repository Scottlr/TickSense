package com.ticksense.activities.execution;

public final class CommonExecutionTrackers
{
    private CommonExecutionTrackers()
    {
    }

    public static ExecutionTrackerSet combatDefaults()
    {
        return ExecutionTrackerSet.of(
            "combat-defaults",
            combatSupport(),
            new TargetReengagementTracker(),
            new MovementResponseTracker());
    }

    public static ExecutionTrackerSet combatSupport()
    {
        return ExecutionTrackerSet.of(
            "combat-support",
            new FoodRecoveryTracker(),
            new GearSwitchTracker(),
            new PrayerSwitchTracker());
    }

    public static ExecutionTrackerSet skillingDefaults()
    {
        // TODO: Decide whether food recovery belongs here for dangerous skilling contexts.
        return ExecutionTrackerSet.of(
            "skilling-defaults",
            new GearSwitchTracker(),
            new MovementResponseTracker());
    }
}
