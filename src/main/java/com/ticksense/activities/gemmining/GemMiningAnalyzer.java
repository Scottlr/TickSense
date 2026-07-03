package com.ticksense.activities.gemmining;

import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.OpportunityEvidence;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ExecutionScore;
import com.ticksense.analytics.MetricDefinition;
import com.ticksense.analytics.MetricUnit;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.OpportunityMarkerResolver;
import com.ticksense.analytics.OpportunityTimelineEntry;
import com.ticksense.analytics.ResolvedOpportunity;
import com.ticksense.analytics.ScoreBreakdown;
import com.ticksense.analytics.TickLossBreakdown;
import com.ticksense.analytics.TickValueFormatter;
import com.ticksense.common.TextValues;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.FinishReason;
import com.ticksense.core.FinishReasonType;
import com.ticksense.core.EventTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class GemMiningAnalyzer
{
    private static final MetricDefinition ROCK_RESPONSE =
        new MetricDefinition("rockResponse", "Rock response", MetricUnit.TICKS, "Average verified rock respawn-to-click latency.");
    private static final MetricDefinition IDLE_TICKS =
        new MetricDefinition("idleTicks", "Idle ticks", MetricUnit.TICKS, "Idle ticks recorded only while a verified gem rock was available.");
    private static final MetricDefinition REDUNDANT_CLICKS =
        new MetricDefinition("redundantClicks", "Redundant clicks", MetricUnit.COUNT, "Repeated mine clicks without progress.");
    private static final MetricDefinition MOVEMENT_LATENCY =
        new MetricDefinition("movementLatency", "Movement latency", MetricUnit.TICKS, "Average verified movement-to-rock latency.");
    private static final MetricDefinition CYCLE_CONSISTENCY =
        new MetricDefinition("cycleConsistency", "Cycle consistency", MetricUnit.TICKS, "Average deviation across verified rock-response cycles.");
    private static final MetricDefinition EXECUTION_SCORE =
        new MetricDefinition("executionScore", "Execution score", MetricUnit.SCORE, "Gem mining execution score.", false);

    public GemMiningReportData analyze(
        ActivitySession session,
        ActivityReportData activityData,
        List<OpportunityMarker> opportunityMarkers)
    {
        final ActivitySession normalizedSession = Objects.requireNonNull(session, "session");
        final ActivityReportData normalizedActivityData = Objects.requireNonNull(activityData, "activityData");
        final List<ResolvedOpportunity> opportunities = OpportunityMarkerResolver.resolve(opportunityMarkers);

        final List<Double> rockResponseLatencies = latenciesFor(opportunities, GemMiningState.OPPORTUNITY_RESPAWN_TO_CLICK);
        final List<Double> movementLatencies = latenciesFor(opportunities, GemMiningState.OPPORTUNITY_MOVEMENT_TO_ROCK);
        final int idleTicks = intAttribute(normalizedActivityData, "idleTicks");
        final int redundantClicks = intAttribute(normalizedActivityData, "redundantClicks");

        final double rockResponseValue = average(rockResponseLatencies);
        final double movementLatencyValue = average(movementLatencies);
        final double cycleConsistencyValue = averageDeviation(rockResponseLatencies);
        final ExecutionScore executionScore = buildExecutionScore(idleTicks, redundantClicks, rockResponseValue, movementLatencyValue, cycleConsistencyValue);

        final Map<String, MetricValue> metrics = new LinkedHashMap<>();
        metrics.put(ROCK_RESPONSE.getKey(), new MetricValue(ROCK_RESPONSE, rockResponseValue));
        metrics.put(IDLE_TICKS.getKey(), new MetricValue(IDLE_TICKS, idleTicks));
        metrics.put(REDUNDANT_CLICKS.getKey(), new MetricValue(REDUNDANT_CLICKS, redundantClicks));
        metrics.put(MOVEMENT_LATENCY.getKey(), new MetricValue(MOVEMENT_LATENCY, movementLatencyValue));
        metrics.put(CYCLE_CONSISTENCY.getKey(), new MetricValue(CYCLE_CONSISTENCY, cycleConsistencyValue));
        metrics.put(EXECUTION_SCORE.getKey(), new MetricValue(EXECUTION_SCORE, executionScore.getValue()));

        final Map<String, Integer> tickLossCategories = new LinkedHashMap<>();
        tickLossCategories.put("Idle ticks", idleTicks);
        tickLossCategories.put("Redundant clicks", redundantClicks);
        tickLossCategories.put("Movement latency", (int) Math.round(sum(movementLatencies)));
        final TickLossBreakdown tickLossBreakdown = new TickLossBreakdown(
            idleTicks + redundantClicks + (int) Math.round(sum(movementLatencies)),
            tickLossCategories);

        return new GemMiningReportData(
            metrics,
            buildTimeline(opportunities),
            tickLossBreakdown,
            buildEvidenceSummary(normalizedSession, normalizedActivityData),
            buildSummaryLines(rockResponseLatencies));
    }

    public ActivityReport buildReport(
        ActivitySession session,
        ActivityReportData activityData,
        List<OpportunityMarker> opportunityMarkers)
    {
        final ActivitySession normalizedSession = Objects.requireNonNull(session, "session");
        final GemMiningReportData reportData = analyze(normalizedSession, activityData, opportunityMarkers);

        return new ActivityReport(
            ActivityReport.SCHEMA_VERSION,
            normalizedSession.getActivityId() + "-report",
            normalizedSession.getActivityId(),
            normalizedSession.getActivityType(),
            displayName(normalizedSession),
            normalizedSession.getEndTime().getWallTimeMillis(),
            normalizedSession.getEndTime().getGameTick() - normalizedSession.getStartTime().getGameTick(),
            normalizedSession.getEndTime().getWallTimeMillis() - normalizedSession.getStartTime().getWallTimeMillis(),
            normalizedSession.getFinishReason(),
            confidence(normalizedSession),
            reportData.getEvidenceSummary(),
            reportData.getMetrics(),
            reportData.getOpportunityTimeline(),
            reportData.getTickLossBreakdown(),
            reportData.getSummaryLines());
    }

    private static List<Double> latenciesFor(List<ResolvedOpportunity> opportunities, String opportunityType)
    {
        final List<Double> latencies = new ArrayList<>();
        for (ResolvedOpportunity opportunity : opportunities)
        {
            if (opportunityType.equals(opportunity.type()) && opportunity.completed())
            {
                latencies.add((double) opportunity.latencyTicks());
            }
        }
        return latencies;
    }

    private static List<OpportunityTimelineEntry> buildTimeline(List<ResolvedOpportunity> opportunities)
    {
        if (opportunities.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<OpportunityTimelineEntry> timeline = new ArrayList<>(opportunities.size());
        for (ResolvedOpportunity opportunity : opportunities)
        {
            timeline.add(new OpportunityTimelineEntry(
                opportunity.type(),
                labelFor(opportunity.type()),
                opportunity.status().name(),
                opportunity.endTick(),
                opportunity.endWallTimeMillis(),
                opportunity.latencyTicks(),
                opportunity.latencyMillis(),
                evidenceDetails(opportunity.terminalEvidence())));
        }
        return Collections.unmodifiableList(timeline);
    }

    private static List<String> buildEvidenceSummary(ActivitySession session, ActivityReportData activityData)
    {
        final List<String> evidence = new ArrayList<>();
        final String startEvidence = TextValues.trimmedOrEmpty(session.getMetadata().get("evidenceSummary"));
        if (!startEvidence.isEmpty())
        {
            for (String part : startEvidence.split("\\|"))
            {
                final String trimmed = part.trim();
                if (!trimmed.isEmpty())
                {
                    evidence.add(trimmed);
                }
            }
        }
        evidence.add("Idle ticks count only verified rock-available windows; depleted-rock wait stays in the RNG caveat, not execution loss.");
        evidence.add("Verification status: " + TextValues.trimmedOrEmpty(activityData.getAttributes().get("verificationStatus")));
        return Collections.unmodifiableList(evidence);
    }

    private static List<String> buildSummaryLines(List<Double> rockResponseLatencies)
    {
        if (rockResponseLatencies.isEmpty())
        {
            return java.util.Arrays.asList(
                "Best execution: No verified gem mining cycle recorded.",
                "Worst execution: No verified gem mining cycle recorded.");
        }

        double best = rockResponseLatencies.get(0);
        double worst = rockResponseLatencies.get(0);
        for (Double latency : rockResponseLatencies)
        {
            best = Math.min(best, latency);
            worst = Math.max(worst, latency);
        }
        return java.util.Arrays.asList(
            "Best execution: Rock response " + TickValueFormatter.formatTicks(best) + " ticks",
            "Worst execution: Rock response " + TickValueFormatter.formatTicks(worst) + " ticks");
    }

    private static ExecutionScore buildExecutionScore(
        int idleTicks,
        int redundantClicks,
        double rockResponseValue,
        double movementLatencyValue,
        double cycleConsistencyValue)
    {
        final ScoreBreakdown breakdown = new ScoreBreakdown(
            100.0D,
            java.util.Arrays.asList(
                ScoreBreakdown.penalty("idle", "Idle ticks", idleTicks * 4.0D, "Idle ticks while a verified rock was available."),
                ScoreBreakdown.penalty("redundant", "Redundant clicks", redundantClicks * 2.0D, "Repeated mine clicks without progress."),
                ScoreBreakdown.penalty("response", "Rock response", rockResponseValue * 8.0D, "Average respawn-to-click latency."),
                ScoreBreakdown.penalty("movement", "Movement latency", movementLatencyValue * 3.0D, "Average movement-to-rock latency."),
                ScoreBreakdown.penalty("consistency", "Cycle consistency", cycleConsistencyValue * 3.0D, "Deviation across verified mining cycles.")));
        return breakdown.getExecutionScore();
    }

    private static String displayName(ActivitySession session)
    {
        final String displayName = TextValues.trimmedOrEmpty(session.getMetadata().get("displayName"));
        return displayName.isEmpty() ? "Gem Mining" : displayName;
    }

    private static double confidence(ActivitySession session)
    {
        final String raw = TextValues.trimmedOrEmpty(session.getMetadata().get("confidence"));
        if (raw.isEmpty())
        {
            return 0.0D;
        }
        return Double.parseDouble(raw);
    }

    private static int intAttribute(ActivityReportData activityData, String key)
    {
        final String raw = TextValues.trimmedOrEmpty(activityData.getAttributes().get(key));
        if (raw.isEmpty())
        {
            return 0;
        }
        return Integer.parseInt(raw);
    }

    private static String labelFor(String opportunityType)
    {
        if (GemMiningState.OPPORTUNITY_RESPAWN_TO_CLICK.equals(opportunityType))
        {
            return "Rock response";
        }
        if (GemMiningState.OPPORTUNITY_IDLE.equals(opportunityType))
        {
            return "Idle ticks";
        }
        if (GemMiningState.OPPORTUNITY_MOVEMENT_TO_ROCK.equals(opportunityType))
        {
            return "Movement latency";
        }
        return opportunityType.toLowerCase(Locale.US);
    }

    private static List<String> evidenceDetails(List<OpportunityEvidence> evidence)
    {
        if (evidence == null || evidence.isEmpty())
        {
            return Collections.emptyList();
        }
        final List<String> details = new ArrayList<>(evidence.size());
        for (OpportunityEvidence item : evidence)
        {
            details.add(item.getDetail().isEmpty() ? item.getSourceEventType() : item.getDetail());
        }
        return Collections.unmodifiableList(details);
    }

    private static double average(List<Double> values)
    {
        if (values == null || values.isEmpty())
        {
            return 0.0D;
        }
        return sum(values) / values.size();
    }

    private static double sum(List<Double> values)
    {
        double sum = 0.0D;
        if (values != null)
        {
            for (Double value : values)
            {
                sum += value;
            }
        }
        return sum;
    }

    private static double averageDeviation(List<Double> values)
    {
        if (values == null || values.size() <= 1)
        {
            return 0.0D;
        }
        final double average = average(values);
        double total = 0.0D;
        for (Double value : values)
        {
            total += Math.abs(value - average);
        }
        return total / values.size();
    }

}
