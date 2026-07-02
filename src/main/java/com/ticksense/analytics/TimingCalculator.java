package com.ticksense.analytics;

import com.ticksense.core.EventTime;
import java.util.Objects;

public final class TimingCalculator
{
    public int latencyTicks(EventTime start, EventTime end)
    {
        return difference(
            Objects.requireNonNull(start, "start").getGameTick(),
            Objects.requireNonNull(end, "end").getGameTick(),
            "game tick");
    }

    public long latencyMillis(EventTime start, EventTime end)
    {
        return difference(
            Objects.requireNonNull(start, "start").getWallTimeMillis(),
            Objects.requireNonNull(end, "end").getWallTimeMillis(),
            "wall time");
    }

    public long latencyFromMonotonicNanos(EventTime start, EventTime end)
    {
        return difference(
            Objects.requireNonNull(start, "start").getMonotonicNanos(),
            Objects.requireNonNull(end, "end").getMonotonicNanos(),
            "monotonic nanos");
    }

    public double downtimeSeconds(EventTime start, EventTime end)
    {
        return latencyMillis(start, end) / 1_000.0D;
    }

    public int tickLoss(int expectedCompletionTick, EventTime observedCompletion)
    {
        return difference(
            expectedCompletionTick,
            Objects.requireNonNull(observedCompletion, "observedCompletion").getGameTick(),
            "expected completion tick");
    }

    public MetricValue metricTicks(String key, EventTime start, EventTime end)
    {
        return new MetricValue(definition(key, MetricUnit.TICKS), latencyTicks(start, end));
    }

    public MetricValue metricMilliseconds(String key, EventTime start, EventTime end)
    {
        return new MetricValue(definition(key, MetricUnit.MILLISECONDS), latencyMillis(start, end));
    }

    public MetricValue metricSeconds(String key, EventTime start, EventTime end)
    {
        return new MetricValue(definition(key, MetricUnit.SECONDS), downtimeSeconds(start, end));
    }

    private static MetricDefinition definition(String key, MetricUnit unit)
    {
        return new MetricDefinition(key, key, unit, "", true);
    }

    private static int difference(int start, int end, String label)
    {
        if (end < start)
        {
            throw new IllegalArgumentException(label + " end must not be before start");
        }
        return end - start;
    }

    private static long difference(long start, long end, String label)
    {
        if (end < start)
        {
            throw new IllegalArgumentException(label + " end must not be before start");
        }
        return end - start;
    }
}
