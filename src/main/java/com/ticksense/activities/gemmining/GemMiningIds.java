package com.ticksense.activities.gemmining;

import com.ticksense.common.IntIdSet;
import java.util.List;
import net.runelite.api.AnimationID;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;

public final class GemMiningIds
{
    private static final int[] GEM_ROCK_OBJECT_IDS = {
        ObjectID.GEM_ROCKS,
        ObjectID.GEM_ROCKS_11381
    };

    private static final int[] GEM_MINING_REGION_IDS = {
        11410 // Source-owned normalized verification fixture local player location 2840,9388 => region 11410
    };

    private static final int[] MINING_ANIMATION_IDS = {
        AnimationID.MINING_BRONZE_PICKAXE,
        AnimationID.MINING_IRON_PICKAXE,
        AnimationID.MINING_STEEL_PICKAXE,
        AnimationID.MINING_BLACK_PICKAXE,
        AnimationID.MINING_MITHRIL_PICKAXE,
        AnimationID.MINING_ADAMANT_PICKAXE,
        AnimationID.MINING_RUNE_PICKAXE,
        AnimationID.MINING_GILDED_PICKAXE,
        AnimationID.MINING_DRAGON_PICKAXE,
        AnimationID.MINING_DRAGON_PICKAXE_UPGRADED,
        AnimationID.MINING_DRAGON_PICKAXE_OR,
        AnimationID.MINING_DRAGON_PICKAXE_OR_TRAILBLAZER,
        AnimationID.MINING_INFERNAL_PICKAXE,
        AnimationID.MINING_3A_PICKAXE,
        AnimationID.MINING_CRYSTAL_PICKAXE,
        AnimationID.MINING_TRAILBLAZER_PICKAXE,
        AnimationID.MINING_TRAILBLAZER_PICKAXE_2,
        AnimationID.MINING_TRAILBLAZER_PICKAXE_3
    };

    private static final int[] UNCUT_GEM_ITEM_IDS = {
        ItemID.UNCUT_DIAMOND,
        ItemID.UNCUT_RUBY,
        ItemID.UNCUT_EMERALD,
        ItemID.UNCUT_SAPPHIRE
    };

    private static final IntIdSet GEM_ROCK_OBJECT_ID_SET = IntIdSet.of(GEM_ROCK_OBJECT_IDS);
    private static final IntIdSet GEM_MINING_REGION_ID_SET = IntIdSet.of(GEM_MINING_REGION_IDS);
    private static final IntIdSet MINING_ANIMATION_ID_SET = IntIdSet.of(MINING_ANIMATION_IDS);
    private static final IntIdSet UNCUT_GEM_ITEM_ID_SET = IntIdSet.of(UNCUT_GEM_ITEM_IDS);

    private static final List<String> VERIFIED_EVIDENCE = List.of(
        "Gem rock object IDs are sourced from official RuneLite ObjectID constants and exercised in the source-owned normalized verification fixture for the underground Shilo gem mine.",
        "Gem mining region 11410 is verified in the source-owned normalized verification fixture via local player and gem rock world locations at 2840,9388.",
        "Rock availability, depletion, and respawn transitions are verified in src/test/resources/replays/gem-mining-basic.jsonl through normalized object state events.",
        "Mine click evidence is verified in src/test/resources/replays/gem-mining-basic.jsonl through a normalized player action event targeting Gem rock.",
        "Mining animation IDs are sourced from official RuneLite AnimationID mining pickaxe constants across supported pickaxe families.",
        "Mining confirmation is verified in src/test/resources/replays/gem-mining-basic.jsonl through mining XP and uncut ruby inventory gain events that use official RuneLite item IDs.");
    private static final List<String> NOTES = List.of(
        "This verified MVP slice is intentionally scoped to the underground Shilo gem mine fixture region rather than every gem-rock location in the game.",
        "If OSRS updates change gem rock behavior or IDs, refresh the fixture and registry comments together before keeping gem mining enabled.");
    private static final GemMiningVerificationDecision VERIFICATION_DECISION =
        GemMiningVerificationDecision.verified(VERIFIED_EVIDENCE, NOTES);

    private GemMiningIds()
    {
    }

    public static int[] gemRockObjectIds()
    {
        return GEM_ROCK_OBJECT_IDS.clone();
    }

    public static int[] gemMiningRegionIds()
    {
        return GEM_MINING_REGION_IDS.clone();
    }

    public static int[] miningAnimationIds()
    {
        return MINING_ANIMATION_IDS.clone();
    }

    public static int[] uncutGemItemIds()
    {
        return UNCUT_GEM_ITEM_IDS.clone();
    }

    public static boolean isGemRockObjectId(int objectId)
    {
        return GEM_ROCK_OBJECT_ID_SET.contains(objectId);
    }

    public static boolean isGemMiningRegionId(int regionId)
    {
        return GEM_MINING_REGION_ID_SET.contains(regionId);
    }

    public static boolean isMiningAnimationId(int animationId)
    {
        return MINING_ANIMATION_ID_SET.contains(animationId);
    }

    public static boolean isUncutGemItemId(int itemId)
    {
        return UNCUT_GEM_ITEM_ID_SET.contains(itemId);
    }

    public static String verifiedRegionIdsCsv()
    {
        return GEM_MINING_REGION_ID_SET.joinCsv();
    }

    public static String verifiedObjectIdsCsv()
    {
        return GEM_ROCK_OBJECT_ID_SET.joinCsv();
    }

    public static GemMiningVerificationDecision verificationDecision()
    {
        return VERIFICATION_DECISION;
    }
}
