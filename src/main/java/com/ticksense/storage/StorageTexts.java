package com.ticksense.storage;

import java.util.Objects;

final class StorageTexts
{
    private StorageTexts()
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
