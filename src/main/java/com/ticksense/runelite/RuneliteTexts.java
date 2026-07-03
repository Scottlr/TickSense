package com.ticksense.runelite;

import com.ticksense.common.TextValues;

final class RuneliteTexts
{
    private RuneliteTexts()
    {
    }

    static String requireText(String value, String fieldName)
    {
        return TextValues.requireText(value, fieldName);
    }
}
