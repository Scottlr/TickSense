package com.ticksense.activities.execution.equipment;

import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class GearSwitchDetector
{
    static final int DEFAULT_EQUIPMENT_CONTAINER_ID = 94;

    private GearSwitchDetector()
    {
    }

    static Set<Integer> defaultEquipmentContainerIds()
    {
        return new HashSet<>(Arrays.asList(DEFAULT_EQUIPMENT_CONTAINER_ID));
    }

    static boolean isLikelyGearSwitch(InventoryDeltaTelemetryEvent event, Set<Integer> equipmentContainerIds)
    {
        if (!equipmentContainerIds.isEmpty())
        {
            return equipmentContainerIds.contains(event.getContainerId()) && !event.getDeltas().isEmpty();
        }

        // TODO: Verify normalized equipment-container IDs from real RuneLite fixtures.
        for (InventoryDeltaTelemetryEvent.ItemDelta delta : event.getDeltas())
        {
            if (delta.getBeforeItemId() > 0
                && delta.getAfterItemId() > 0
                && delta.getBeforeItemId() != delta.getAfterItemId()
                && delta.getBeforeQuantity() == 1
                && delta.getAfterQuantity() == 1)
            {
                return true;
            }
        }
        return false;
    }
}
