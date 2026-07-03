package com.ticksense.analytics;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class ActivityAnalysisData
{
    private final Map<String, MetricValue> metrics;
    private final List<OpportunityTimelineEntry> opportunityTimeline;
    private final TickLossBreakdown tickLossBreakdown;
    private final List<String> evidenceSummary;
    private final List<String> summaryLines;

    protected ActivityAnalysisData(
        Map<String, MetricValue> metrics,
        List<OpportunityTimelineEntry> opportunityTimeline,
        TickLossBreakdown tickLossBreakdown,
        List<String> evidenceSummary,
        List<String> summaryLines)
    {
        this.metrics = AnalyticsCollections.immutableMetricValues(metrics);
        this.opportunityTimeline = AnalyticsCollections.immutableTimelineEntries(opportunityTimeline);
        this.tickLossBreakdown = Objects.requireNonNull(tickLossBreakdown, "tickLossBreakdown");
        this.evidenceSummary = AnalyticsCollections.immutableTextList(evidenceSummary, "evidenceSummary");
        this.summaryLines = AnalyticsCollections.immutableTextList(summaryLines, "summaryLines");
    }

    public final Map<String, MetricValue> getMetrics()
    {
        return metrics;
    }

    public final List<OpportunityTimelineEntry> getOpportunityTimeline()
    {
        return opportunityTimeline;
    }

    public final TickLossBreakdown getTickLossBreakdown()
    {
        return tickLossBreakdown;
    }

    public final List<String> getEvidenceSummary()
    {
        return evidenceSummary;
    }

    public final List<String> getSummaryLines()
    {
        return summaryLines;
    }
}
