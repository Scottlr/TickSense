package com.ticksense.activities.gemmining;

import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.analytics.ActivityAnalysisData;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ActivityReportAssembler;
import com.ticksense.analytics.AnalysisMath;
import com.ticksense.analytics.ExecutionScore;
import com.ticksense.analytics.MetricDefinition;
import com.ticksense.analytics.MetricUnit;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.MetricValueMap;
import com.ticksense.analytics.OpportunityAnalysis;
import com.ticksense.analytics.OpportunityTimelineBuilder;
import com.ticksense.analytics.ReportMetadata;
import com.ticksense.analytics.ResolvedOpportunity;
import com.ticksense.analytics.ScoreBreakdown;
import com.ticksense.analytics.TickLossBreakdown;
import com.ticksense.analytics.TickLossCategories;
import com.ticksense.analytics.TickValueFormatter;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.FinishReason;
import com.ticksense.core.FinishReasonType;
import com.ticksense.core.EventTime;
import java.util.ArrayList;
import java.util.Collections;
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

    public ActivityAnalysisData analyze(
        ActivitySession session,
        ActivityReportData activityData,
        List<OpportunityMarker> opportunityMarkers)
    {
        final ActivitySession normalizedSession = Objects.requireNonNull(session, "session");
        final ActivityReportData normalizedActivityData = Objects.requireNonNull(activityData, "activityData");
        final List<ResolvedOpportunity> opportunities = OpportunityAnalysis.resolve(opportunityMarkers);

        final List<Double> rockResponseLatencies =
            OpportunityAnalysis.completedLatencies(opportunities, GemMiningState.OPPORTUNITY_RESPAWN_TO_CLICK);
        final List<Double> movementLatencies =
            OpportunityAnalysis.completedLatencies(opportunities, GemMiningState.OPPORTUNITY_MOVEMENT_TO_ROCK);
        final int idleTicks = AnalysisMath.intAttribute(normalizedActivityData, "idleTicks");
        final int redundantClicks = AnalysisMath.intAttribute(normalizedActivityData, "redundantClicks");

        final double rockResponseValue = AnalysisMath.average(rockResponseLatencies);
        final double movementLatencyValue = AnalysisMath.average(movementLatencies);
        final double cycleConsistencyValue = averageDeviation(rockResponseLatencies);
        final ExecutionScore executionScore = buildExecutionScore(idleTicks, redundantClicks, rockResponseValue, movementLatencyValue, cycleConsistencyValue);

        final Map<String, MetricValue> metrics = MetricValueMap.builder()
            .put(ROCK_RESPONSE, rockResponseValue)
            .put(IDLE_TICKS, idleTicks)
            .put(REDUNDANT_CLICKS, redundantClicks)
            .put(MOVEMENT_LATENCY, movementLatencyValue)
            .put(CYCLE_CONSISTENCY, cycleConsistencyValue)
            .put(EXECUTION_SCORE, executionScore.getValue())
            .build();

        final Map<String, Integer> tickLossCategories = TickLossCategories.builder()
            .put("Idle ticks", idleTicks)
            .put("Redundant clicks", redundantClicks)
            .putRounded("Movement latency", AnalysisMath.sum(movementLatencies))
            .build();
        final TickLossBreakdown tickLossBreakdown = new TickLossBreakdown(
            idleTicks + redundantClicks + (int) Math.round(AnalysisMath.sum(movementLatencies)),
            tickLossCategories);

        return new ActivityAnalysisData(
            metrics,
            OpportunityTimelineBuilder.build(opportunities, GemMiningAnalyzer::labelFor),
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
        final ActivityAnalysisData reportData = analyze(normalizedSession, activityData, opportunityMarkers);

        return ActivityReportAssembler.assemble(normalizedSession, activityData, "Gem Mining", reportData);
    }

    private static List<String> buildEvidenceSummary(ActivitySession session, ActivityReportData activityData)
    {
        final List<String> evidence = new ArrayList<>(ReportMetadata.startEvidence(session));
        evidence.add("Idle ticks count only verified rock-available windows; depleted-rock wait stays in the RNG caveat, not execution loss.");
        evidence.add("Verification status: " + activityData.attributes().getText("verificationStatus"));
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

    private static double averageDeviation(List<Double> values)
    {
        if (values == null || values.size() <= 1)
        {
            return 0.0D;
        }
        final double average = AnalysisMath.average(values);
        double total = 0.0D;
        for (Double value : values)
        {
            total += Math.abs(value - average);
        }
        return total / values.size();
    }

}
