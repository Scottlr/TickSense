package com.ticksense.activities;

import com.ticksense.core.ActivityType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class OpportunityDefinition
{
    private final String id;
    private final String displayName;
    private final ActivityType activityType;
    private final long defaultTimeoutMillis;
    private final List<String> expectedResponses;

    public OpportunityDefinition(
        String id,
        String displayName,
        ActivityType activityType,
        long defaultTimeoutMillis,
        List<String> expectedResponses)
    {
        this.id = requireText(id, "id");
        this.displayName = requireText(displayName, "displayName");
        this.activityType = Objects.requireNonNull(activityType, "activityType");
        if (defaultTimeoutMillis < 0L)
        {
            throw new IllegalArgumentException("defaultTimeoutMillis must be >= 0");
        }
        this.defaultTimeoutMillis = defaultTimeoutMillis;
        this.expectedResponses = immutableList(expectedResponses);
    }

    public String getId()
    {
        return id;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public ActivityType getActivityType()
    {
        return activityType;
    }

    public long getDefaultTimeoutMillis()
    {
        return defaultTimeoutMillis;
    }

    public List<String> getExpectedResponses()
    {
        return expectedResponses;
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

    private static List<String> immutableList(List<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }
}
