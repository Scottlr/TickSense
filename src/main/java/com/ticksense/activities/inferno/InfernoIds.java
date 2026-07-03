package com.ticksense.activities.inferno;

public final class InfernoIds
{
    private static final int[] NIBBLER_NPC_IDS = {
        7674, // RuneLite NpcID.JALNIBREK
        7675 // RuneLite NpcID.JALNIBREK_7675
    };

    private static final int[] WAVE_NPC_IDS = {
        7691, // RuneLite NpcID.JALNIB
        7692, // RuneLite NpcID.JALMEJRAH
        7693, // RuneLite NpcID.JALAK
        7694, // RuneLite NpcID.JALAKREKMEJ
        7695, // RuneLite NpcID.JALAKREKXIL
        7696, // RuneLite NpcID.JALAKREKKET
        7697, // RuneLite NpcID.JALIMKOT
        7698, // RuneLite NpcID.JALXIL
        7699, // RuneLite NpcID.JALZEK
        7700, // RuneLite NpcID.JALTOKJAD
        7704, // RuneLite NpcID.JALTOKJAD_7704
        7706 // RuneLite NpcID.TZKALZUK
    };

    private static final int[] SUPPLY_ITEM_IDS = {
        2434, // RuneLite ItemID.PRAYER_POTION4
        3024, // RuneLite ItemID.SUPER_RESTORE4
        6685, // RuneLite ItemID.SARADOMIN_BREW4
        3144, // RuneLite ItemID.COOKED_KARAMBWAN
        811, // RuneLite ItemID.RUNE_DART
        12926 // RuneLite ItemID.TOXIC_BLOWPIPE
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

    public static InfernoVerificationDecision verificationDecision()
    {
        return VERIFICATION_DECISION;
    }
}
