package com.ticksense.activities.execution.equipment;

import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.OpportunityDefinition;
import com.ticksense.activities.OpportunityInstance;
import com.ticksense.activities.execution.AbstractExecutionTracker;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.EntityRef;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import com.ticksense.telemetry.events.PlayerActionTelemetryEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class GearSwitchAttackTracker extends AbstractExecutionTracker
{
    public static final String ID = "gear-switch-attack";
    public static final String OPPORTUNITY_GEAR_SWITCH_ATTACK = "GEAR_SWITCH_ATTACK";

    private static final long DEFAULT_TIMEOUT_MILLIS = 2_400L;

    private final Set<Integer> equipmentContainerIds;
    private OpportunityInstance switchAttackWindow;

    public GearSwitchAttackTracker()
    {
        this(GearSwitchDetector.defaultEquipmentContainerIds());
    }

    public GearSwitchAttackTracker(Set<Integer> equipmentContainerIds)
    {
        super(ID);
        this.equipmentContainerIds = new HashSet<>(equipmentContainerIds);
    }

    public static GearSwitchAttackTracker forEquipmentContainers(Integer... containerIds)
    {
        return new GearSwitchAttackTracker(new HashSet<>(Arrays.asList(containerIds)));
    }

    @Override
    public void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        if (!supports(context, session))
        {
            return;
        }
        if (event instanceof InventoryDeltaTelemetryEvent)
        {
            openAfterGearSwitch(session, (InventoryDeltaTelemetryEvent) event);
            return;
        }
        if (event instanceof PlayerActionTelemetryEvent)
        {
            completeOnAttack((PlayerActionTelemetryEvent) event);
        }
    }

    @Override
    public void reset()
    {
        super.reset();
        switchAttackWindow = null;
    }

    private void openAfterGearSwitch(ActivitySession session, InventoryDeltaTelemetryEvent event)
    {
        if (isOpen(switchAttackWindow) || !GearSwitchDetector.isLikelyGearSwitch(event, equipmentContainerIds))
        {
            return;
        }
        // TODO: Carry changed slot/item details once equipment deltas are normalized and stable across activities.
        switchAttackWindow = startOpportunity(
            definition(
                OPPORTUNITY_GEAR_SWITCH_ATTACK,
                "Gear switch to attack",
                session.getActivityType(),
                DEFAULT_TIMEOUT_MILLIS,
                "Attack the intended target after switching gear"),
            event.getTime(),
            context(
                "containerId", String.valueOf(event.getContainerId()),
                "deltaCount", String.valueOf(event.getDeltas().size())));
    }

    private void completeOnAttack(PlayerActionTelemetryEvent event)
    {
        if (!isOpen(switchAttackWindow)
            || event.getTargetRef().getType() != EntityRef.Type.NPC
            || !"Attack".equalsIgnoreCase(event.getOption()))
        {
            return;
        }
        // TODO: Treat special-attack clicks, spell casts, and activity-specific target actions as valid follow-ups.
        complete(
            switchAttackWindow,
            event.getTime(),
            PlayerActionTelemetryEvent.TYPE,
            "Player attacked an NPC after switching gear.");
        switchAttackWindow = null;
    }
}
