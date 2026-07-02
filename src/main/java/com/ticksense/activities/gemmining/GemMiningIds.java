package com.ticksense.activities.gemmining;

import java.util.Arrays;

public final class GemMiningIds
{
    private static final int[] GEM_ROCK_OBJECT_IDS = {
        11380, // RuneLite ObjectID.GEMROCK1, verified in src/test/resources/replays/gem-mining-basic.jsonl
        11381 // RuneLite ObjectID.GEMROCK, verified in src/test/resources/replays/gem-mining-basic.jsonl
    };

    private static final int[] GEM_MINING_REGION_IDS = {
        11410 // Source-owned normalized verification fixture local player location 2840,9388 => region 11410
    };

    private static final int[] MINING_ANIMATION_IDS = {
        625, // RuneLite AnimationID.MINING_BRONZE_PICKAXE
        626, // RuneLite AnimationID.MINING_IRON_PICKAXE
        627, // RuneLite AnimationID.MINING_STEEL_PICKAXE
        3873, // RuneLite AnimationID.MINING_BLACK_PICKAXE
        629, // RuneLite AnimationID.MINING_MITHRIL_PICKAXE
        628, // RuneLite AnimationID.MINING_ADAMANT_PICKAXE
        624, // RuneLite AnimationID.MINING_RUNE_PICKAXE
        8313, // RuneLite AnimationID.MINING_GILDED_PICKAXE
        7139, // RuneLite AnimationID.MINING_DRAGON_PICKAXE
        642, // RuneLite AnimationID.MINING_DRAGON_PICKAXE_UPGRADED
        8346, // RuneLite AnimationID.MINING_DRAGON_PICKAXE_OR
        8887, // RuneLite AnimationID.MINING_DRAGON_PICKAXE_OR_TRAILBLAZER
        4482, // RuneLite AnimationID.MINING_INFERNAL_PICKAXE
        7283, // RuneLite AnimationID.MINING_3A_PICKAXE
        8347, // RuneLite AnimationID.MINING_CRYSTAL_PICKAXE
        8787, // RuneLite AnimationID.MINING_TRAILBLAZER_PICKAXE
        8788, // RuneLite AnimationID.MINING_TRAILBLAZER_PICKAXE_2
        8789 // RuneLite AnimationID.MINING_TRAILBLAZER_PICKAXE_3
    };

    private static final int[] UNCUT_GEM_ITEM_IDS = {
        1617, // RuneLite ItemID.UNCUT_DIAMOND
        1619, // RuneLite ItemID.UNCUT_RUBY
        1621, // RuneLite ItemID.UNCUT_EMERALD
        1623 // RuneLite ItemID.UNCUT_SAPPHIRE
    };

    private static final GemMiningVerificationDecision VERIFICATION_DECISION =
        GemMiningVerificationDecision.verified(
            Arrays.asList(
                "Gem rock object IDs are sourced from official RuneLite ObjectID constants and exercised in the source-owned normalized verification fixture for the underground Shilo gem mine.",
                "Gem mining region 11410 is verified in the source-owned normalized verification fixture via local player and gem rock world locations at 2840,9388.",
                "Rock availability, depletion, and respawn transitions are verified in src/test/resources/replays/gem-mining-basic.jsonl through normalized object state events.",
                "Mine click evidence is verified in src/test/resources/replays/gem-mining-basic.jsonl through a normalized player action event targeting Gem rock.",
                "Mining animation IDs are sourced from official RuneLite AnimationID mining pickaxe constants across supported pickaxe families.",
                "Mining confirmation is verified in src/test/resources/replays/gem-mining-basic.jsonl through mining XP and uncut ruby inventory gain events that use official RuneLite item IDs."
            ),
            Arrays.asList(
                "This verified MVP slice is intentionally scoped to the underground Shilo gem mine fixture region rather than every gem-rock location in the game.",
                "If OSRS updates change gem rock behavior or IDs, refresh the fixture and registry comments together before keeping gem mining enabled."
            ));

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

    public static GemMiningVerificationDecision verificationDecision()
    {
        return VERIFICATION_DECISION;
    }
}
