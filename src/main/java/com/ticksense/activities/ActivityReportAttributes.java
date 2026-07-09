package com.ticksense.activities;

import com.ticksense.common.ImmutableCollections;
import com.ticksense.common.TextValues;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ActivityReportAttributes
{
    private final Map<String, String> values;

    public ActivityReportAttributes(Map<String, String> values)
    {
        this.values = ImmutableCollections.immutableMap(values);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public String getText(String key)
    {
        return TextValues.trimmedOrEmpty(values.get(TextValues.requireText(key, "key")));
    }

    public int getInt(String key)
    {
        final String raw = getText(key);
        if (raw.isEmpty())
        {
            return 0;
        }
        return Integer.parseInt(raw);
    }

    public Map<String, String> asMap()
    {
        return values;
    }

    public static final class Builder
    {
        private final Map<String, String> values = new LinkedHashMap<>();

        private Builder()
        {
        }

        public Builder putText(String key, String value)
        {
            values.put(TextValues.requireText(key, "key"), TextValues.rawOrEmpty(value));
            return this;
        }

        public Builder putInt(String key, int value)
        {
            values.put(TextValues.requireText(key, "key"), String.valueOf(value));
            return this;
        }

        public ActivityReportAttributes build()
        {
            return new ActivityReportAttributes(Objects.requireNonNull(values, "values"));
        }
    }
}
