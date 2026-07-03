package com.ticksense.analytics;

import com.ticksense.common.TextValues;

final class AnalyticsTexts
{
    private AnalyticsTexts()
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
