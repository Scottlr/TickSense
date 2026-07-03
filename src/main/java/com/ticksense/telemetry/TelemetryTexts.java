package com.ticksense.telemetry;

import java.util.Objects;

final class TelemetryTexts
{
    private TelemetryTexts()
    {
    }

    static String requireText(String value, String fieldName)
    {
        final String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    static String safeText(String value)
    {
        return value == null ? "" : value;
    }
}
