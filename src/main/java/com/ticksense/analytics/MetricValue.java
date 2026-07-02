package com.ticksense.analytics;

import java.util.Objects;

public final class MetricValue
{
    private final MetricDefinition definition;
    private final double value;

    public MetricValue(MetricDefinition definition, double value)
    {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.value = requireFinite(value);
    }

    public MetricDefinition getDefinition()
    {
        return definition;
    }

    public double getValue()
    {
        return value;
    }

    public MetricUnit getUnit()
    {
        return definition.getUnit();
    }

    private static double requireFinite(double value)
    {
        if (Double.isNaN(value) || Double.isInfinite(value))
        {
            throw new IllegalArgumentException("value must be finite");
        }
        return value;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof MetricValue))
        {
            return false;
        }
        final MetricValue that = (MetricValue) other;
        return Double.compare(that.value, value) == 0
            && definition.equals(that.definition);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(definition, value);
    }
}
