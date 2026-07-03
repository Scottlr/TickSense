package com.ticksense.activities.construction;

import com.ticksense.common.IntIdSet;

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

    private static final int[] CONSTRUCTION_WIDGET_GROUP_IDS = {
        458 // Source-owned normalized verification fixture construction build widget group for oak larder
    };

    private static final int[] CONSTRUCTION_WIDGET_CHILD_IDS = {
        12 // Source-owned normalized verification fixture construction build widget child for oak larder
    };

    private static final IntIdSet METHOD_ITEM_ID_SET = IntIdSet.of(METHOD_ITEM_IDS);
    private static final IntIdSet BUILD_SPOT_OBJECT_ID_SET = IntIdSet.of(BUILD_SPOT_OBJECT_IDS);
    private static final IntIdSet BUILT_OBJECT_ID_SET = IntIdSet.of(BUILT_OBJECT_IDS);
    private static final IntIdSet BUILD_ANIMATION_ID_SET = IntIdSet.of(BUILD_ANIMATION_IDS);
    private static final IntIdSet BANK_WIDGET_GROUP_ID_SET = IntIdSet.of(BANK_WIDGET_GROUP_IDS);
    private static final IntIdSet CONSTRUCTION_WIDGET_GROUP_ID_SET = IntIdSet.of(CONSTRUCTION_WIDGET_GROUP_IDS);
    private static final IntIdSet CONSTRUCTION_WIDGET_CHILD_ID_SET = IntIdSet.of(CONSTRUCTION_WIDGET_CHILD_IDS);

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

    public static int[] constructionWidgetGroupIds()
    {
        return CONSTRUCTION_WIDGET_GROUP_IDS.clone();
    }

    public static int[] constructionWidgetChildIds()
    {
        return CONSTRUCTION_WIDGET_CHILD_IDS.clone();
    }

    public static boolean isBuildSpotObjectId(int objectId)
    {
        return BUILD_SPOT_OBJECT_ID_SET.contains(objectId);
    }

    public static boolean isBuiltObjectId(int objectId)
    {
        return BUILT_OBJECT_ID_SET.contains(objectId);
    }

    public static boolean isMethodItemId(int itemId)
    {
        return METHOD_ITEM_ID_SET.contains(itemId);
    }

    public static boolean isBuildAnimationId(int animationId)
    {
        return BUILD_ANIMATION_ID_SET.contains(animationId);
    }

    public static boolean isBankWidgetGroupId(int groupId)
    {
        return BANK_WIDGET_GROUP_ID_SET.contains(groupId);
    }

    public static boolean isConstructionWidgetGroupId(int groupId)
    {
        return CONSTRUCTION_WIDGET_GROUP_ID_SET.contains(groupId);
    }

    public static boolean isConstructionWidgetChildId(int childId)
    {
        return CONSTRUCTION_WIDGET_CHILD_ID_SET.contains(childId);
    }

    public static ConstructionVerificationDecision verificationDecision()
    {
        return VERIFICATION_DECISION;
    }
}
