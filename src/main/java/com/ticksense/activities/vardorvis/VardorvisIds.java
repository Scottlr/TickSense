package com.ticksense.activities.vardorvis;

import com.ticksense.common.IntIdSet;
import net.runelite.api.NpcID;

public final class VardorvisIds
{
    private static final int[] BOSS_NPC_IDS = {
        NpcID.VARDORVIS,
        NpcID.VARDORVIS_12224,
        NpcID.VARDORVIS_12228,
        NpcID.VARDORVIS_12425,
        NpcID.VARDORVIS_12426,
        NpcID.VARDORVIS_13656
    };

    private static final int[] HEAD_NPC_IDS = {
        NpcID.VARDORVIS_HEAD
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

    private static final IntIdSet BOSS_NPC_ID_SET = IntIdSet.of(BOSS_NPC_IDS);
    private static final IntIdSet HEAD_NPC_ID_SET = IntIdSet.of(HEAD_NPC_IDS);
    private static final IntIdSet RANGED_HEAD_PROJECTILE_ID_SET = IntIdSet.of(RANGED_HEAD_PROJECTILE_IDS);
    private static final IntIdSet BLOOD_SPLAT_GRAPHIC_ID_SET = IntIdSet.of(BLOOD_SPLAT_GRAPHIC_IDS);
    private static final IntIdSet AXE_MECHANIC_ID_SET = IntIdSet.of(AXE_MECHANIC_IDS);
    private static final IntIdSet VERIFIED_REGION_ID_SET = IntIdSet.of(VERIFIED_REGION_IDS);

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

    public static boolean isBossNpcId(int npcId)
    {
        return BOSS_NPC_ID_SET.contains(npcId);
    }

    public static boolean isHeadNpcId(int npcId)
    {
        return HEAD_NPC_ID_SET.contains(npcId);
    }

    public static boolean isRangedHeadProjectileId(int projectileId)
    {
        return RANGED_HEAD_PROJECTILE_ID_SET.contains(projectileId);
    }

    public static boolean isBloodSplatGraphicId(int graphicId)
    {
        return BLOOD_SPLAT_GRAPHIC_ID_SET.contains(graphicId);
    }

    public static boolean isAxeMechanicId(int mechanicId)
    {
        return AXE_MECHANIC_ID_SET.contains(mechanicId);
    }

    public static boolean isVerifiedRegionId(int regionId)
    {
        return VERIFIED_REGION_ID_SET.contains(regionId);
    }

    public static boolean hasVerifiedRegionIds()
    {
        return !VERIFIED_REGION_ID_SET.isEmpty();
    }

    public static VardorvisVerificationDecision verificationDecision()
    {
        return VERIFICATION_DECISION;
    }
}
