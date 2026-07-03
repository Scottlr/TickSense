package com.ticksense.activities.ids;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConsumableItemIdsTest
{
    @Test
    public void exposesSharedFoodAndPotionFallbacks()
    {
        assertTrue(ConsumableItemIds.foodItemIds().contains(3144));
        assertTrue(ConsumableItemIds.foodItemIds().contains(13441));

        assertTrue(ConsumableItemIds.potionItemIds().contains(2434));
        assertTrue(ConsumableItemIds.potionItemIds().contains(6685));
    }
}
