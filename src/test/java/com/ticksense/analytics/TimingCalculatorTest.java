package com.ticksense.analytics;

import static org.junit.Assert.assertEquals;

import com.ticksense.core.EventTime;
import org.junit.Test;

public class TimingCalculatorTest
{
    private final TimingCalculator timingCalculator = new TimingCalculator();

    @Test
    public void computesTickAndMillisLatency()
    {
        final EventTime start = time(1_000L, 2_000_000_000L, 100);
        final EventTime end = time(2_200L, 3_250_000_000L, 103);

        assertEquals(3, timingCalculator.latencyTicks(start, end));
        assertEquals(1_200L, timingCalculator.latencyMillis(start, end));
        assertEquals(1.2D, timingCalculator.downtimeSeconds(start, end), 0.0001D);
        assertEquals(3.0D, timingCalculator.metricTicks("responseLatencyTicks", start, end).getValue(), 0.0D);
        assertEquals(1_200.0D, timingCalculator.metricMilliseconds("responseLatencyMillis", start, end).getValue(), 0.0D);
    }

    @Test
    public void separatesWallTimeAndMonotonicDuration()
    {
        final EventTime start = time(10_000L, 100_000_000L, 200);
        final EventTime end = time(10_450L, 875_000_000L, 201);

        assertEquals(450L, timingCalculator.latencyMillis(start, end));
        assertEquals(775_000_000L, timingCalculator.latencyFromMonotonicNanos(start, end));
    }

    private static EventTime time(long wallTimeMillis, long monotonicNanos, int gameTick)
    {
        return new EventTime(wallTimeMillis, monotonicNanos, gameTick, gameTick * 30L, gameTick * 2);
    }
}
