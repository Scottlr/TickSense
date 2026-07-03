package com.ticksense.activities.vardorvis;

import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.analytics.ActivityAnalysisData;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ActivityReportAssembler;
import com.ticksense.analytics.AnalysisMath;
import com.ticksense.analytics.MetricDefinition;
import com.ticksense.analytics.MetricUnit;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.OpportunityMarkerResolver;
import com.ticksense.analytics.OpportunityTimelineBuilder;
import com.ticksense.analytics.ReportMetadata;
import com.ticksense.analytics.ResolvedOpportunity;
import com.ticksense.analytics.TickLossBreakdown;
import com.ticksense.analytics.TickValueFormatter;
import com.ticksense.common.TextValues;
import com.ticksense.core.ActivitySession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VardorvisAnalyzer
{
    private static final Pattern DAMAGE_PATTERN = Pattern.compile("(\\d+) damage");

    private static final MetricDefinition RESPONSE_LATENCY =
        new MetricDefinition("responseLatency", "Response latency", MetricUnit.TICKS, "Average verified Vardorvis mechanic response latency.");
    private static final MetricDefinition DAMAGE_DURING_OPPORTUNITIES =
        new MetricDefinition("damageDuringOpportunities", "Damage during opportunities", MetricUnit.COUNT, "Local-player damage attributed only inside verified Vardorvis mechanic windows.");
    private static final MetricDefinition DOWNTIME =
        new MetricDefinition("downtime", "Downtime", MetricUnit.TICKS, "Total ticks spent inside verified Vardorvis mechanic response windows.");
    private static final MetricDefinition MECHANIC_CONFIDENCE =
        new MetricDefinition("mechanicConfidence", "Mechanic confidence", MetricUnit.PERCENT, "Explicit confidence for the verified mechanic attribution in this report.", false);

    public ActivityAnalysisData analyze(
        ActivitySession session,
        ActivityReportData activityData,
        List<OpportunityMarker> opportunityMarkers)
    {
        final ActivitySession normalizedSession = Objects.requireNonNull(session, "session");
        final ActivityReportData normalizedActivityData = Objects.requireNonNull(activityData, "activityData");
        final List<ResolvedOpportunity> opportunities = OpportunityMarkerResolver.resolve(opportunityMarkers);

        final List<Double> responseLatencies = latenciesFor(opportunities, VardorvisState.OPPORTUNITY_RANGED_HEAD_RESPONSE, OpportunityStatus.COMPLETED);
        final int damageDuringOpportunities = damageDuringFailedOpportunities(opportunities, VardorvisState.OPPORTUNITY_RANGED_HEAD_RESPONSE);
        final int completedLatencyTicks = (int) Math.round(AnalysisMath.sum(responseLatencies));
        final int failedWindowTicks = latencyTicksFor(opportunities, VardorvisState.OPPORTUNITY_RANGED_HEAD_RESPONSE, OpportunityStatus.FAILED);
        final double downtimeValue = completedLatencyTicks + failedWindowTicks;
        final double mechanicConfidenceValue = ReportMetadata.confidence(normalizedSession) * 100.0D;

        final Map<String, MetricValue> metrics = new LinkedHashMap<>();
        metrics.put(RESPONSE_LATENCY.getKey(), new MetricValue(RESPONSE_LATENCY, AnalysisMath.average(responseLatencies)));
        metrics.put(DAMAGE_DURING_OPPORTUNITIES.getKey(), new MetricValue(DAMAGE_DURING_OPPORTUNITIES, damageDuringOpportunities));
        metrics.put(DOWNTIME.getKey(), new MetricValue(DOWNTIME, downtimeValue));
        metrics.put(MECHANIC_CONFIDENCE.getKey(), new MetricValue(MECHANIC_CONFIDENCE, mechanicConfidenceValue));

        final Map<String, Integer> tickLossCategories = new LinkedHashMap<>();
        tickLossCategories.put("Response latency", completedLatencyTicks);
        tickLossCategories.put("Failed response windows", failedWindowTicks);
        final TickLossBreakdown tickLossBreakdown = new TickLossBreakdown(completedLatencyTicks + failedWindowTicks, tickLossCategories);

        return new ActivityAnalysisData(
            metrics,
            OpportunityTimelineBuilder.build(opportunities, VardorvisAnalyzer::labelFor),
            tickLossBreakdown,
            buildEvidenceSummary(normalizedSession, normalizedActivityData),
            buildSummaryLines(responseLatencies, damageDuringOpportunities));
    }

    public ActivityReport buildReport(
        ActivitySession session,
        ActivityReportData activityData,
        List<OpportunityMarker> opportunityMarkers)
    {
        final ActivitySession normalizedSession = Objects.requireNonNull(session, "session");
        final ActivityAnalysisData reportData = analyze(normalizedSession, activityData, opportunityMarkers);

        return ActivityReportAssembler.assemble(normalizedSession, activityData, "Vardorvis", reportData);
    }

    private static List<Double> latenciesFor(List<ResolvedOpportunity> opportunities, String opportunityType, OpportunityStatus status)
    {
        final List<Double> latencies = new ArrayList<>();
        for (ResolvedOpportunity opportunity : opportunities)
        {
            if (opportunityType.equals(opportunity.type()) && opportunity.status() == status)
            {
                latencies.add((double) opportunity.latencyTicks());
            }
        }
        return latencies;
    }

    private static int latencyTicksFor(List<ResolvedOpportunity> opportunities, String opportunityType, OpportunityStatus status)
    {
        int total = 0;
        for (ResolvedOpportunity opportunity : opportunities)
        {
            if (opportunityType.equals(opportunity.type()) && opportunity.status() == status)
            {
                total += opportunity.latencyTicks();
            }
        }
        return total;
    }

    private static int damageDuringFailedOpportunities(List<ResolvedOpportunity> opportunities, String opportunityType)
    {
        int totalDamage = 0;
        for (ResolvedOpportunity opportunity : opportunities)
        {
            if (!opportunityType.equals(opportunity.type()) || opportunity.status() != OpportunityStatus.FAILED)
            {
                continue;
            }
            for (String detail : OpportunityTimelineBuilder.evidenceDetails(opportunity.terminalEvidence()))
            {
                final Matcher matcher = DAMAGE_PATTERN.matcher(detail);
                if (matcher.find())
                {
                    totalDamage += Integer.parseInt(matcher.group(1));
                }
            }
        }
        return totalDamage;
    }

    private static List<String> buildEvidenceSummary(ActivitySession session, ActivityReportData activityData)
    {
        final List<String> evidence = new ArrayList<>(ReportMetadata.startEvidence(session));
        evidence.add("Vardorvis damage attribution counts only local-player damage inside verified mechanic windows.");
        evidence.add("Verification status: " + TextValues.trimmedOrEmpty(activityData.getAttributes().get("verificationStatus")));
        evidence.add("Verified mechanics: " + TextValues.trimmedOrEmpty(activityData.getAttributes().get("verifiedMechanics")));
        return Collections.unmodifiableList(evidence);
    }

    private static List<String> buildSummaryLines(List<Double> responseLatencies, int damageDuringOpportunities)
    {
        if (responseLatencies.isEmpty())
        {
            return java.util.Arrays.asList(
                "Best execution: No verified Vardorvis mechanic response recorded.",
                "Damage during opportunities: " + damageDuringOpportunities);
        }

        return java.util.Arrays.asList(
            "Best execution: Ranged head response " + TickValueFormatter.formatTicks(AnalysisMath.minimum(responseLatencies)) + " ticks",
            "Damage during opportunities: " + damageDuringOpportunities);
    }

    private static String labelFor(String opportunityType)
    {
        if (VardorvisState.OPPORTUNITY_RANGED_HEAD_RESPONSE.equals(opportunityType))
        {
            return "Ranged head response";
        }
        if (VardorvisState.OPPORTUNITY_BLOOD_SPLAT_MOVEMENT.equals(opportunityType))
        {
            return "Blood splat movement";
        }
        if (VardorvisState.OPPORTUNITY_AXE_DODGE.equals(opportunityType))
        {
            return "Axe dodge";
        }
        if (VardorvisState.OPPORTUNITY_PRAYER_RESPONSE.equals(opportunityType))
        {
            return "Prayer response";
        }
        return opportunityType.toLowerCase(Locale.US);
    }

}
