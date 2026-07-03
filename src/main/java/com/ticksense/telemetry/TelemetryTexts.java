package com.ticksense.telemetry;

import com.ticksense.common.TextValues;

final class TelemetryTexts
{
    private TelemetryTexts()
    {
    }

    static String requireText(String value, String fieldName)
    {
        return TextValues.requireText(value, fieldName);
    }

    static String safeText(String value)
    {
        return value == null ? "" : value;
    }
}
