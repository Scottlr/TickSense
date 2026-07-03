package com.ticksense.analytics;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MetricValueMap
{
    private final Map<String, MetricValue> values = new LinkedHashMap<>();

    private MetricValueMap()
    {
    }

    public static MetricValueMap builder()
    {
        return new MetricValueMap();
    }

    public MetricValueMap put(MetricDefinition definition, double value)
    {
        values.put(definition.getKey(), new MetricValue(definition, value));
        return this;
    }

    public Map<String, MetricValue> build()
    {
        return new LinkedHashMap<>(values);
    }
}
