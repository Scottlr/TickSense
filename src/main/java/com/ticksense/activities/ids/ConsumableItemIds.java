package com.ticksense.activities.ids;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.runelite.api.ItemID;

public final class ConsumableItemIds
{
    // Fallback item IDs remain secondary to inventory action metadata when telemetry carries it.
    private static final int LOBSTER_ITEM_ID = ItemID.LOBSTER;
    private static final int SHARK_ITEM_ID = ItemID.SHARK;
    private static final int MANTA_RAY_ITEM_ID = ItemID.MANTA_RAY;
    private static final int COOKED_KARAMBWAN_ITEM_ID = ItemID.COOKED_KARAMBWAN;
    private static final int ANGLERFISH_ITEM_ID = ItemID.ANGLERFISH;

    private static final int PRAYER_POTION4_ITEM_ID = ItemID.PRAYER_POTION4;
    private static final int RANGING_POTION4_ITEM_ID = ItemID.RANGING_POTION4;
    private static final int SUPER_RESTORE4_ITEM_ID = ItemID.SUPER_RESTORE4;
    private static final int SARADOMIN_BREW4_ITEM_ID = ItemID.SARADOMIN_BREW4;
    private static final int ANTIVENOM4_ITEM_ID = ItemID.ANTIVENOM4;
    private static final int BASTION_POTION4_ITEM_ID = ItemID.BASTION_POTION4;
    private static final int DIVINE_RANGING_POTION4_ITEM_ID = ItemID.DIVINE_RANGING_POTION4;

    private static final Set<Integer> FOOD_ITEM_IDS = Set.of(
        LOBSTER_ITEM_ID,
        SHARK_ITEM_ID,
        MANTA_RAY_ITEM_ID,
        COOKED_KARAMBWAN_ITEM_ID,
        ANGLERFISH_ITEM_ID);

    private static final Set<Integer> POTION_ITEM_IDS = Set.of(
        PRAYER_POTION4_ITEM_ID,
        RANGING_POTION4_ITEM_ID,
        SUPER_RESTORE4_ITEM_ID,
        SARADOMIN_BREW4_ITEM_ID,
        ANTIVENOM4_ITEM_ID,
        BASTION_POTION4_ITEM_ID,
        DIVINE_RANGING_POTION4_ITEM_ID);

    private static final Map<Integer, Set<ItemCapability>> ITEM_CAPABILITIES = itemCapabilities();

    private ConsumableItemIds()
    {
    }

    public static Set<Integer> foodItemIds()
    {
        return FOOD_ITEM_IDS;
    }

    public static Set<Integer> potionItemIds()
    {
        return POTION_ITEM_IDS;
    }

    public static Set<ItemCapability> capabilitiesFor(int itemId)
    {
        final Set<ItemCapability> capabilities = ITEM_CAPABILITIES.get(itemId);
        return capabilities == null ? Collections.emptySet() : capabilities;
    }

    private static Map<Integer, Set<ItemCapability>> itemCapabilities()
    {
        final Map<Integer, Set<ItemCapability>> capabilities = new LinkedHashMap<>();
        for (int itemId : FOOD_ITEM_IDS)
        {
            capabilities.put(itemId, immutableCapabilities(ItemCapability.FOOD));
        }
        capabilities.put(PRAYER_POTION4_ITEM_ID, immutableCapabilities(ItemCapability.DRINK, ItemCapability.PRAYER_RESTORE));
        capabilities.put(RANGING_POTION4_ITEM_ID, immutableCapabilities(ItemCapability.DRINK, ItemCapability.COMBAT_BOOST));
        capabilities.put(SUPER_RESTORE4_ITEM_ID, immutableCapabilities(ItemCapability.DRINK, ItemCapability.PRAYER_RESTORE, ItemCapability.STAT_RESTORE));
        capabilities.put(SARADOMIN_BREW4_ITEM_ID, immutableCapabilities(ItemCapability.DRINK, ItemCapability.BREW));
        capabilities.put(ANTIVENOM4_ITEM_ID, immutableCapabilities(ItemCapability.DRINK, ItemCapability.POISON_CURE));
        capabilities.put(BASTION_POTION4_ITEM_ID, immutableCapabilities(ItemCapability.DRINK, ItemCapability.COMBAT_BOOST));
        capabilities.put(DIVINE_RANGING_POTION4_ITEM_ID, immutableCapabilities(ItemCapability.DRINK, ItemCapability.COMBAT_BOOST));
        return Collections.unmodifiableMap(capabilities);
    }

    private static Set<ItemCapability> immutableCapabilities(ItemCapability first, ItemCapability... rest)
    {
        final EnumSet<ItemCapability> capabilities = EnumSet.of(first, rest);
        return Collections.unmodifiableSet(capabilities);
    }
}
