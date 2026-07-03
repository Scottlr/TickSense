package com.ticksense.activities.scurrius;

import com.ticksense.common.IntIdSet;
import net.runelite.api.NpcID;

public final class ScurriusIds
{
    private static final IntIdSet BOSS_NPC_IDS = IntIdSet.of(
        NpcID.SCURRIUS,
        NpcID.SCURRIUS_7222,
        NpcID.SCURRIUS_15548,
        NpcID.SCURRIUS_15695);

    private ScurriusIds()
    {
    }

    public static boolean isBossNpcId(int npcId)
    {
        return BOSS_NPC_IDS.contains(npcId);
    }
}
