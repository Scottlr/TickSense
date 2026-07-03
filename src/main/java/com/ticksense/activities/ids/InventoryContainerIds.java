package com.ticksense.activities.ids;

import java.util.Set;
import net.runelite.api.InventoryID;

public final class InventoryContainerIds
{
    private static final int EQUIPMENT_CONTAINER_ID = InventoryID.EQUIPMENT.getId();

    private InventoryContainerIds()
    {
    }

    public static Set<Integer> equipmentContainerIds()
    {
        return Set.of(EQUIPMENT_CONTAINER_ID);
    }
}
