package com.ticksense.activities.phantommuspah;

import com.ticksense.common.IntIdSet;

public final class PhantomMuspahIds
{
    private static final IntIdSet BOSS_NPC_IDS = IntIdSet.of(
        12077, // RuneLite NpcID.PHANTOM_MUSPAH
        12078, // RuneLite NpcID.PHANTOM_MUSPAH_12078
        12079, // RuneLite NpcID.PHANTOM_MUSPAH_12079
        12080, // RuneLite NpcID.PHANTOM_MUSPAH_12080
        12082, // RuneLite NpcID.PHANTOM_MUSPAH_12082
        15549); // RuneLite NpcID.PHANTOM_MUSPAH_15549

    private PhantomMuspahIds()
    {
    }

    public static boolean isBossNpcId(int npcId)
    {
        return BOSS_NPC_IDS.contains(npcId);
    }
}
