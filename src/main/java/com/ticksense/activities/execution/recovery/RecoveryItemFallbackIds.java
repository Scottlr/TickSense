package com.ticksense.activities.execution.recovery;

import java.util.Set;

final class RecoveryItemFallbackIds
{
    // TODO: Replace these fallback ids with item-action metadata once replayed telemetry reliably carries consumable semantics.
    private RecoveryItemFallbackIds()
    {
    }

    static Set<Integer> foodItemIds()
    {
        return Set.of(379, 385, 391, 3144, 13441);
    }

    static Set<Integer> potionItemIds()
    {
        return Set.of(2434, 2444, 3024, 6685, 12905, 22461, 23733);
    }
}
