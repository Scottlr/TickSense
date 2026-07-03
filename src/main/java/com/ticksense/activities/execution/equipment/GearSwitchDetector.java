package com.ticksense.activities.execution.equipment;

import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import java.util.Set;

final class GearSwitchDetector
{
    private static final int MIN_VALID_ITEM_ID = 1;
    private static final int SINGLE_ITEM_QUANTITY = 1;

    private GearSwitchDetector()
    {
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
            if (looksLikeEquipmentSwap(delta))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean looksLikeEquipmentSwap(InventoryDeltaTelemetryEvent.ItemDelta delta)
    {
        return isValidItemId(delta.getBeforeItemId())
            && isValidItemId(delta.getAfterItemId())
            && delta.getBeforeItemId() != delta.getAfterItemId()
            && isSingleQuantitySwap(delta);
    }

    private static boolean isValidItemId(int itemId)
    {
        return itemId >= MIN_VALID_ITEM_ID;
    }

    private static boolean isSingleQuantitySwap(InventoryDeltaTelemetryEvent.ItemDelta delta)
    {
        return delta.getBeforeQuantity() == SINGLE_ITEM_QUANTITY
            && delta.getAfterQuantity() == SINGLE_ITEM_QUANTITY;
    }
}
