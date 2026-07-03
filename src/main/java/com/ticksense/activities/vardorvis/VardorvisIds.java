package com.ticksense.activities.vardorvis;

public final class VardorvisIds
{
    private static final int[] BOSS_NPC_IDS = {
        12223, // RuneLite NpcID.VARDORVIS
        12224, // RuneLite NpcID.VARDORVIS_12224
        12228, // RuneLite NpcID.VARDORVIS_12228
        12425, // RuneLite NpcID.VARDORVIS_12425
        12426, // RuneLite NpcID.VARDORVIS_12426
        13656 // RuneLite NpcID.VARDORVIS_13656
    };

    private static final int[] HEAD_NPC_IDS = {
        12226 // RuneLite NpcID.VARDORVIS_HEAD
    };

    private static final int[] RANGED_HEAD_PROJECTILE_IDS = {
        // Intentionally empty until source-owned normalized fixtures verify projectile IDs and timing.
    };

    private static final int[] BLOOD_SPLAT_GRAPHIC_IDS = {
        // Intentionally empty until source-owned normalized fixtures verify graphic IDs.
    };

    private static final int[] AXE_MECHANIC_IDS = {
        // Intentionally empty until source-owned normalized fixtures verify axe projectile/graphic IDs.
    };

    private static final int[] VERIFIED_REGION_IDS = {
        // Intentionally empty until source-owned normalized fixtures verify arena/region evidence.
    };

    private static final VardorvisVerificationDecision VERIFICATION_DECISION =
        VardorvisVerificationDecision.current();

    private VardorvisIds()
    {
    }

    public static int[] bossNpcIds()
    {
        return BOSS_NPC_IDS.clone();
    }

    public static int[] headNpcIds()
    {
        return HEAD_NPC_IDS.clone();
    }

    public static int[] rangedHeadProjectileIds()
    {
        return RANGED_HEAD_PROJECTILE_IDS.clone();
    }

    public static int[] bloodSplatGraphicIds()
    {
        return BLOOD_SPLAT_GRAPHIC_IDS.clone();
    }

    public static int[] axeMechanicIds()
    {
        return AXE_MECHANIC_IDS.clone();
    }

    public static int[] verifiedRegionIds()
    {
        return VERIFIED_REGION_IDS.clone();
    }

    public static VardorvisVerificationDecision verificationDecision()
    {
        return VERIFICATION_DECISION;
    }
}
