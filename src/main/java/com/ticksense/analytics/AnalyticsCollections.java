package com.ticksense.analytics;

import com.ticksense.common.ImmutableCollections;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class AnalyticsCollections
{
    private AnalyticsCollections()
    {
    }

    static List<String> immutableTextList(List<String> values, String fieldName)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<String> copied = new ArrayList<>(values.size());
        for (String value : values)
        {
            copied.add(AnalyticsTexts.requireText(value, fieldName + " value"));
        }
        return Collections.unmodifiableList(copied);
    }

    static <T> List<T> immutableList(List<T> values)
    {
        return ImmutableCollections.immutableList(values);
    }

    static Map<String, MetricValue> immutableMetricValues(Map<String, MetricValue> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyMap();
        }

        final Map<String, MetricValue> copied = new LinkedHashMap<>();
        for (Map.Entry<String, MetricValue> entry : values.entrySet())
        {
            copied.put(
                AnalyticsTexts.requireText(entry.getKey(), "metric key"),
                Objects.requireNonNull(entry.getValue(), "metric value"));
        }
        return Collections.unmodifiableMap(copied);
    }

    static <K, V> Map<K, V> immutableMap(Map<K, V> values)
    {
        return ImmutableCollections.immutableMap(values);
    }

    static List<OpportunityTimelineEntry> immutableTimelineEntries(List<OpportunityTimelineEntry> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<OpportunityTimelineEntry> copied = new ArrayList<>(values.size());
        for (OpportunityTimelineEntry entry : values)
        {
            copied.add(Objects.requireNonNull(entry, "opportunityTimeline entry"));
        }
        return Collections.unmodifiableList(copied);
    }
}
