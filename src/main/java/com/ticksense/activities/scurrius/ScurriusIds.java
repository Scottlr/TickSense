package com.ticksense.activities.scurrius;

public final class ScurriusIds
{
    private static final int[] BOSS_NPC_IDS = {
        7221, // RuneLite NpcID.SCURRIUS
        7222, // RuneLite NpcID.SCURRIUS_7222
        15548, // RuneLite NpcID.SCURRIUS_15548
        15695 // RuneLite NpcID.SCURRIUS_15695
    };

    private static final int[] RAT_NPC_IDS = {
        // Intentionally empty until debug fixtures verify rat spawn NPC IDs.
    };

    private static final int[] ATTACK_PROJECTILE_IDS = {
        // Intentionally empty until debug fixtures verify ranged/magic projectile IDs.
    };

    private static final int[] ROCKFALL_GRAPHIC_IDS = {
        // Intentionally empty until debug fixtures verify rockfall graphic/object cues.
    };

    private ScurriusIds()
    {
    }

    public static int[] bossNpcIds()
    {
        return BOSS_NPC_IDS.clone();
    }

    public static int[] ratNpcIds()
    {
        return RAT_NPC_IDS.clone();
    }

    public static int[] attackProjectileIds()
    {
        return ATTACK_PROJECTILE_IDS.clone();
    }

    public static int[] rockfallGraphicIds()
    {
        return ROCKFALL_GRAPHIC_IDS.clone();
    }
}
