package com.ticksense.analytics;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class PercentileCalculatorTest
{
    private final PercentileCalculator percentileCalculator = new PercentileCalculator();

    @Test
    public void computesP50P90P95Deterministically()
    {
        final MetricDefinition definition = new MetricDefinition(
            "responseLatencyMillis",
            "Response latency",
            MetricUnit.MILLISECONDS,
            "Player response latency");
        final MetricSeries series = MetricSeries.of(definition, Arrays.asList(120.0D, 240.0D, 360.0D));

        assertEquals(240.0D, percentileCalculator.p50(series, "responseLatencyP50", "Response latency p50").getValue(), 0.0D);
        assertEquals(360.0D, percentileCalculator.p90(series, "responseLatencyP90", "Response latency p90").getValue(), 0.0D);
        assertEquals(360.0D, percentileCalculator.p95(series, "responseLatencyP95", "Response latency p95").getValue(), 0.0D);
    }
}
