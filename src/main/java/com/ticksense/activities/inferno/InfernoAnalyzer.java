package com.ticksense.activities.inferno;

import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.analytics.ActivityAnalysisData;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ActivityReportAssembler;
import com.ticksense.analytics.AnalysisMath;
import com.ticksense.analytics.MetricDefinition;
import com.ticksense.analytics.MetricUnit;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.MetricValueMap;
import com.ticksense.analytics.OpportunityMarkerResolver;
import com.ticksense.analytics.OpportunityTimelineBuilder;
import com.ticksense.analytics.ReportMetadata;
import com.ticksense.analytics.ResolvedOpportunity;
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

public final class InfernoAnalyzer
{
    private static final MetricDefinition WAVE_DURATION =
        new MetricDefinition("waveDuration", "Wave duration", MetricUnit.TICKS, "Average verified Inferno wave span duration.");
    private static final MetricDefinition NIBBLER_RESPONSE =
        new MetricDefinition("nibblerResponse", "Nibbler response", MetricUnit.TICKS, "Average verified Inferno nibbler response latency.");
    private static final MetricDefinition SUPPLY_USAGE =
        new MetricDefinition("supplyUsage", "Supply usage", MetricUnit.COUNT, "Verified Inferno supplies consumed during the recorded attempt.");
    private static final MetricDefinition DEATH_TIMELINE_EVENTS =
        new MetricDefinition("deathTimelineEvents", "Death timeline events", MetricUnit.COUNT, "Bounded retrospective events captured for the Inferno death timeline.");

    public ActivityAnalysisData analyze(
        ActivitySession session,
        ActivityReportData activityData,
        List<OpportunityMarker> opportunityMarkers)
    {
        final ActivitySession normalizedSession = Objects.requireNonNull(session, "session");
        final ActivityReportData normalizedActivityData = Objects.requireNonNull(activityData, "activityData");
        final List<ResolvedOpportunity> opportunities = OpportunityMarkerResolver.resolve(opportunityMarkers);

        final List<Double> waveDurations = latenciesFor(opportunities, InfernoState.OPPORTUNITY_WAVE);
        final List<Double> nibblerResponses = latenciesFor(opportunities, InfernoState.OPPORTUNITY_NIBBLER_WINDOW);
        final int supplyUsage = AnalysisMath.intAttribute(normalizedActivityData, "supplyUseCount");
        final int deathTimelineEvents = AnalysisMath.intAttribute(normalizedActivityData, "deathTimelineEventCount");

        final Map<String, MetricValue> metrics = MetricValueMap.builder()
            .put(WAVE_DURATION, AnalysisMath.average(waveDurations))
            .put(NIBBLER_RESPONSE, AnalysisMath.average(nibblerResponses))
            .put(SUPPLY_USAGE, supplyUsage)
            .put(DEATH_TIMELINE_EVENTS, deathTimelineEvents)
            .build();

        final Map<String, Integer> tickLossCategories = TickLossCategories.builder()
            .putRounded("Wave duration", AnalysisMath.sum(waveDurations))
            .putRounded("Nibbler response", AnalysisMath.sum(nibblerResponses))
            .build();
        final TickLossBreakdown tickLossBreakdown = new TickLossBreakdown(
            tickLossCategories.get("Wave duration") + tickLossCategories.get("Nibbler response"),
            tickLossCategories);

        return new ActivityAnalysisData(
            metrics,
            OpportunityTimelineBuilder.build(opportunities, InfernoAnalyzer::labelFor),
            tickLossBreakdown,
            buildEvidenceSummary(normalizedSession, normalizedActivityData),
            buildSummaryLines(nibblerResponses, deathTimelineEvents));
    }

    public ActivityReport buildReport(
        ActivitySession session,
        ActivityReportData activityData,
        List<OpportunityMarker> opportunityMarkers)
    {
        final ActivitySession normalizedSession = Objects.requireNonNull(session, "session");
        final ActivityAnalysisData reportData = analyze(normalizedSession, activityData, opportunityMarkers);

        return ActivityReportAssembler.assemble(normalizedSession, activityData, "Inferno", reportData);
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

    private static List<String> buildEvidenceSummary(ActivitySession session, ActivityReportData activityData)
    {
        final List<String> evidence = new ArrayList<>(ReportMetadata.startEvidence(session));
        final String prayerStatus = TextValues.trimmedOrEmpty(activityData.getAttributes().get("prayerEvidenceStatus"));
        evidence.add("Prayer timing omitted because prayer evidence is " + prayerStatus + ".");
        final String deathTimeline = TextValues.trimmedOrEmpty(activityData.getAttributes().get("deathTimelineEvidence"));
        if (!deathTimeline.isEmpty())
        {
            evidence.add("Death timeline: " + deathTimeline);
        }
        evidence.add("Verification status: " + TextValues.trimmedOrEmpty(activityData.getAttributes().get("verificationStatus")));
        return Collections.unmodifiableList(evidence);
    }

    private static List<String> buildSummaryLines(List<Double> nibblerResponses, int deathTimelineEvents)
    {
        if (nibblerResponses.isEmpty())
        {
            return java.util.Arrays.asList(
                "Best execution: No verified Inferno nibbler response recorded.",
                "Death timeline captured " + deathTimelineEvents + " events");
        }

        return java.util.Arrays.asList(
            "Best execution: Nibbler response " + TickValueFormatter.formatTicks(AnalysisMath.minimum(nibblerResponses)) + " ticks",
            "Death timeline captured " + deathTimelineEvents + " events");
    }

    private static String labelFor(String opportunityType)
    {
        if (InfernoState.OPPORTUNITY_WAVE.equals(opportunityType))
        {
            return "Wave span";
        }
        if (InfernoState.OPPORTUNITY_NIBBLER_WINDOW.equals(opportunityType))
        {
            return "Nibbler response";
        }
        if (InfernoState.OPPORTUNITY_PRAYER_WINDOW.equals(opportunityType))
        {
            return "Prayer window";
        }
        return opportunityType.toLowerCase(Locale.US);
    }

}
