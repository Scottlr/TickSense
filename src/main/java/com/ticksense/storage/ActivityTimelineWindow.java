package com.ticksense.storage;

import com.ticksense.core.EventTime;
import java.util.Objects;

public final class ActivityTimelineWindow
{
    private final EventTime startTime;
    private final EventTime endTime;

    public ActivityTimelineWindow(EventTime startTime, EventTime endTime)
    {
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = Objects.requireNonNull(endTime, "endTime");
    }

    public EventTime getStartTime()
    {
        return startTime;
    }

    public EventTime getEndTime()
    {
        return endTime;
    }

    public boolean contains(EventTime time)
    {
        final long millis = Objects.requireNonNull(time, "time").getWallTimeMillis();
        return millis >= startTime.getWallTimeMillis() && millis <= endTime.getWallTimeMillis();
    }
}
