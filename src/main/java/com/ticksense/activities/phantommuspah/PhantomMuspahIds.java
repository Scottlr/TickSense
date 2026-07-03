package com.ticksense.activities.phantommuspah;

public final class PhantomMuspahIds
{
    private static final int[] BOSS_NPC_IDS = {
        12077, // RuneLite NpcID.PHANTOM_MUSPAH
        12078, // RuneLite NpcID.PHANTOM_MUSPAH_12078
        12079, // RuneLite NpcID.PHANTOM_MUSPAH_12079
        12080, // RuneLite NpcID.PHANTOM_MUSPAH_12080
        12082, // RuneLite NpcID.PHANTOM_MUSPAH_12082
        15549 // RuneLite NpcID.PHANTOM_MUSPAH_15549
    };

    private static final int[] ATTACK_PROJECTILE_IDS = {
        // Intentionally empty until debug fixtures verify ranged/magic/shield projectile IDs.
    };

    private static final int[] SPIKE_OBJECT_OR_GRAPHIC_IDS = {
        // Intentionally empty until debug fixtures verify spike and movement cue IDs.
    };

    private PhantomMuspahIds()
    {
    }

    public static int[] bossNpcIds()
    {
        return BOSS_NPC_IDS.clone();
    }

    public static int[] attackProjectileIds()
    {
        return ATTACK_PROJECTILE_IDS.clone();
    }

    public static int[] spikeObjectOrGraphicIds()
    {
        return SPIKE_OBJECT_OR_GRAPHIC_IDS.clone();
    }
}
