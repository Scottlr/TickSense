package com.ticksense.activities.hunllef;

import com.ticksense.common.IntIdSet;

public final class HunllefIds
{
    private static final IntIdSet CRYSTALLINE_HUNLLEF_NPC_IDS = IntIdSet.of(
        9021, // RuneLite NpcID.CRYSTALLINE_HUNLLEF
        9022, // RuneLite NpcID.CRYSTALLINE_HUNLLEF_9022
        9023, // RuneLite NpcID.CRYSTALLINE_HUNLLEF_9023
        9024, // RuneLite NpcID.CRYSTALLINE_HUNLLEF_9024
        12123, // RuneLite NpcID.CRYSTALLINE_HUNLLEF_12123
        15613, // RuneLite NpcID.CRYSTALLINE_HUNLLEF_15613
        15614); // RuneLite NpcID.CRYSTALLINE_HUNLLEF_15614

    private static final IntIdSet CORRUPTED_HUNLLEF_NPC_IDS = IntIdSet.of(
        9035, // RuneLite NpcID.CORRUPTED_HUNLLEF
        9036, // RuneLite NpcID.CORRUPTED_HUNLLEF_9036
        9037, // RuneLite NpcID.CORRUPTED_HUNLLEF_9037
        9038); // RuneLite NpcID.CORRUPTED_HUNLLEF_9038

    private HunllefIds()
    {
    }

    public static boolean isHunllef(int npcId)
    {
        return isCrystallineHunllef(npcId) || isCorruptedHunllef(npcId);
    }

    public static boolean isCrystallineHunllef(int npcId)
    {
        return CRYSTALLINE_HUNLLEF_NPC_IDS.contains(npcId);
    }

    public static boolean isCorruptedHunllef(int npcId)
    {
        return CORRUPTED_HUNLLEF_NPC_IDS.contains(npcId);
    }
}
