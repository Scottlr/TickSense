package com.ticksense.activities.scurrius;

import com.ticksense.common.IntIdSet;

public final class ScurriusIds
{
    private static final IntIdSet BOSS_NPC_IDS = IntIdSet.of(
        7221, // RuneLite NpcID.SCURRIUS
        7222, // RuneLite NpcID.SCURRIUS_7222
        15548, // RuneLite NpcID.SCURRIUS_15548
        15695); // RuneLite NpcID.SCURRIUS_15695

    private ScurriusIds()
    {
    }

    public static boolean isBossNpcId(int npcId)
    {
        return BOSS_NPC_IDS.contains(npcId);
    }
}
