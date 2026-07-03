package com.ticksense.activities;

import com.ticksense.common.TextValues;

final class ActivityTexts
{
    private ActivityTexts()
    {
    }

    static String requireText(String value, String fieldName)
    {
        return TextValues.requireText(value, fieldName);
    }

    static String safeText(String value)
    {
        return TextValues.trimmedOrEmpty(value);
    }
}
