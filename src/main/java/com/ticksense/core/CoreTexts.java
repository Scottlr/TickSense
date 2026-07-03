package com.ticksense.core;

import com.ticksense.common.TextValues;

final class CoreTexts
{
    private CoreTexts()
    {
    }

    static String requireText(String value, String fieldName)
    {
        return TextValues.requireText(value, fieldName);
    }
}
