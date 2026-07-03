package com.ticksense.activities.execution.equipment;

import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.OpportunityDefinition;
import com.ticksense.activities.OpportunityInstance;
import com.ticksense.activities.execution.AbstractExecutionTracker;
import com.ticksense.core.ActivitySession;
import com.ticksense.telemetry.TelemetryEvent;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import java.util.Set;

public final class GearSwitchTracker extends AbstractExecutionTracker
{
    public static final String ID = "gear-switch";
    public static final String OPPORTUNITY_GEAR_SWITCH = "GEAR_SWITCH";

    private final Set<Integer> equipmentContainerIds;

    public GearSwitchTracker()
    {
        this(GearSwitchContainerIds.defaultIds());
    }

    public GearSwitchTracker(Set<Integer> equipmentContainerIds)
    {
        super(ID);
        this.equipmentContainerIds = GearSwitchContainerIds.of(equipmentContainerIds);
    }

    public static GearSwitchTracker forEquipmentContainers(Integer... containerIds)
    {
        return new GearSwitchTracker(GearSwitchContainerIds.of(containerIds));
    }

    @Override
    public void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        if (!supports(context, session) || !(event instanceof InventoryDeltaTelemetryEvent))
        {
            return;
        }
        final InventoryDeltaTelemetryEvent inventory = (InventoryDeltaTelemetryEvent) event;
        if (!GearSwitchDetector.isLikelyGearSwitch(inventory, equipmentContainerIds))
        {
            return;
        }
        final OpportunityDefinition definition = definition(
            OPPORTUNITY_GEAR_SWITCH,
            "Gear switch",
            session.getActivityType(),
            0L,
            "Switch equipment for the next action");
        final OpportunityInstance instance = startOpportunity(
            definition,
            inventory.getTime(),
            context(
                "containerId", String.valueOf(inventory.getContainerId()),
                "deltaCount", String.valueOf(inventory.getDeltas().size())));
        complete(instance, inventory.getTime(), InventoryDeltaTelemetryEvent.TYPE, "Detected an equipment-style item replacement.");
    }

}
