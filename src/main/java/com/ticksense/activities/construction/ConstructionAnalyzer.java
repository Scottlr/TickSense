package com.ticksense.activities.construction;

import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ActivityAnalysisData;
import com.ticksense.analytics.ActivityReportAssembler;
import com.ticksense.analytics.AnalysisMath;
import com.ticksense.analytics.ExecutionScore;
import com.ticksense.analytics.MetricDefinition;
import com.ticksense.analytics.MetricUnit;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.MetricValueMap;
import com.ticksense.analytics.OpportunityMarkerResolver;
import com.ticksense.analytics.OpportunityTimelineBuilder;
import com.ticksense.analytics.ReportMetadata;
import com.ticksense.analytics.ResolvedOpportunity;
import com.ticksense.analytics.ScoreBreakdown;
import com.ticksense.analytics.TickLossBreakdown;
import com.ticksense.analytics.TickLossCategories;
import com.ticksense.analytics.TickValueFormatter;
import com.ticksense.common.TextValues;
import com.ticksense.core.ActivitySession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class ConstructionAnalyzer
{
    private static final MetricDefinition MENU_LATENCY =
        new MetricDefinition("menuLatency", "Menu latency", MetricUnit.TICKS, "Average verified menu-open to option-click latency.");
    private static final MetricDefinition BUILD_REMOVE_CADENCE =
        new MetricDefinition("buildRemoveCadence", "Build/remove cadence", MetricUnit.TICKS, "Average verified build confirmation to remove-click cadence.");
    private static final MetricDefinition BANKING_DOWNTIME =
        new MetricDefinition("bankingDowntime", "Banking downtime", MetricUnit.TICKS, "Average verified downtime between plank exhaustion or remove flow and bank-open evidence.");
    private static final MetricDefinition INVENTORY_CYCLE =
        new MetricDefinition("inventoryCycle", "Inventory cycle", MetricUnit.TICKS, "Average verified build-click to bank-open inventory cycle duration.");
    private static final MetricDefinition EXECUTION_SCORE =
        new MetricDefinition("executionScore", "Execution score", MetricUnit.SCORE, "Construction execution score.", false);

    public ActivityAnalysisData analyze(
        ActivitySession session,
        ActivityReportData activityData,
        List<OpportunityMarker> opportunityMarkers)
    {
        final ActivitySession normalizedSession = Objects.requireNonNull(session, "session");
        final ActivityReportData normalizedActivityData = Objects.requireNonNull(activityData, "activityData");
        final List<ResolvedOpportunity> opportunities = reportableOpportunities(OpportunityMarkerResolver.resolve(opportunityMarkers));

        final List<Double> menuLatencies = latenciesFor(opportunities, ConstructionState.OPPORTUNITY_MENU_LATENCY);
        final List<Double> cadenceLatencies = latenciesFor(opportunities, ConstructionState.OPPORTUNITY_BUILD_REMOVE_CADENCE);
        final List<Double> bankingLatencies = latenciesFor(opportunities, ConstructionState.OPPORTUNITY_BANKING_DOWNTIME);
        final List<Double> inventoryCycleLatencies = latenciesFor(opportunities, ConstructionState.OPPORTUNITY_INVENTORY_CYCLE);

        final double menuLatencyValue = AnalysisMath.average(menuLatencies);
        final double cadenceValue = AnalysisMath.average(cadenceLatencies);
        final double bankingValue = AnalysisMath.average(bankingLatencies);
        final double inventoryCycleValue = AnalysisMath.average(inventoryCycleLatencies);
        final ExecutionScore executionScore = buildExecutionScore(menuLatencyValue, cadenceValue, bankingValue, inventoryCycleValue);

        final Map<String, MetricValue> metrics = MetricValueMap.builder()
            .put(MENU_LATENCY, menuLatencyValue)
            .put(BUILD_REMOVE_CADENCE, cadenceValue)
            .put(BANKING_DOWNTIME, bankingValue)
            .put(INVENTORY_CYCLE, inventoryCycleValue)
            .put(EXECUTION_SCORE, executionScore.getValue())
            .build();

        final Map<String, Integer> tickLossCategories = TickLossCategories.builder()
            .putRounded("Menu latency", AnalysisMath.sum(menuLatencies))
            .putRounded("Build/remove cadence", AnalysisMath.sum(cadenceLatencies))
            .putRounded("Banking downtime", AnalysisMath.sum(bankingLatencies))
            .build();
        final TickLossBreakdown tickLossBreakdown = new TickLossBreakdown(
            tickLossCategories.get("Menu latency") + tickLossCategories.get("Build/remove cadence") + tickLossCategories.get("Banking downtime"),
            tickLossCategories);

        return new ActivityAnalysisData(
            metrics,
            OpportunityTimelineBuilder.build(opportunities, ConstructionAnalyzer::labelFor),
            tickLossBreakdown,
            buildEvidenceSummary(normalizedSession, normalizedActivityData),
            buildSummaryLines(menuLatencies, inventoryCycleLatencies));
    }

    public ActivityReport buildReport(
        ActivitySession session,
        ActivityReportData activityData,
        List<OpportunityMarker> opportunityMarkers)
    {
        final ActivitySession normalizedSession = Objects.requireNonNull(session, "session");
        final ActivityAnalysisData reportData = analyze(normalizedSession, activityData, opportunityMarkers);

        return ActivityReportAssembler.assemble(normalizedSession, activityData, "Construction", reportData);
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

    private static List<ResolvedOpportunity> reportableOpportunities(List<ResolvedOpportunity> opportunities)
    {
        if (opportunities.isEmpty())
        {
            return opportunities;
        }

        final List<ResolvedOpportunity> filtered = new ArrayList<>(opportunities.size());
        for (ResolvedOpportunity opportunity : opportunities)
        {
            if (isReportable(opportunity))
            {
                filtered.add(opportunity);
            }
        }
        return Collections.unmodifiableList(filtered);
    }

    private static boolean isReportable(ResolvedOpportunity opportunity)
    {
        if (!ConstructionState.OPPORTUNITY_MENU_LATENCY.equals(opportunity.type()))
        {
            return true;
        }
        return "build".equalsIgnoreCase(opportunity.contextValue("option"))
            || "larder space".equalsIgnoreCase(opportunity.contextValue("target"));
    }

    private static List<String> buildEvidenceSummary(ActivitySession session, ActivityReportData activityData)
    {
        final List<String> evidence = new ArrayList<>(ReportMetadata.startEvidence(session));
        evidence.add("Observe-only Construction analytics: menu, widget, animation, inventory, XP, and bank evidence are retrospective only.");
        evidence.add("Verification status: " + TextValues.trimmedOrEmpty(activityData.getAttributes().get("verificationStatus")));
        evidence.add("Verified method: " + TextValues.trimmedOrEmpty(activityData.getAttributes().get("methodName")));
        return Collections.unmodifiableList(evidence);
    }

    private static List<String> buildSummaryLines(List<Double> menuLatencies, List<Double> inventoryCycleLatencies)
    {
        if (menuLatencies.isEmpty() && inventoryCycleLatencies.isEmpty())
        {
            return java.util.Arrays.asList(
                "Best execution: No verified Construction cycle recorded.",
                "Longest cycle: No verified Construction cycle recorded.");
        }

        final double bestMenuLatency = menuLatencies.isEmpty() ? 0.0D : AnalysisMath.minimum(menuLatencies);
        final double longestCycle = inventoryCycleLatencies.isEmpty() ? 0.0D : AnalysisMath.maximum(inventoryCycleLatencies);
        return java.util.Arrays.asList(
            "Best execution: Menu latency " + TickValueFormatter.formatTicks(bestMenuLatency) + " ticks",
            "Longest cycle: Inventory cycle " + TickValueFormatter.formatTicks(longestCycle) + " ticks");
    }

    private static ExecutionScore buildExecutionScore(
        double menuLatencyValue,
        double cadenceValue,
        double bankingValue,
        double inventoryCycleValue)
    {
        final ScoreBreakdown breakdown = new ScoreBreakdown(
            100.0D,
            java.util.Arrays.asList(
                ScoreBreakdown.penalty("menu", "Menu latency", menuLatencyValue * 12.0D, "Average verified menu-open to option-click latency."),
                ScoreBreakdown.penalty("cadence", "Build/remove cadence", cadenceValue * 1.5D, "Average time between build confirmation and remove click."),
                ScoreBreakdown.penalty("banking", "Banking downtime", bankingValue * 0.75D, "Average verified downtime before bank-open evidence."),
                ScoreBreakdown.penalty("cycle", "Inventory cycle", inventoryCycleValue * 0.25D, "Average build-click to bank-open inventory cycle duration.")));
        return breakdown.getExecutionScore();
    }

    private static String labelFor(String opportunityType)
    {
        if (ConstructionState.OPPORTUNITY_MENU_LATENCY.equals(opportunityType))
        {
            return "Menu latency";
        }
        if (ConstructionState.OPPORTUNITY_BUILD_REMOVE_CADENCE.equals(opportunityType))
        {
            return "Build/remove cadence";
        }
        if (ConstructionState.OPPORTUNITY_BANKING_DOWNTIME.equals(opportunityType))
        {
            return "Banking downtime";
        }
        if (ConstructionState.OPPORTUNITY_INVENTORY_CYCLE.equals(opportunityType))
        {
            return "Inventory cycle";
        }
        return opportunityType.toLowerCase(Locale.US);
    }

}
