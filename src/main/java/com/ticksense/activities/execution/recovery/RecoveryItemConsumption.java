package com.ticksense.activities.execution.recovery;

import com.ticksense.activities.ids.InventoryItemClassifier;
import com.ticksense.activities.ids.ItemCapability;
import com.ticksense.telemetry.events.InventoryDeltaTelemetryEvent;

final class RecoveryItemConsumption
{
    private RecoveryItemConsumption()
    {
    }

    static Integer consumedItemId(
        InventoryDeltaTelemetryEvent event,
        InventoryItemClassifier itemClassifier,
        ItemCapability requiredCapability,
        boolean allowItemReplacement)
    {
        for (InventoryDeltaTelemetryEvent.ItemDelta delta : event.getDeltas())
        {
            if (!matchesConsumption(delta, itemClassifier, requiredCapability))
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
        InventoryItemClassifier itemClassifier,
        ItemCapability requiredCapability)
    {
        return itemClassifier.hasCapability(
            delta.getBeforeItemId(),
            delta.getBeforeInventoryActions(),
            requiredCapability);
    }
}
