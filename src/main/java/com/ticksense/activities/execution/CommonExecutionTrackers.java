package com.ticksense.activities.execution;

import com.ticksense.activities.execution.equipment.GearSwitchAttackTracker;
import com.ticksense.activities.execution.equipment.GearSwitchTracker;
import com.ticksense.activities.execution.movement.MovementResponseTracker;
import com.ticksense.activities.execution.movement.TargetReengagementTracker;
import com.ticksense.activities.execution.prayer.PrayerSwitchTracker;
import com.ticksense.activities.execution.recovery.FoodRecoveryTracker;
import com.ticksense.activities.execution.recovery.PotionRecoveryTracker;

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
            new PotionRecoveryTracker(),
            new GearSwitchTracker(),
            new GearSwitchAttackTracker(),
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
