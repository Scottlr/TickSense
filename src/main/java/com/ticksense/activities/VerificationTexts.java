package com.ticksense.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class VerificationTexts
{
    private VerificationTexts()
    {
    }

    public static List<String> immutableCopy(List<String> values, String fieldName)
    {
        Objects.requireNonNull(values, fieldName);
        final List<String> copy = new ArrayList<>(values.size());
        for (String value : values)
        {
            copy.add(normalizedValue(value, fieldName + " entry"));
        }
        return Collections.unmodifiableList(copy);
    }

    public static String normalizedValue(String value, String fieldName)
    {
        final String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
