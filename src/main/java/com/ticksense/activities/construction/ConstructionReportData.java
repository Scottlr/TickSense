package com.ticksense.activities.construction;

import com.ticksense.analytics.ActivityAnalysisData;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.OpportunityTimelineEntry;
import com.ticksense.analytics.TickLossBreakdown;
import java.util.List;
import java.util.Map;

public final class ConstructionReportData extends ActivityAnalysisData
{
    public ConstructionReportData(
        Map<String, MetricValue> metrics,
        List<OpportunityTimelineEntry> opportunityTimeline,
        TickLossBreakdown tickLossBreakdown,
        List<String> evidenceSummary,
        List<String> summaryLines)
    {
        super(metrics, opportunityTimeline, tickLossBreakdown, evidenceSummary, summaryLines);
    }
}
