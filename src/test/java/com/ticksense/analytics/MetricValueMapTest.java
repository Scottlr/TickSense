package com.ticksense.analytics;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import org.junit.Test;

public class MetricValueMapTest
{
    @Test
    public void keysValuesByMetricDefinition()
    {
        final MetricDefinition latency = new MetricDefinition("latency", "Latency", MetricUnit.TICKS, "Latency");

        final Map<String, MetricValue> values = MetricValueMap.builder()
            .put(latency, 3.0D)
            .build();

        assertEquals(1, values.size());
        assertEquals(3.0D, values.get("latency").getValue(), 0.0D);
        assertEquals(latency, values.get("latency").getDefinition());
    }
}
