package com.ticksense.activities.inferno;

import com.ticksense.common.IntIdSet;
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;

public final class InfernoIds
{
    private static final int[] NIBBLER_NPC_IDS = {
        NpcID.JALNIBREK,
        NpcID.JALNIBREK_7675
    };

    private static final int[] WAVE_NPC_IDS = {
        NpcID.JALNIB,
        NpcID.JALMEJRAH,
        NpcID.JALAK,
        NpcID.JALAKREKMEJ,
        NpcID.JALAKREKXIL,
        NpcID.JALAKREKKET,
        NpcID.JALIMKOT,
        NpcID.JALXIL,
        NpcID.JALZEK,
        NpcID.JALTOKJAD,
        NpcID.JALTOKJAD_7704,
        NpcID.TZKALZUK
    };

    private static final int[] SUPPLY_ITEM_IDS = {
        ItemID.PRAYER_POTION4,
        ItemID.SUPER_RESTORE4,
        ItemID.SARADOMIN_BREW4,
        ItemID.COOKED_KARAMBWAN,
        ItemID.RUNE_DART,
        ItemID.TOXIC_BLOWPIPE
    };

    private static final int[] VERIFIED_REGION_IDS = {
        // Intentionally empty until source-owned normalized fixtures verify Inferno region/instance evidence.
    };

    private static final int[] PRAYER_STATE_IDS = {
        // Intentionally empty until source-owned normalized fixtures verify prayer API/varbit evidence.
    };

    private static final int[] DEATH_TIMELINE_IDS = {
        // Intentionally empty until source-owned normalized fixtures verify death cue IDs or markers.
    };

    private static final IntIdSet NIBBLER_NPC_ID_SET = IntIdSet.of(NIBBLER_NPC_IDS);
    private static final IntIdSet WAVE_NPC_ID_SET = IntIdSet.of(WAVE_NPC_IDS);
    private static final IntIdSet SUPPLY_ITEM_ID_SET = IntIdSet.of(SUPPLY_ITEM_IDS);
    private static final IntIdSet VERIFIED_REGION_ID_SET = IntIdSet.of(VERIFIED_REGION_IDS);
    private static final IntIdSet PRAYER_STATE_ID_SET = IntIdSet.of(PRAYER_STATE_IDS);
    private static final IntIdSet DEATH_TIMELINE_ID_SET = IntIdSet.of(DEATH_TIMELINE_IDS);

    private static final InfernoVerificationDecision VERIFICATION_DECISION =
        InfernoVerificationDecision.current();

    private InfernoIds()
    {
    }

    public static int[] nibblerNpcIds()
    {
        return NIBBLER_NPC_IDS.clone();
    }

    public static int[] waveNpcIds()
    {
        return WAVE_NPC_IDS.clone();
    }

    public static int[] supplyItemIds()
    {
        return SUPPLY_ITEM_IDS.clone();
    }

    public static int[] verifiedRegionIds()
    {
        return VERIFIED_REGION_IDS.clone();
    }

    public static int[] prayerStateIds()
    {
        return PRAYER_STATE_IDS.clone();
    }

    public static int[] deathTimelineIds()
    {
        return DEATH_TIMELINE_IDS.clone();
    }

    public static boolean isNibblerNpcId(int npcId)
    {
        return NIBBLER_NPC_ID_SET.contains(npcId);
    }

    public static boolean isWaveNpcId(int npcId)
    {
        return WAVE_NPC_ID_SET.contains(npcId);
    }

    public static boolean isSupplyItemId(int itemId)
    {
        return SUPPLY_ITEM_ID_SET.contains(itemId);
    }

    public static boolean isVerifiedRegionId(int regionId)
    {
        return VERIFIED_REGION_ID_SET.contains(regionId);
    }

    public static boolean hasVerifiedRegionIds()
    {
        return !VERIFIED_REGION_ID_SET.isEmpty();
    }

    public static boolean isPrayerStateId(int stateId)
    {
        return PRAYER_STATE_ID_SET.contains(stateId);
    }

    public static boolean isDeathTimelineId(int timelineId)
    {
        return DEATH_TIMELINE_ID_SET.contains(timelineId);
    }

    public static InfernoVerificationDecision verificationDecision()
    {
        return VERIFICATION_DECISION;
    }
}
