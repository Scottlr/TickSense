package com.ticksense.activities;

import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ActivityMarker
{
    private final String markerId;
    private final ActivityId activityId;
    private final ActivityType activityType;
    private final String markerType;
    private final EventTime time;
    private final Map<String, String> metadata;

    public ActivityMarker(
        String markerId,
        ActivityId activityId,
        ActivityType activityType,
        String markerType,
        EventTime time,
        Map<String, String> metadata)
    {
        this.markerId = requireText(markerId, "markerId");
        this.activityId = Objects.requireNonNull(activityId, "activityId");
        this.activityType = Objects.requireNonNull(activityType, "activityType");
        this.markerType = requireText(markerType, "markerType");
        this.time = Objects.requireNonNull(time, "time");
        this.metadata = immutableMap(metadata);
    }

    public String getMarkerId()
    {
        return markerId;
    }

    public ActivityId getActivityId()
    {
        return activityId;
    }

    public ActivityType getActivityType()
    {
        return activityType;
    }

    public String getMarkerType()
    {
        return markerType;
    }

    public EventTime getTime()
    {
        return time;
    }

    public Map<String, String> getMetadata()
    {
        return metadata;
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

    private static Map<String, String> immutableMap(Map<String, String> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
