package com.ticksense.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class PercentileCalculator
{
    public double percentile(List<Double> samples, double percentile)
    {
        final List<Double> sorted = sortedSamples(samples);
        validatePercentile(percentile);

        if (percentile == 0.0D)
        {
            return sorted.get(0);
        }

        final int rank = (int) Math.ceil((percentile / 100.0D) * sorted.size());
        return sorted.get(Math.max(1, rank) - 1);
    }

    public MetricValue percentileMetric(MetricSeries series, String key, String displayName, double percentile)
    {
        final MetricSeries normalizedSeries = Objects.requireNonNull(series, "series");
        final MetricDefinition definition = new MetricDefinition(
            key,
            displayName,
            normalizedSeries.getDefinition().getUnit(),
            "Percentile derived from " + normalizedSeries.getDefinition().getKey(),
            normalizedSeries.getDefinition().isLowerValueBetter());
        return new MetricValue(definition, percentile(normalizedSeries.rawValues(), percentile));
    }

    public MetricValue p50(MetricSeries series, String key, String displayName)
    {
        return percentileMetric(series, key, displayName, 50.0D);
    }

    public MetricValue p90(MetricSeries series, String key, String displayName)
    {
        return percentileMetric(series, key, displayName, 90.0D);
    }

    public MetricValue p95(MetricSeries series, String key, String displayName)
    {
        return percentileMetric(series, key, displayName, 95.0D);
    }

    private static List<Double> sortedSamples(List<Double> samples)
    {
        if (samples == null || samples.isEmpty())
        {
            throw new IllegalArgumentException("samples must not be empty");
        }

        final List<Double> sorted = new ArrayList<>(samples.size());
        for (Double sample : samples)
        {
            if (sample == null || Double.isNaN(sample) || Double.isInfinite(sample))
            {
                throw new IllegalArgumentException("samples must contain only finite values");
            }
            sorted.add(sample);
        }
        Collections.sort(sorted);
        return sorted;
    }

    private static void validatePercentile(double percentile)
    {
        if (Double.isNaN(percentile) || percentile < 0.0D || percentile > 100.0D)
        {
            throw new IllegalArgumentException("percentile must be between 0 and 100");
        }
    }
}
