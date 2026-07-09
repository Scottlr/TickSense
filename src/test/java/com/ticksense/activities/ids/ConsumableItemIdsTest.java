package com.ticksense.activities.ids;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import net.runelite.api.ItemID;
import org.junit.Test;

public class ConsumableItemIdsTest
{
    @Test
    public void exposesSharedFoodAndPotionFallbacks()
    {
        assertTrue(ConsumableItemIds.foodItemIds().contains(ItemID.COOKED_KARAMBWAN));
        assertTrue(ConsumableItemIds.foodItemIds().contains(ItemID.ANGLERFISH));

        assertTrue(ConsumableItemIds.potionItemIds().contains(ItemID.PRAYER_POTION4));
        assertTrue(ConsumableItemIds.potionItemIds().contains(ItemID.SARADOMIN_BREW4));
    }

    @Test
    public void classifiesBroadCapabilitiesFromInventoryActions()
    {
        final InventoryItemClassifier classifier = DefaultInventoryItemClassifier.INSTANCE;

        assertTrue(classifier.hasCapability(999_001, Collections.singletonList("Eat"), ItemCapability.FOOD));
        assertTrue(classifier.hasCapability(999_002, Collections.singletonList("Drink"), ItemCapability.DRINK));
    }

    @Test
    public void classifiesSemanticCapabilitiesFromRegistry()
    {
        final InventoryItemClassifier classifier = DefaultInventoryItemClassifier.INSTANCE;

        final Set<ItemCapability> prayerPotion = classifier.classify(ItemID.PRAYER_POTION4, Collections.emptyList());
        assertTrue(prayerPotion.contains(ItemCapability.DRINK));
        assertTrue(prayerPotion.contains(ItemCapability.PRAYER_RESTORE));

        final Set<ItemCapability> superRestore = classifier.classify(ItemID.SUPER_RESTORE4, Arrays.asList("Drink"));
        assertTrue(superRestore.contains(ItemCapability.PRAYER_RESTORE));
        assertTrue(superRestore.contains(ItemCapability.STAT_RESTORE));

        final Set<ItemCapability> saradominBrew = classifier.classify(ItemID.SARADOMIN_BREW4, Arrays.asList("Drink"));
        assertTrue(saradominBrew.contains(ItemCapability.BREW));
    }
}
