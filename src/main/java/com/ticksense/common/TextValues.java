package com.ticksense.common;

import java.util.Objects;

public final class TextValues
{
    private TextValues()
    {
    }

    public static String requireText(String value, String fieldName)
    {
        final String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
