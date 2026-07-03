package com.ticksense.core;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ActivitySession
{
    private final ActivityId activityId;
    private final ActivityType activityType;
    private final EventTime startTime;
    private final EventTime endTime;
    private final FinishReason finishReason;
    private final List<ActivitySpan> spans;
    private final Map<String, String> metadata;

    public ActivitySession(
        ActivityId activityId,
        ActivityType activityType,
        EventTime startTime,
        EventTime endTime,
        FinishReason finishReason,
        List<ActivitySpan> spans,
        Map<String, String> metadata)
    {
        this.activityId = Objects.requireNonNull(activityId, "activityId");
        this.activityType = Objects.requireNonNull(activityType, "activityType");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = endTime;
        this.finishReason = finishReason;
        validateFinishState(endTime, finishReason);
        this.spans = CoreCollections.immutableList(spans);
        this.metadata = CoreCollections.immutableStringMap(metadata);
    }

    public ActivitySession finish(FinishReason finishReason)
    {
        final FinishReason reason = Objects.requireNonNull(finishReason, "finishReason");
        return new ActivitySession(activityId, activityType, startTime, reason.getTime(), reason, spans, metadata);
    }

    public boolean isFinished()
    {
        return finishReason != null;
    }

    public ActivityId getActivityId()
    {
        return activityId;
    }

    public ActivityType getActivityType()
    {
        return activityType;
    }

    public EventTime getStartTime()
    {
        return startTime;
    }

    public EventTime getEndTime()
    {
        return endTime;
    }

    public FinishReason getFinishReason()
    {
        return finishReason;
    }

    public List<ActivitySpan> getSpans()
    {
        return spans;
    }

    public Map<String, String> getMetadata()
    {
        return metadata;
    }

    private static void validateFinishState(EventTime endTime, FinishReason finishReason)
    {
        if ((endTime == null) != (finishReason == null))
        {
            throw new IllegalArgumentException("endTime and finishReason must be provided together");
        }
        if (finishReason != null && !finishReason.getTime().equals(endTime))
        {
            throw new IllegalArgumentException("finishReason time must match endTime");
        }
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof ActivitySession))
        {
            return false;
        }
        final ActivitySession that = (ActivitySession) other;
        return activityId.equals(that.activityId)
            && activityType == that.activityType
            && startTime.equals(that.startTime)
            && Objects.equals(endTime, that.endTime)
            && Objects.equals(finishReason, that.finishReason)
            && spans.equals(that.spans)
            && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(activityId, activityType, startTime, endTime, finishReason, spans, metadata);
    }
}
