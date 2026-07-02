package com.ticksense.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class MetricSeries
{
    private final MetricDefinition definition;
    private final List<MetricValue> values;

    public MetricSeries(MetricDefinition definition, List<MetricValue> values)
    {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.values = immutableValues(definition, values);
    }

    public static MetricSeries of(MetricDefinition definition, List<Double> values)
    {
        final List<MetricValue> metricValues = new ArrayList<>();
        if (values != null)
        {
            for (Double value : values)
            {
                metricValues.add(new MetricValue(definition, value));
            }
        }
        return new MetricSeries(definition, metricValues);
    }

    public MetricDefinition getDefinition()
    {
        return definition;
    }

    public List<MetricValue> getValues()
    {
        return values;
    }

    public int count()
    {
        return values.size();
    }

    public Optional<MetricValue> best()
    {
        if (values.isEmpty())
        {
            return Optional.empty();
        }

        MetricValue best = values.get(0);
        for (MetricValue value : values)
        {
            if (definition.isLowerValueBetter())
            {
                if (value.getValue() < best.getValue())
                {
                    best = value;
                }
            }
            else if (value.getValue() > best.getValue())
            {
                best = value;
            }
        }
        return Optional.of(best);
    }

    public Optional<MetricValue> worst()
    {
        if (values.isEmpty())
        {
            return Optional.empty();
        }

        MetricValue worst = values.get(0);
        for (MetricValue value : values)
        {
            if (definition.isLowerValueBetter())
            {
                if (value.getValue() > worst.getValue())
                {
                    worst = value;
                }
            }
            else if (value.getValue() < worst.getValue())
            {
                worst = value;
            }
        }
        return Optional.of(worst);
    }

    public double average()
    {
        if (values.isEmpty())
        {
            throw new IllegalStateException("Cannot average an empty metric series");
        }

        double total = 0.0D;
        for (MetricValue value : values)
        {
            total += value.getValue();
        }
        return total / values.size();
    }

    List<Double> rawValues()
    {
        final List<Double> raw = new ArrayList<>(values.size());
        for (MetricValue value : values)
        {
            raw.add(value.getValue());
        }
        return raw;
    }

    private static List<MetricValue> immutableValues(MetricDefinition definition, List<MetricValue> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<MetricValue> copied = new ArrayList<>(values.size());
        for (MetricValue value : values)
        {
            final MetricValue normalized = Objects.requireNonNull(value, "value");
            if (!definition.equals(normalized.getDefinition()))
            {
                throw new IllegalArgumentException("MetricValue definition must match series definition");
            }
            copied.add(normalized);
        }
        return Collections.unmodifiableList(copied);
    }
}
