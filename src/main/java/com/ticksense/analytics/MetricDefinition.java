package com.ticksense.analytics;

import java.util.Objects;

public final class MetricDefinition
{
    private final String key;
    private final String displayName;
    private final MetricUnit unit;
    private final String description;
    private final boolean lowerValueBetter;

    public MetricDefinition(String key, String displayName, MetricUnit unit, String description)
    {
        this(key, displayName, unit, description, true);
    }

    public MetricDefinition(
        String key,
        String displayName,
        MetricUnit unit,
        String description,
        boolean lowerValueBetter)
    {
        this.key = requireText(key, "key");
        this.displayName = requireText(displayName, "displayName");
        this.unit = Objects.requireNonNull(unit, "unit");
        this.description = safeText(description);
        this.lowerValueBetter = lowerValueBetter;
    }

    public String getKey()
    {
        return key;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public MetricUnit getUnit()
    {
        return unit;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isLowerValueBetter()
    {
        return lowerValueBetter;
    }

    private static String requireText(String value, String fieldName)
    {
        final String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static String safeText(String value)
    {
        return value == null ? "" : value.trim();
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof MetricDefinition))
        {
            return false;
        }
        final MetricDefinition that = (MetricDefinition) other;
        return lowerValueBetter == that.lowerValueBetter
            && key.equals(that.key)
            && displayName.equals(that.displayName)
            && unit == that.unit
            && description.equals(that.description);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(key, displayName, unit, description, lowerValueBetter);
    }
}
