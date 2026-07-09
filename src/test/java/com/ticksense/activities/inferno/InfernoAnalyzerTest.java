package com.ticksense.activities.inferno;

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
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;
import org.junit.Test;

public class InfernoAnalyzerTest
{
    private final InfernoAnalyzer analyzer = new InfernoAnalyzer();

    @Test
    public void reportsWaveAndDeathTimeline()
    {
        final ActivityReport report = analyzer.buildReport(
            session(),
            activityData(),
            markers(
                opportunityMarkers("wave-1", InfernoState.OPPORTUNITY_WAVE, 701, 710, "Inferno ended because verified death evidence placed the local player at zero health."),
                opportunityMarkers("nibbler-1", InfernoState.OPPORTUNITY_NIBBLER_WINDOW, 705, 706, "Local player engaged the verified Inferno nibbler.")));

        assertEquals("Inferno", report.getDetectedActivityName());
        assertEquals(FinishReasonType.PLAYER_DEAD, report.getFinishReason().getType());
        assertEquals(0.93D, report.getConfidence(), 0.0D);

        final Map<String, MetricValue> metrics = report.getMetrics();
        assertEquals(9.0D, metrics.get("waveDuration").getValue(), 0.0D);
        assertEquals(1.0D, metrics.get("nibblerResponse").getValue(), 0.0D);
        assertEquals(1.0D, metrics.get("supplyUsage").getValue(), 0.0D);
        assertEquals(4.0D, metrics.get("deathTimelineEvents").getValue(), 0.0D);

        assertEquals(2, report.getOpportunities().size());
        assertEquals(10, report.getTickLossBreakdown().getTotalTickLoss());
        assertEquals("Best execution: Nibbler response 1 ticks", report.getSummaryLines().get(0));
        assertEquals("Death timeline captured 4 events", report.getSummaryLines().get(1));
        assertTrue(report.getEvidenceSummary().stream().anyMatch(line -> line.contains("Prayer timing omitted")));
    }

    private static ActivitySession session()
    {
        final Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("displayName", "Inferno");
        metadata.put("confidence", "0.93");
        metadata.put("evidenceSummary", "Inferno verification status is VERIFIED. | Verified Inferno region 9043 is active. | Verified wave NPC " + NpcID.JALNIB + " opened the Inferno attempt.");
        return new ActivitySession(
            ActivityId.of("inferno-session-1"),
            ActivityType.INFERNO,
            time(701),
            time(710),
            new FinishReason(
                FinishReasonType.PLAYER_DEAD,
                time(710),
                0.95D,
                "Inferno ended because verified death evidence placed the local player at zero health.",
                Collections.singletonList("Local player took 32 damage at tick 710.")),
            Collections.emptyList(),
            metadata);
    }

    private static ActivityReportData activityData()
    {
        final Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("verificationStatus", "VERIFIED");
        attributes.put("waveSpanCount", "1");
        attributes.put("nibblerResponseCount", "1");
        attributes.put("supplyUseCount", "1");
        attributes.put("prayerWindowCount", "0");
        attributes.put("prayerEvidenceStatus", "BLOCKED");
        attributes.put("deathTimelineEventCount", "4");
        attributes.put("deathTimelineEvidence",
            "Tick 701: verified Inferno wave span opened. | Tick 705: verified nibbler " + NpcID.JALNIBREK + " opened. | Tick 706: local player engaged a verified nibbler. | Tick 707: used verified supply item " + ItemID.PRAYER_POTION4 + ".");
        return new ActivityReportData(ActivityId.of("inferno-session-1"), ActivityType.INFERNO, attributes);
    }

    private static List<OpportunityMarker> opportunityMarkers(String instanceId, String type, int startTick, int endTick, String detail)
    {
        final Map<String, String> context = new LinkedHashMap<>();
        context.put("regionId", "9043");
        if (InfernoState.OPPORTUNITY_WAVE.equals(type))
        {
            context.put("waveNpcId", String.valueOf(NpcID.JALNIB));
        }
        if (InfernoState.OPPORTUNITY_NIBBLER_WINDOW.equals(type))
        {
            context.put("nibblerNpcId", String.valueOf(NpcID.JALNIBREK));
        }
        return Arrays.asList(
            new OpportunityMarker(
                "marker-open-" + instanceId,
                instanceId,
                ActivityId.of("inferno-session-1"),
                type,
                OpportunityStatus.OPEN,
                time(startTick),
                context,
                Collections.emptyList()),
            new OpportunityMarker(
                "marker-completed-" + instanceId,
                instanceId,
                ActivityId.of("inferno-session-1"),
                type,
                OpportunityStatus.COMPLETED,
                time(endTick),
                context,
                Collections.singletonList(new OpportunityEvidence(time(endTick), RegionInstanceTelemetryEvent.TYPE, com.ticksense.activities.EvidenceStrength.CONFIRMING, detail))));
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
