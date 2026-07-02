package com.ticksense.activities.construction;

public final class ConstructionIds
{
    private static final String APPROVED_METHOD_NAME = "oak-larder";

    private static final int[] BUILD_SPOT_OBJECT_IDS = {
        15403 // RuneLite ObjectID.LARDER_SPACE
    };

    private static final int[] BUILT_OBJECT_IDS = {
        13565, // RuneLite ObjectID.LARDER_13565
        13566, // RuneLite ObjectID.LARDER_13566
        13567 // RuneLite ObjectID.LARDER_13567
    };

    private static final int[] METHOD_ITEM_IDS = {
        8778, // RuneLite ItemID.OAK_PLANK
        2347, // RuneLite ItemID.HAMMER
        8794, // RuneLite ItemID.SAW
        9625, // RuneLite ItemID.CRYSTAL_SAW
        29774 // RuneLite ItemID.AMYS_SAW_OFFHAND
    };

    private static final int[] BUILD_ANIMATION_IDS = {
        3676, // RuneLite AnimationID.CONSTRUCTION
        8912 // RuneLite AnimationID.CONSTRUCTION_IMCANDO
    };

    private static final int[] BANK_WIDGET_GROUP_IDS = {
        12, // RuneLite WidgetID.BANK_GROUP_ID
        15 // RuneLite WidgetID.BANK_INVENTORY_GROUP_ID
    };

    private static final ConstructionVerificationDecision VERIFICATION_DECISION =
        ConstructionVerificationDecision.current();

    private ConstructionIds()
    {
    }

    public static String approvedMethodName()
    {
        return APPROVED_METHOD_NAME;
    }

    public static int[] buildSpotObjectIds()
    {
        return BUILD_SPOT_OBJECT_IDS.clone();
    }

    public static int[] builtObjectIds()
    {
        return BUILT_OBJECT_IDS.clone();
    }

    public static int[] methodItemIds()
    {
        return METHOD_ITEM_IDS.clone();
    }

    public static int[] buildAnimationIds()
    {
        return BUILD_ANIMATION_IDS.clone();
    }

    public static int[] bankWidgetGroupIds()
    {
        return BANK_WIDGET_GROUP_IDS.clone();
    }

    public static ConstructionVerificationDecision verificationDecision()
    {
        return VERIFICATION_DECISION;
    }
}
