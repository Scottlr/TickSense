package com.ticksense.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public static String trimmedOrEmpty(String value)
    {
        return value == null ? "" : value.trim();
    }

    public static String rawOrEmpty(String value)
    {
        return value == null ? "" : value;
    }

    public static List<String> immutableTextList(List<String> values, String fieldName)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<String> copy = new ArrayList<>(values.size());
        for (String value : values)
        {
            copy.add(requireText(value, fieldName + " entry"));
        }
        return Collections.unmodifiableList(copy);
    }
}
