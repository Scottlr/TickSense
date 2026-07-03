package com.ticksense.activities;

import com.ticksense.core.ActivityType;
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
        this.id = ActivityTexts.requireText(id, "id");
        this.displayName = ActivityTexts.requireText(displayName, "displayName");
        this.activityType = Objects.requireNonNull(activityType, "activityType");
        if (defaultTimeoutMillis < 0L)
        {
            throw new IllegalArgumentException("defaultTimeoutMillis must be >= 0");
        }
        this.defaultTimeoutMillis = defaultTimeoutMillis;
        this.expectedResponses = ActivityCollections.immutableList(expectedResponses);
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
}
