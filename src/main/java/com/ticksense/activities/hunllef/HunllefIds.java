package com.ticksense.activities.hunllef;

import com.ticksense.common.IntIdSet;
import net.runelite.api.NpcID;

public final class HunllefIds
{
    private static final IntIdSet CRYSTALLINE_HUNLLEF_NPC_IDS = IntIdSet.of(
        NpcID.CRYSTALLINE_HUNLLEF,
        NpcID.CRYSTALLINE_HUNLLEF_9022,
        NpcID.CRYSTALLINE_HUNLLEF_9023,
        NpcID.CRYSTALLINE_HUNLLEF_9024,
        NpcID.CRYSTALLINE_HUNLLEF_12123,
        NpcID.CRYSTALLINE_HUNLLEF_15613,
        NpcID.CRYSTALLINE_HUNLLEF_15614);

    private static final IntIdSet CORRUPTED_HUNLLEF_NPC_IDS = IntIdSet.of(
        NpcID.CORRUPTED_HUNLLEF,
        NpcID.CORRUPTED_HUNLLEF_9036,
        NpcID.CORRUPTED_HUNLLEF_9037,
        NpcID.CORRUPTED_HUNLLEF_9038);

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
