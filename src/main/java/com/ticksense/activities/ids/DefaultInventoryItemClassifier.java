package com.ticksense.activities.ids;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class DefaultInventoryItemClassifier implements InventoryItemClassifier
{
    public static final DefaultInventoryItemClassifier INSTANCE = new DefaultInventoryItemClassifier();

    private DefaultInventoryItemClassifier()
    {
    }

    @Override
    public Set<ItemCapability> classify(int itemId, List<String> inventoryActions)
    {
        final EnumSet<ItemCapability> capabilities = EnumSet.noneOf(ItemCapability.class);
        addActionCapabilities(capabilities, inventoryActions);
        capabilities.addAll(ConsumableItemIds.capabilitiesFor(itemId));
        return capabilities.isEmpty() ? java.util.Collections.emptySet() : java.util.Collections.unmodifiableSet(capabilities);
    }

    private static void addActionCapabilities(EnumSet<ItemCapability> capabilities, List<String> inventoryActions)
    {
        if (inventoryActions == null || inventoryActions.isEmpty())
        {
            return;
        }
        for (String action : inventoryActions)
        {
            final String normalized = action == null ? "" : action.trim().toLowerCase(Locale.ROOT);
            if ("eat".equals(normalized))
            {
                capabilities.add(ItemCapability.FOOD);
            }
            else if ("drink".equals(normalized))
            {
                capabilities.add(ItemCapability.DRINK);
            }
        }
    }
}
