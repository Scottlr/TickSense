package com.ticksense.runelite;

import java.util.Objects;

final class RuneliteTexts
{
    private RuneliteTexts()
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
}
