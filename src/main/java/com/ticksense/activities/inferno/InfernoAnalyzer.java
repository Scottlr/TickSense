package com.ticksense.activities.inferno;

import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.OpportunityEvidence;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.MetricDefinition;
import com.ticksense.analytics.MetricUnit;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.OpportunityMarkerResolver;
import com.ticksense.analytics.OpportunityTimelineEntry;
import com.ticksense.analytics.ResolvedOpportunity;
import com.ticksense.analytics.TickLossBreakdown;
import com.ticksense.core.ActivitySession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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

    public InfernoReportData analyze(
        ActivitySession session,
        ActivityReportData activityData,
        List<OpportunityMarker> opportunityMarkers)
    {
        final ActivitySession normalizedSession = Objects.requireNonNull(session, "session");
        final ActivityReportData normalizedActivityData = Objects.requireNonNull(activityData, "activityData");
        final List<ResolvedOpportunity> opportunities = OpportunityMarkerResolver.resolve(opportunityMarkers);

        final List<Double> waveDurations = latenciesFor(opportunities, InfernoState.OPPORTUNITY_WAVE);
        final List<Double> nibblerResponses = latenciesFor(opportunities, InfernoState.OPPORTUNITY_NIBBLER_WINDOW);
        final int supplyUsage = intAttribute(normalizedActivityData, "supplyUseCount");
        final int deathTimelineEvents = intAttribute(normalizedActivityData, "deathTimelineEventCount");

        final Map<String, MetricValue> metrics = new LinkedHashMap<>();
        metrics.put(WAVE_DURATION.getKey(), new MetricValue(WAVE_DURATION, average(waveDurations)));
        metrics.put(NIBBLER_RESPONSE.getKey(), new MetricValue(NIBBLER_RESPONSE, average(nibblerResponses)));
        metrics.put(SUPPLY_USAGE.getKey(), new MetricValue(SUPPLY_USAGE, supplyUsage));
        metrics.put(DEATH_TIMELINE_EVENTS.getKey(), new MetricValue(DEATH_TIMELINE_EVENTS, deathTimelineEvents));

        final Map<String, Integer> tickLossCategories = new LinkedHashMap<>();
        tickLossCategories.put("Wave duration", (int) Math.round(sum(waveDurations)));
        tickLossCategories.put("Nibbler response", (int) Math.round(sum(nibblerResponses)));
        final TickLossBreakdown tickLossBreakdown = new TickLossBreakdown(
            tickLossCategories.get("Wave duration") + tickLossCategories.get("Nibbler response"),
            tickLossCategories);

        return new InfernoReportData(
            metrics,
            buildTimeline(opportunities),
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
        final InfernoReportData reportData = analyze(normalizedSession, activityData, opportunityMarkers);

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
        final String prayerStatus = safeText(activityData.getAttributes().get("prayerEvidenceStatus"));
        evidence.add("Prayer timing omitted because prayer evidence is " + prayerStatus + ".");
        final String deathTimeline = safeText(activityData.getAttributes().get("deathTimelineEvidence"));
        if (!deathTimeline.isEmpty())
        {
            evidence.add("Death timeline: " + deathTimeline);
        }
        evidence.add("Verification status: " + safeText(activityData.getAttributes().get("verificationStatus")));
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
            "Best execution: Nibbler response " + formatTicks(minimum(nibblerResponses)) + " ticks",
            "Death timeline captured " + deathTimelineEvents + " events");
    }

    private static String displayName(ActivitySession session)
    {
        final String displayName = safeText(session.getMetadata().get("displayName"));
        return displayName.isEmpty() ? "Inferno" : displayName;
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

    private static int intAttribute(ActivityReportData activityData, String key)
    {
        final String raw = safeText(activityData.getAttributes().get(key));
        if (raw.isEmpty())
        {
            return 0;
        }
        return Integer.parseInt(raw);
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

    private static String safeText(String value)
    {
        return value == null ? "" : value.trim();
    }

    private static String formatTicks(double value)
    {
        if (Math.rint(value) == value)
        {
            return String.valueOf((int) Math.rint(value));
        }
        return String.format(Locale.US, "%.1f", value);
    }
}
