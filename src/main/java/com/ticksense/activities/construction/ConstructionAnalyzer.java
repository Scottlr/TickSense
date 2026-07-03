package com.ticksense.activities.construction;

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
import com.ticksense.core.ActivitySession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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

    public ConstructionReportData analyze(
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

        final double menuLatencyValue = average(menuLatencies);
        final double cadenceValue = average(cadenceLatencies);
        final double bankingValue = average(bankingLatencies);
        final double inventoryCycleValue = average(inventoryCycleLatencies);
        final ExecutionScore executionScore = buildExecutionScore(menuLatencyValue, cadenceValue, bankingValue, inventoryCycleValue);

        final Map<String, MetricValue> metrics = new LinkedHashMap<>();
        metrics.put(MENU_LATENCY.getKey(), new MetricValue(MENU_LATENCY, menuLatencyValue));
        metrics.put(BUILD_REMOVE_CADENCE.getKey(), new MetricValue(BUILD_REMOVE_CADENCE, cadenceValue));
        metrics.put(BANKING_DOWNTIME.getKey(), new MetricValue(BANKING_DOWNTIME, bankingValue));
        metrics.put(INVENTORY_CYCLE.getKey(), new MetricValue(INVENTORY_CYCLE, inventoryCycleValue));
        metrics.put(EXECUTION_SCORE.getKey(), new MetricValue(EXECUTION_SCORE, executionScore.getValue()));

        final Map<String, Integer> tickLossCategories = new LinkedHashMap<>();
        tickLossCategories.put("Menu latency", (int) Math.round(sum(menuLatencies)));
        tickLossCategories.put("Build/remove cadence", (int) Math.round(sum(cadenceLatencies)));
        tickLossCategories.put("Banking downtime", (int) Math.round(sum(bankingLatencies)));
        final TickLossBreakdown tickLossBreakdown = new TickLossBreakdown(
            tickLossCategories.get("Menu latency") + tickLossCategories.get("Build/remove cadence") + tickLossCategories.get("Banking downtime"),
            tickLossCategories);

        return new ConstructionReportData(
            metrics,
            buildTimeline(opportunities),
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
        final ConstructionReportData reportData = analyze(normalizedSession, activityData, opportunityMarkers);

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
        final String startEvidence = safeText(session.getMetadata().get("evidenceSummary"));
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
        evidence.add("Observe-only Construction analytics: menu, widget, animation, inventory, XP, and bank evidence are retrospective only.");
        evidence.add("Verification status: " + safeText(activityData.getAttributes().get("verificationStatus")));
        evidence.add("Verified method: " + safeText(activityData.getAttributes().get("methodName")));
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

        final double bestMenuLatency = menuLatencies.isEmpty() ? 0.0D : minimum(menuLatencies);
        final double longestCycle = inventoryCycleLatencies.isEmpty() ? 0.0D : maximum(inventoryCycleLatencies);
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

    private static String displayName(ActivitySession session)
    {
        final String displayName = safeText(session.getMetadata().get("displayName"));
        return displayName.isEmpty() ? "Construction" : displayName;
    }

    private static double confidence(ActivitySession session)
    {
        final String raw = safeText(session.getMetadata().get("confidence"));
        if (raw.isEmpty())
        {
            return 0.0D;
        }
        return Double.parseDouble(raw);
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

    private static double minimum(List<Double> values)
    {
        double minimum = values.get(0);
        for (Double value : values)
        {
            minimum = Math.min(minimum, value);
        }
        return minimum;
    }

    private static double maximum(List<Double> values)
    {
        double maximum = values.get(0);
        for (Double value : values)
        {
            maximum = Math.max(maximum, value);
        }
        return maximum;
    }

    private static String safeText(String value)
    {
        return value == null ? "" : value.trim();
    }

}
