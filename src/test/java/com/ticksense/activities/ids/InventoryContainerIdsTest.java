package com.ticksense.activities.ids;

import static org.junit.Assert.assertTrue;

import net.runelite.api.InventoryID;
import org.junit.Test;

public class InventoryContainerIdsTest
{
    @Test
    public void exposesEquipmentContainerIds()
    {
        assertTrue(InventoryContainerIds.equipmentContainerIds().contains(InventoryID.EQUIPMENT.getId()));
    }
}
