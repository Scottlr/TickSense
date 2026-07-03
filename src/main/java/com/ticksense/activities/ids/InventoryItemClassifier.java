package com.ticksense.activities.ids;

import java.util.List;
import java.util.Set;

public interface InventoryItemClassifier
{
    Set<ItemCapability> classify(int itemId, List<String> inventoryActions);

    default boolean hasCapability(int itemId, List<String> inventoryActions, ItemCapability capability)
    {
        return classify(itemId, inventoryActions).contains(capability);
    }
}
