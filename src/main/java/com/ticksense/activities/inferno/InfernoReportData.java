package com.ticksense.activities.inferno;

import com.ticksense.analytics.ActivityAnalysisData;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.OpportunityTimelineEntry;
import com.ticksense.analytics.TickLossBreakdown;
import java.util.List;
import java.util.Map;

public final class InfernoReportData extends ActivityAnalysisData
{
    public InfernoReportData(
        Map<String, MetricValue> metrics,
        List<OpportunityTimelineEntry> opportunityTimeline,
        TickLossBreakdown tickLossBreakdown,
        List<String> evidenceSummary,
        List<String> summaryLines)
    {
        super(metrics, opportunityTimeline, tickLossBreakdown, evidenceSummary, summaryLines);
    }
}
