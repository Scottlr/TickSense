package com.ticksense.activities.execution.recovery;

import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;
import java.util.Set;

final class RecoveryItemConsumption
{
    private RecoveryItemConsumption()
    {
    }

    static Integer consumedItemId(
        InventoryDeltaTelemetryEvent event,
        String inventoryAction,
        Set<Integer> fallbackItemIds,
        boolean allowItemReplacement)
    {
        for (InventoryDeltaTelemetryEvent.ItemDelta delta : event.getDeltas())
        {
            if (!matchesConsumption(delta, inventoryAction, fallbackItemIds))
            {
                continue;
            }
            if (delta.getAfterQuantity() < delta.getBeforeQuantity()
                || (allowItemReplacement && delta.getAfterItemId() != delta.getBeforeItemId()))
            {
                return delta.getBeforeItemId();
            }
        }
        return null;
    }

    private static boolean matchesConsumption(
        InventoryDeltaTelemetryEvent.ItemDelta delta,
        String inventoryAction,
        Set<Integer> fallbackItemIds)
    {
        return delta.hasBeforeInventoryAction(inventoryAction) || fallbackItemIds.contains(delta.getBeforeItemId());
    }
}
