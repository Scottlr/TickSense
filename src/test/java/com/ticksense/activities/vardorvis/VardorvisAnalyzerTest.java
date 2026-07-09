package com.ticksense.activities.vardorvis;

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
import net.runelite.api.NpcID;
import org.junit.Test;

public class VardorvisAnalyzerTest
{
    private final VardorvisAnalyzer analyzer = new VardorvisAnalyzer();

    @Test
    public void reportsVerifiedMechanicLatency()
    {
        final ActivityReport report = analyzer.buildReport(
            session(),
            activityData(),
            markers(
                opportunityMarkers("ranged-1", OpportunityStatus.COMPLETED, 501, 503, "Local player movement responded to the verified Vardorvis ranged-head cue."),
                opportunityMarkers("ranged-2", OpportunityStatus.FAILED, 510, 513, "Local player took 18 damage during the verified Vardorvis ranged-head window.")));

        assertEquals("Vardorvis", report.getDetectedActivityName());
        assertEquals(FinishReasonType.LEFT_REGION, report.getFinishReason().getType());
        assertEquals(0.94D, report.getConfidence(), 0.0D);

        final Map<String, MetricValue> metrics = report.getMetrics();
        assertEquals(2.0D, metrics.get("responseLatency").getValue(), 0.0D);
        assertEquals(18.0D, metrics.get("damageDuringOpportunities").getValue(), 0.0D);
        assertEquals(5.0D, metrics.get("downtime").getValue(), 0.0D);
        assertEquals(94.0D, metrics.get("mechanicConfidence").getValue(), 0.0D);

        assertEquals(2, report.getOpportunities().size());
        assertEquals(5, report.getTickLossBreakdown().getTotalTickLoss());
        assertEquals("Best execution: Ranged head response 2 ticks", report.getSummaryLines().get(0));
        assertEquals("Damage during opportunities: 18", report.getSummaryLines().get(1));
        assertTrue(report.getEvidenceSummary().stream().anyMatch(line -> line.toLowerCase().contains("damage attribution")));
    }

    private static ActivitySession session()
    {
        final Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("displayName", "Vardorvis");
        metadata.put("confidence", "0.94");
        metadata.put("evidenceSummary", "Vardorvis verification status is VERIFIED. | Verified region 4405 contains the ranged-head cue. | Verified ranged-head projectile 9911 targeted the local player from NPC " + NpcID.VARDORVIS_HEAD + ".");
        return new ActivitySession(
            ActivityId.of("vardorvis-session-1"),
            ActivityType.VARDORVIS,
            time(501),
            time(520),
            new FinishReason(
                FinishReasonType.LEFT_REGION,
                time(520),
                0.92D,
                "Vardorvis ended because the player left the verified Vardorvis region.",
                Collections.singletonList("Region changed from verified Vardorvis context to 12345.")),
            Collections.emptyList(),
            metadata);
    }

    private static ActivityReportData activityData()
    {
        final Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("verificationStatus", "VERIFIED");
        attributes.put("verifiedMechanics", "ranged-head-response");
        attributes.put("rangedHeadResponseCount", "1");
        attributes.put("rangedHeadDamageFailures", "1");
        return new ActivityReportData(ActivityId.of("vardorvis-session-1"), ActivityType.VARDORVIS, attributes);
    }

    private static List<OpportunityMarker> opportunityMarkers(String instanceId, OpportunityStatus status, int startTick, int endTick, String detail)
    {
        final Map<String, String> context = new LinkedHashMap<>();
        context.put("mechanic", VardorvisState.MECHANIC_RANGED_HEAD_RESPONSE);
        context.put("projectileId", "9911");
        context.put("sourceNpcId", String.valueOf(NpcID.VARDORVIS_HEAD));
        context.put("regionId", "4405");
        return Arrays.asList(
            new OpportunityMarker(
                "marker-open-" + instanceId,
                instanceId,
                ActivityId.of("vardorvis-session-1"),
                VardorvisState.OPPORTUNITY_RANGED_HEAD_RESPONSE,
                OpportunityStatus.OPEN,
                time(startTick),
                context,
                Collections.emptyList()),
            new OpportunityMarker(
                "marker-" + status.name().toLowerCase() + "-" + instanceId,
                instanceId,
                ActivityId.of("vardorvis-session-1"),
                VardorvisState.OPPORTUNITY_RANGED_HEAD_RESPONSE,
                status,
                time(endTick),
                context,
                Collections.singletonList(new OpportunityEvidence(time(endTick), status == OpportunityStatus.COMPLETED ? "movement.location" : "damage", com.ticksense.activities.EvidenceStrength.CONFIRMING, detail))));
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
