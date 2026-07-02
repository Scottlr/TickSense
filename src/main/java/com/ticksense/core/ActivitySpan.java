package com.ticksense.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ActivitySpan
{
    private final String spanType;
    private final EventTime startTime;
    private final EventTime endTime;
    private final Map<String, String> metadata;

    public ActivitySpan(String spanType, EventTime startTime, EventTime endTime, Map<String, String> metadata)
    {
        this.spanType = requireText(spanType, "spanType");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = endTime;
        this.metadata = immutableCopy(metadata);
    }

    public ActivitySpan finish(EventTime endTime)
    {
        return new ActivitySpan(spanType, startTime, Objects.requireNonNull(endTime, "endTime"), metadata);
    }

    public boolean isFinished()
    {
        return endTime != null;
    }

    public String getSpanType()
    {
        return spanType;
    }

    public EventTime getStartTime()
    {
        return startTime;
    }

    public EventTime getEndTime()
    {
        return endTime;
    }

    public Map<String, String> getMetadata()
    {
        return metadata;
    }

    private static Map<String, String> immutableCopy(Map<String, String> source)
    {
        if (source == null || source.isEmpty())
        {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
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

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof ActivitySpan))
        {
            return false;
        }
        final ActivitySpan that = (ActivitySpan) other;
        return spanType.equals(that.spanType)
            && startTime.equals(that.startTime)
            && Objects.equals(endTime, that.endTime)
            && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(spanType, startTime, endTime, metadata);
    }
}
