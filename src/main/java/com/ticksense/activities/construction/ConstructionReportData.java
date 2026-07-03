package com.ticksense.activities.construction;

import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.OpportunityTimelineEntry;
import com.ticksense.analytics.TickLossBreakdown;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ConstructionReportData
{
    private final Map<String, MetricValue> metrics;
    private final List<OpportunityTimelineEntry> opportunityTimeline;
    private final TickLossBreakdown tickLossBreakdown;
    private final List<String> evidenceSummary;
    private final List<String> summaryLines;

    public ConstructionReportData(
        Map<String, MetricValue> metrics,
        List<OpportunityTimelineEntry> opportunityTimeline,
        TickLossBreakdown tickLossBreakdown,
        List<String> evidenceSummary,
        List<String> summaryLines)
    {
        this.metrics = immutableMetrics(metrics);
        this.opportunityTimeline = immutableTimeline(opportunityTimeline);
        this.tickLossBreakdown = Objects.requireNonNull(tickLossBreakdown, "tickLossBreakdown");
        this.evidenceSummary = immutableStrings(evidenceSummary);
        this.summaryLines = immutableStrings(summaryLines);
    }

    public Map<String, MetricValue> getMetrics()
    {
        return metrics;
    }

    public List<OpportunityTimelineEntry> getOpportunityTimeline()
    {
        return opportunityTimeline;
    }

    public TickLossBreakdown getTickLossBreakdown()
    {
        return tickLossBreakdown;
    }

    public List<String> getEvidenceSummary()
    {
        return evidenceSummary;
    }

    public List<String> getSummaryLines()
    {
        return summaryLines;
    }

    private static Map<String, MetricValue> immutableMetrics(Map<String, MetricValue> source)
    {
        if (source == null || source.isEmpty())
        {
            return Collections.emptyMap();
        }
        final Map<String, MetricValue> copied = new LinkedHashMap<>();
        for (Map.Entry<String, MetricValue> entry : source.entrySet())
        {
            copied.put(requireText(entry.getKey(), "metric key"), Objects.requireNonNull(entry.getValue(), "metric"));
        }
        return Collections.unmodifiableMap(copied);
    }

    private static List<OpportunityTimelineEntry> immutableTimeline(List<OpportunityTimelineEntry> source)
    {
        if (source == null || source.isEmpty())
        {
            return Collections.emptyList();
        }
        final List<OpportunityTimelineEntry> copied = new ArrayList<>(source.size());
        for (OpportunityTimelineEntry entry : source)
        {
            copied.add(Objects.requireNonNull(entry, "opportunityTimeline entry"));
        }
        return Collections.unmodifiableList(copied);
    }

    private static List<String> immutableStrings(List<String> source)
    {
        if (source == null || source.isEmpty())
        {
            return Collections.emptyList();
        }
        final List<String> copied = new ArrayList<>(source.size());
        for (String value : source)
        {
            copied.add(requireText(value, "string value"));
        }
        return Collections.unmodifiableList(copied);
    }

    private static String requireText(String value, String fieldName)
    {
        final String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
