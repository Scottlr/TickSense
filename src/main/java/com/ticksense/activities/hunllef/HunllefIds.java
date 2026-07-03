package com.ticksense.activities.hunllef;

public final class HunllefIds
{
    private static final int[] CRYSTALLINE_HUNLLEF_NPC_IDS = {
        9021, // RuneLite NpcID.CRYSTALLINE_HUNLLEF
        9022, // RuneLite NpcID.CRYSTALLINE_HUNLLEF_9022
        9023, // RuneLite NpcID.CRYSTALLINE_HUNLLEF_9023
        9024, // RuneLite NpcID.CRYSTALLINE_HUNLLEF_9024
        12123, // RuneLite NpcID.CRYSTALLINE_HUNLLEF_12123
        15613, // RuneLite NpcID.CRYSTALLINE_HUNLLEF_15613
        15614 // RuneLite NpcID.CRYSTALLINE_HUNLLEF_15614
    };

    private static final int[] CORRUPTED_HUNLLEF_NPC_IDS = {
        9035, // RuneLite NpcID.CORRUPTED_HUNLLEF
        9036, // RuneLite NpcID.CORRUPTED_HUNLLEF_9036
        9037, // RuneLite NpcID.CORRUPTED_HUNLLEF_9037
        9038 // RuneLite NpcID.CORRUPTED_HUNLLEF_9038
    };

    private static final int[] ATTACK_PROJECTILE_IDS = {
        // Intentionally empty until debug fixtures verify ranged/magic projectile IDs and attack cadence.
    };

    private static final int[] TORNADO_NPC_OR_GRAPHIC_IDS = {
        // Intentionally empty until debug fixtures verify tornado NPC/graphic IDs.
    };

    private HunllefIds()
    {
    }

    public static int[] crystallineHunllefNpcIds()
    {
        return CRYSTALLINE_HUNLLEF_NPC_IDS.clone();
    }

    public static int[] corruptedHunllefNpcIds()
    {
        return CORRUPTED_HUNLLEF_NPC_IDS.clone();
    }

    public static int[] attackProjectileIds()
    {
        return ATTACK_PROJECTILE_IDS.clone();
    }

    public static int[] tornadoNpcOrGraphicIds()
    {
        return TORNADO_NPC_OR_GRAPHIC_IDS.clone();
    }
}
