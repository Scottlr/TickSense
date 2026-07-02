package com.ticksense.activities.gemmining;

import java.util.Arrays;
import java.util.Collections;

public final class GemMiningIds
{
    private static final int[] GEM_ROCK_OBJECT_IDS = {
        9030, // RuneLite ObjectID.VILLAGE_GEM_ROCK1
        9031, // RuneLite ObjectID.VILLAGE_GEM_ROCK2
        9032, // RuneLite ObjectID.VILLAGE_GEM_ROCK3
        11380, // RuneLite ObjectID.GEMROCK1
        11381 // RuneLite ObjectID.GEMROCK
    };

    private static final int[] GEM_MINING_REGION_IDS = {
        // Intentionally empty until normalized capture verifies the exact gem-rock region set.
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
        GemMiningVerificationDecision.partiallyVerified(
            Arrays.asList(
                "Gem rock object IDs are sourced from official RuneLite ObjectID constants for village gem rocks and gem rock variants.",
                "Mining animation IDs are sourced from official RuneLite AnimationID mining pickaxe constants across supported pickaxe families.",
                "Uncut sapphire, emerald, ruby, and diamond item IDs are sourced from official RuneLite ItemID constants for inventory confirmation."
            ),
            Arrays.asList(
                "Normalized replay evidence still needs a real gem mining capture that proves the exact region set where these IDs are observed.",
                "Rock depletion and respawn transitions have not yet been verified from normalized object snapshots.",
                "A real anonymized fixture is still required to prove mine click evidence alongside mining animation, XP, or inventory confirmation."
            ),
            Collections.singletonList(
                "Keep gem mining strategy enablement gated until a real normalized replay upgrades this decision to VERIFIED."
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
