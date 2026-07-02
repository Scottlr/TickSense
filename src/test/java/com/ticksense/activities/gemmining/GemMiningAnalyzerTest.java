package com.ticksense.activities.gemmining;

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
import org.junit.Test;

public class GemMiningAnalyzerTest
{
    private final GemMiningAnalyzer analyzer = new GemMiningAnalyzer();

    @Test
    public void reportsLatencyAndIdleTicks()
    {
        final ActivityReport report = analyzer.buildReport(
            session("0.92", "Verified gem rock became available | Mine click targeted that verified gem rock."),
            activityData(3, 2),
            markers(
                opportunityMarkers("respawn-1", GemMiningState.OPPORTUNITY_RESPAWN_TO_CLICK, 200, 202, "Mine click on available gem rock."),
                opportunityMarkers("respawn-2", GemMiningState.OPPORTUNITY_RESPAWN_TO_CLICK, 210, 211, "Mine click on available gem rock."),
                opportunityMarkers("move-1", GemMiningState.OPPORTUNITY_MOVEMENT_TO_ROCK, 201, 202, "Player moved into range before mining.")));

        assertEquals("Gem Mining", report.getDetectedActivityName());
        assertEquals(FinishReasonType.LEFT_REGION, report.getFinishReason().getType());
        assertEquals(0.92D, report.getConfidence(), 0.0D);

        final Map<String, MetricValue> metrics = report.getMetrics();
        assertEquals("Rock response", metrics.get("rockResponse").getDefinition().getDisplayName());
        assertEquals(1.5D, metrics.get("rockResponse").getValue(), 0.0D);
        assertEquals("Idle ticks", metrics.get("idleTicks").getDefinition().getDisplayName());
        assertEquals(3.0D, metrics.get("idleTicks").getValue(), 0.0D);
        assertEquals("Redundant clicks", metrics.get("redundantClicks").getDefinition().getDisplayName());
        assertEquals(2.0D, metrics.get("redundantClicks").getValue(), 0.0D);
        assertEquals("Movement latency", metrics.get("movementLatency").getDefinition().getDisplayName());
        assertEquals(1.0D, metrics.get("movementLatency").getValue(), 0.0D);
        assertEquals("Cycle consistency", metrics.get("cycleConsistency").getDefinition().getDisplayName());
        assertEquals(0.5D, metrics.get("cycleConsistency").getValue(), 0.0D);

        assertEquals(3, report.getOpportunities().size());
        assertEquals(6, report.getTickLossBreakdown().getTotalTickLoss());
        assertEquals("Best execution: Rock response 1 ticks", report.getSummaryLines().get(0));
        assertEquals("Worst execution: Rock response 2 ticks", report.getSummaryLines().get(1));
    }

    @Test
    public void separatesExecutionLossFromMiningRng()
    {
        final ActivityReport report = analyzer.buildReport(
            session("0.92", "Verified gem rock became available | Mine click targeted that verified gem rock."),
            activityData(0, 0),
            markers(
                opportunityMarkers("respawn-1", GemMiningState.OPPORTUNITY_RESPAWN_TO_CLICK, 200, 200, "Mine click on available gem rock.")));

        assertEquals(0, report.getTickLossBreakdown().getTotalTickLoss());
        assertEquals(0.0D, report.getMetrics().get("idleTicks").getValue(), 0.0D);
        assertTrue(report.getEvidenceSummary().stream().anyMatch(line -> line.contains("depleted-rock wait")));
        assertTrue(report.getSummaryLines().get(0).contains("Rock response 0 ticks"));
    }

    private static ActivitySession session(String confidence, String evidenceSummary)
    {
        final Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("displayName", "Gem Mining");
        metadata.put("confidence", confidence);
        metadata.put("evidenceSummary", evidenceSummary);
        return new ActivitySession(
            ActivityId.of("gem-mining-session-1"),
            ActivityType.GEM_MINING,
            time(200),
            time(240),
            new FinishReason(
                FinishReasonType.LEFT_REGION,
                time(240),
                0.92D,
                "Gem mining ended because the player left the verified gem-mining region.",
                Collections.singletonList("Region changed from verified gem mine to 12000.")),
            Collections.emptyList(),
            metadata);
    }

    private static ActivityReportData activityData(int idleTicks, int redundantClicks)
    {
        final Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("verificationStatus", "VERIFIED");
        attributes.put("idleTicks", String.valueOf(idleTicks));
        attributes.put("redundantClicks", String.valueOf(redundantClicks));
        return new ActivityReportData(ActivityId.of("gem-mining-session-1"), ActivityType.GEM_MINING, attributes);
    }

    private static List<OpportunityMarker> opportunityMarkers(String instanceId, String type, int startTick, int endTick, String detail)
    {
        return Arrays.asList(
            new OpportunityMarker(
                "marker-open-" + instanceId,
                instanceId,
                ActivityId.of("gem-mining-session-1"),
                type,
                OpportunityStatus.OPEN,
                time(startTick),
                Collections.singletonMap("regionId", "11410"),
                Collections.emptyList()),
            new OpportunityMarker(
                "marker-completed-" + instanceId,
                instanceId,
                ActivityId.of("gem-mining-session-1"),
                type,
                OpportunityStatus.COMPLETED,
                time(endTick),
                Collections.singletonMap("regionId", "11410"),
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
