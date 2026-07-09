package com.ticksense.activities.construction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.OpportunityEvidence;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.MetricValue;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import com.ticksense.core.FinishReason;
import com.ticksense.core.FinishReasonType;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.ObjectID;
import net.runelite.api.widgets.WidgetID;
import org.junit.Test;

public class ConstructionAnalyzerTest
{
    private final ConstructionAnalyzer analyzer = new ConstructionAnalyzer();

    @Test
    public void reportsMenuAndCycleMetrics()
    {
        final ActivityReport report = analyzer.buildReport(
            session(),
            activityData(),
            markers(
                opportunityMarkers("menu-1", ConstructionState.OPPORTUNITY_MENU_LATENCY, 401, 402, "Verified menu click followed menu-open evidence.", "Build", "Larder space"),
                opportunityMarkers("menu-2", ConstructionState.OPPORTUNITY_MENU_LATENCY, 415, 416, "Verified menu click followed menu-open evidence.", "Remove", "Oak larder"),
                opportunityMarkers("cadence-1", ConstructionState.OPPORTUNITY_BUILD_REMOVE_CADENCE, 403, 416, "Verified remove click followed the last build confirmation."),
                opportunityMarkers("cycle-1", ConstructionState.OPPORTUNITY_INVENTORY_CYCLE, 402, 439, "Verified bank widget completed the construction inventory cycle."),
                opportunityMarkers("bank-1", ConstructionState.OPPORTUNITY_BANKING_DOWNTIME, 405, 439, "Verified bank widget ended Construction downtime.")));

        assertEquals("Construction", report.getDetectedActivityName());
        assertEquals(FinishReasonType.BANK_OPENED, report.getFinishReason().getType());
        assertEquals(0.92D, report.getConfidence(), 0.0D);

        final Map<String, MetricValue> metrics = report.getMetrics();
        assertEquals(1.0D, metrics.get("menuLatency").getValue(), 0.0D);
        assertEquals(13.0D, metrics.get("buildRemoveCadence").getValue(), 0.0D);
        assertEquals(34.0D, metrics.get("bankingDowntime").getValue(), 0.0D);
        assertEquals(37.0D, metrics.get("inventoryCycle").getValue(), 0.0D);
        assertEquals(33.75D, metrics.get("executionScore").getValue(), 0.0D);

        assertEquals(3, report.getTickLossBreakdown().getCategories().size());
        assertEquals(48, report.getTickLossBreakdown().getTotalTickLoss());
        assertEquals(4, report.getOpportunities().size());
        assertEquals("Best execution: Menu latency 1 ticks", report.getSummaryLines().get(0));
        assertEquals("Longest cycle: Inventory cycle 37 ticks", report.getSummaryLines().get(1));
        assertTrue(report.getEvidenceSummary().stream().anyMatch(line -> line.toLowerCase().contains("observe-only")));
    }

    private static ActivitySession session()
    {
        final Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("displayName", "Construction");
        metadata.put("confidence", "0.92");
        metadata.put("evidenceSummary", "Construction verification decision is VERIFIED for method oak-larder. | Verified build spot " + ObjectID.LARDER_SPACE + " is available at 3110,3497. | Observed verified Build click on Larder space at region 7513.");
        return new ActivitySession(
            ActivityId.of("construction-session-1"),
            ActivityType.CONSTRUCTION,
            time(400),
            time(439),
            new FinishReason(
                FinishReasonType.BANK_OPENED,
                time(439),
                0.95D,
                "Construction ended because verified banking evidence completed the oak-larder cycle.",
                Collections.singletonList("Bank widget group " + WidgetID.BANK_GROUP_ID + " loaded after the verified build/remove flow.")),
            Collections.emptyList(),
            metadata);
    }

    private static ActivityReportData activityData()
    {
        final Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("verificationStatus", "VERIFIED");
        attributes.put("methodName", "oak-larder");
        attributes.put("menuLatencyCount", "1");
        attributes.put("buildRemoveCadenceCount", "1");
        attributes.put("inventoryCycleCount", "1");
        attributes.put("bankingDowntimeCount", "1");
        return new ActivityReportData(ActivityId.of("construction-session-1"), ActivityType.CONSTRUCTION, attributes);
    }

    private static List<OpportunityMarker> opportunityMarkers(String instanceId, String type, int startTick, int endTick, String detail)
    {
        return opportunityMarkers(instanceId, type, startTick, endTick, detail, "", "");
    }

    private static List<OpportunityMarker> opportunityMarkers(String instanceId, String type, int startTick, int endTick, String detail, String option, String target)
    {
        final Map<String, String> context = new LinkedHashMap<>();
        context.put("method", "oak-larder");
        if (!option.isEmpty())
        {
            context.put("option", option);
        }
        if (!target.isEmpty())
        {
            context.put("target", target);
        }
        return Arrays.asList(
            new OpportunityMarker(
                "marker-open-" + instanceId,
                instanceId,
                ActivityId.of("construction-session-1"),
                type,
                OpportunityStatus.OPEN,
                time(startTick),
                context,
                Collections.emptyList()),
            new OpportunityMarker(
                "marker-completed-" + instanceId,
                instanceId,
                ActivityId.of("construction-session-1"),
                type,
                OpportunityStatus.COMPLETED,
                time(endTick),
                context,
                Collections.singletonList(new OpportunityEvidence(time(endTick), type, com.ticksense.activities.EvidenceStrength.CONFIRMING, detail))));
    }

    @SafeVarargs
    private static List<OpportunityMarker> markers(List<OpportunityMarker>... groups)
    {
        final List<OpportunityMarker> markers = new java.util.ArrayList<>();
        for (List<OpportunityMarker> group : groups)
        {
            markers.addAll(group);
        }
        return markers;
    }

    private static EventTime time(int tick)
    {
        final long wallTimeMillis = 1_000L + (tick * 600L);
        return new EventTime(wallTimeMillis, wallTimeMillis * 1_000_000L, tick, tick * 20L, tick);
    }
}
