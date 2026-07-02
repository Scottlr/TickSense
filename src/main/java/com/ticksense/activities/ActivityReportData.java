package com.ticksense.activities;

import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ActivityReportData
{
    private final ActivityId activityId;
    private final ActivityType activityType;
    private final Map<String, String> attributes;

    public ActivityReportData(ActivityId activityId, ActivityType activityType, Map<String, String> attributes)
    {
        this.activityId = Objects.requireNonNull(activityId, "activityId");
        this.activityType = Objects.requireNonNull(activityType, "activityType");
        this.attributes = immutableMap(attributes);
    }

    public ActivityId getActivityId()
    {
        return activityId;
    }

    public ActivityType getActivityType()
    {
        return activityType;
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }

    private static Map<String, String> immutableMap(Map<String, String> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
