package com.ticksense.activities.ids;

import java.util.Set;

public final class InventoryContainerIds
{
    private static final int EQUIPMENT_CONTAINER_ID = 94;

    private InventoryContainerIds()
    {
    }

    public static Set<Integer> equipmentContainerIds()
    {
        return Set.of(EQUIPMENT_CONTAINER_ID);
    }
}
