package com.ticksense.activities.phantommuspah;

import com.ticksense.common.IntIdSet;
import net.runelite.api.NpcID;

public final class PhantomMuspahIds
{
    private static final IntIdSet BOSS_NPC_IDS = IntIdSet.of(
        NpcID.PHANTOM_MUSPAH,
        NpcID.PHANTOM_MUSPAH_12078,
        NpcID.PHANTOM_MUSPAH_12079,
        NpcID.PHANTOM_MUSPAH_12080,
        NpcID.PHANTOM_MUSPAH_12082,
        NpcID.PHANTOM_MUSPAH_15549);

    private PhantomMuspahIds()
    {
    }

    public static boolean isBossNpcId(int npcId)
    {
        return BOSS_NPC_IDS.contains(npcId);
    }
}
