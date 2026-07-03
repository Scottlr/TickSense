package com.ticksense.activities.execution.recovery;

import java.util.Set;

final class RecoveryItemFallbackIds
{
    // TODO: Replace these fallback ids with item-action or item-tag metadata once replayed telemetry reliably carries consumable semantics.
    private static final int LOBSTER_ITEM_ID = 379;
    private static final int SHARK_ITEM_ID = 385;
    private static final int MANTA_RAY_ITEM_ID = 391;
    private static final int COOKED_KARAMBWAN_ITEM_ID = 3144;
    private static final int ANGLERFISH_ITEM_ID = 13441;

    private static final int PRAYER_POTION4_ITEM_ID = 2434;
    private static final int RANGING_POTION4_ITEM_ID = 2444;
    private static final int SUPER_RESTORE4_ITEM_ID = 3024;
    private static final int SARADOMIN_BREW4_ITEM_ID = 6685;
    private static final int ANTIVENOM4_ITEM_ID = 12905;
    private static final int BASTION_POTION4_ITEM_ID = 22461;
    private static final int DIVINE_RANGING_POTION4_ITEM_ID = 23733;

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

    private RecoveryItemFallbackIds()
    {
    }

    static Set<Integer> foodItemIds()
    {
        return FOOD_ITEM_IDS;
    }

    static Set<Integer> potionItemIds()
    {
        return POTION_ITEM_IDS;
    }
}
