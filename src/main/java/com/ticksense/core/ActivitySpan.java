package com.ticksense.core;

import com.ticksense.common.TextValues;
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
        this.spanType = TextValues.requireText(spanType, "spanType");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = endTime;
        this.metadata = CoreCollections.immutableStringMap(metadata);
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
