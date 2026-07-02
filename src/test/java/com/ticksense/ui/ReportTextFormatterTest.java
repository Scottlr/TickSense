package com.ticksense.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.MetricDefinition;
import com.ticksense.analytics.MetricUnit;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.OpportunityTimelineEntry;
import com.ticksense.analytics.TickLossBreakdown;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import com.ticksense.core.FinishReason;
import com.ticksense.core.FinishReasonType;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

public class ReportTextFormatterTest
{
    @Test
    public void formatsDurationAndFinishReasonForPlayerFacingText()
    {
        assertEquals("2:11.4 / 219 ticks", ReportTextFormatter.formatDuration(131_400L, 219));
        assertEquals("Idle Timeout", ReportTextFormatter.formatFinishReason(FinishReasonType.IDLE_TIMEOUT));
    }

    @Test
    public void derivesGradeFromScoreMetrics()
    {
        final Map<String, MetricValue> metrics = new LinkedHashMap<>();
        metrics.put(
            "executionScore",
            new MetricValue(new MetricDefinition("executionScore", "Execution score", MetricUnit.SCORE, "Overall score"), 88.0D));

        assertEquals("A", ReportTextFormatter.gradeForMetrics(metrics));
    }

    @Test
    public void formatsActivityReportHighlights()
    {
        final ActivityReport report = sampleReport();

        assertEquals("Click same tick as rock respawned", ReportTextFormatter.bestExecution(report));
        assertEquals("Lost 2 ticks on bank turn", ReportTextFormatter.worstExecution(report));
        assertEquals("Tick 120  Mine rock (+2 ticks)  COMPLETED", ReportTextFormatter.timelineEntry(report.getOpportunities().get(0)));
        assertTrue(ReportTextFormatter.evidenceText(report.getEvidenceSummary()).contains("Gem rock"));
    }

    private static ActivityReport sampleReport()
    {
        final Map<String, MetricValue> metrics = new LinkedHashMap<>();
        metrics.put(
            "totalTickLoss",
            new MetricValue(new MetricDefinition("totalTickLoss", "Total tick loss", MetricUnit.TICKS, "Lost time"), 3.0D));
        metrics.put(
            "executionScore",
            new MetricValue(new MetricDefinition("executionScore", "Execution score", MetricUnit.SCORE, "Overall score"), 88.0D));

        return new ActivityReport(
            ActivityReport.SCHEMA_VERSION,
            "report-1",
            ActivityId.of("activity-1"),
            ActivityType.GEM_MINING,
            "Gem mining",
            1_783_010_000_123L,
            219,
            131_400L,
            new FinishReason(FinishReasonType.COMPLETED, new EventTime(1L, 2L, 3, 4L, 5), 0.92D, "Finished", Collections.singletonList("Bag full")),
            0.92D,
            Arrays.asList("Gem rock click", "Uncut ruby gained"),
            metrics,
            Collections.singletonList(new OpportunityTimelineEntry(
                "GEM_ROCK_RESPAWN_TO_CLICK",
                "Mine rock",
                "COMPLETED",
                120,
                1_783_010_010_000L,
                2,
                1_200L,
                Collections.singletonList("Rock returned"))),
            new TickLossBreakdown(3, Collections.singletonMap("Movement", 3)),
            Arrays.asList("Click same tick as rock respawned", "Lost 2 ticks on bank turn"));
    }
}
