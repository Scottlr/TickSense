package com.ticksense.activities.araxxor;

import com.ticksense.common.IntIdSet;
import java.util.List;
import net.runelite.api.NpcID;

public final class AraxxorIds
{
    private static final int[] ARAXXOR_NPC_IDS = {
        NpcID.ARAXXOR,
        NpcID.ARAXXOR_13669
    };

    private static final int[] SPIDER_NPC_IDS = {
        NpcID.MIRRORBACK_ARAXYTE,
        NpcID.RUPTURA_ARAXYTE,
        NpcID.ACIDIC_ARAXYTE,
        NpcID.DREADBORN_ARAXYTE
    };

    private static final int[] RELEVANT_GRAPHIC_IDS = {
        // Intentionally empty until T029 captures verified Araxxor mechanic fixtures.
    };

    private static final int[] RELEVANT_PROJECTILE_IDS = {
        // Intentionally empty until T029 captures verified Araxxor mechanic fixtures.
    };

    private static final int[] VERIFIED_REGION_IDS = {
        // Intentionally empty until T029 captures verified Araxxor region/instance fixtures.
    };

    private static final IntIdSet ARAXXOR_NPC_ID_SET = IntIdSet.of(ARAXXOR_NPC_IDS);
    private static final IntIdSet SPIDER_NPC_ID_SET = IntIdSet.of(SPIDER_NPC_IDS);
    private static final IntIdSet RELEVANT_GRAPHIC_ID_SET = IntIdSet.of(RELEVANT_GRAPHIC_IDS);
    private static final IntIdSet RELEVANT_PROJECTILE_ID_SET = IntIdSet.of(RELEVANT_PROJECTILE_IDS);
    private static final IntIdSet VERIFIED_REGION_ID_SET = IntIdSet.of(VERIFIED_REGION_IDS);

    private static final AraxxorVerificationStatus VERIFICATION_STATUS = AraxxorVerificationStatus.PARTIALLY_VERIFIED;

    private static final List<String> VERIFIED_EVIDENCE = List.of(
        "Official RuneLite NpcID constants expose Araxxor boss IDs 13668 and 13669.",
        "Official RuneLite NpcID constants expose named Araxyte spider IDs 13671, 13673, 13675, and 13680.",
        "Activity callers consume these IDs through catalog intent methods rather than direct NpcID references."
    );

    private static final List<String> BLOCKERS = List.of(
        "T029 has not yet committed source-owned normalized Araxxor replay fixtures for boss start, spider windows, teleport/mid-kill transitions, or finish evidence.",
        "Araxxor spider attackability triggers still need RuneLite Dev Tools verification before normal strategy activation is safe.",
        "Relevant Araxxor projectile, graphic, and region identifiers remain intentionally uncommitted until they are verified from source-owned evidence."
    );

    private static final List<String> NOTES = List.of(
        "Normal Araxxor strategy registration must stay disabled until verificationStatus() is VERIFIED.",
        "If OSRS updates change Araxxor or Araxyte NPC identifiers, refresh the source comments and replay fixtures together before enabling the strategy."
    );

    private AraxxorIds()
    {
    }

    public static int[] araxxorNpcIds()
    {
        return ARAXXOR_NPC_IDS.clone();
    }

    public static int[] spiderNpcIds()
    {
        return SPIDER_NPC_IDS.clone();
    }

    public static int[] relevantGraphicIds()
    {
        return RELEVANT_GRAPHIC_IDS.clone();
    }

    public static int[] relevantProjectileIds()
    {
        return RELEVANT_PROJECTILE_IDS.clone();
    }

    public static int[] verifiedRegionIds()
    {
        return VERIFIED_REGION_IDS.clone();
    }

    public static boolean isAraxxorNpcId(int npcId)
    {
        return ARAXXOR_NPC_ID_SET.contains(npcId);
    }

    public static boolean isSpiderNpcId(int npcId)
    {
        return SPIDER_NPC_ID_SET.contains(npcId);
    }

    public static boolean isRelevantGraphicId(int graphicId)
    {
        return RELEVANT_GRAPHIC_ID_SET.contains(graphicId);
    }

    public static boolean isRelevantProjectileId(int projectileId)
    {
        return RELEVANT_PROJECTILE_ID_SET.contains(projectileId);
    }

    public static boolean isVerifiedRegionId(int regionId)
    {
        return VERIFIED_REGION_ID_SET.contains(regionId);
    }

    public static boolean hasVerifiedRegionIds()
    {
        return !VERIFIED_REGION_ID_SET.isEmpty();
    }

    public static AraxxorVerificationStatus verificationStatus()
    {
        return VERIFICATION_STATUS;
    }

    public static List<String> verifiedEvidence()
    {
        return VERIFIED_EVIDENCE;
    }

    public static List<String> blockers()
    {
        return BLOCKERS;
    }

    public static List<String> notes()
    {
        return NOTES;
    }

    public static boolean allowsStrategyEnablement()
    {
        return verificationStatus().allowsNormalStrategyEnablement()
            && !ARAXXOR_NPC_ID_SET.isEmpty()
            && !SPIDER_NPC_ID_SET.isEmpty();
    }
}
