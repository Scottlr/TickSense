package com.ticksense.activities.vardorvis;

import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.OpportunityEvidence;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.MetricDefinition;
import com.ticksense.analytics.MetricUnit;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.OpportunityTimelineEntry;
import com.ticksense.analytics.TickLossBreakdown;
import com.ticksense.core.ActivitySession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    public VardorvisReportData analyze(
        ActivitySession session,
        ActivityReportData activityData,
        List<OpportunityMarker> opportunityMarkers)
    {
        final ActivitySession normalizedSession = Objects.requireNonNull(session, "session");
        final ActivityReportData normalizedActivityData = Objects.requireNonNull(activityData, "activityData");
        final List<ResolvedOpportunity> opportunities = resolveOpportunities(opportunityMarkers);

        final List<Double> responseLatencies = latenciesFor(opportunities, VardorvisState.OPPORTUNITY_RANGED_HEAD_RESPONSE, OpportunityStatus.COMPLETED);
        final int damageDuringOpportunities = damageDuringFailedOpportunities(opportunities, VardorvisState.OPPORTUNITY_RANGED_HEAD_RESPONSE);
        final int completedLatencyTicks = (int) Math.round(sum(responseLatencies));
        final int failedWindowTicks = latencyTicksFor(opportunities, VardorvisState.OPPORTUNITY_RANGED_HEAD_RESPONSE, OpportunityStatus.FAILED);
        final double downtimeValue = completedLatencyTicks + failedWindowTicks;
        final double mechanicConfidenceValue = confidence(normalizedSession) * 100.0D;

        final Map<String, MetricValue> metrics = new LinkedHashMap<>();
        metrics.put(RESPONSE_LATENCY.getKey(), new MetricValue(RESPONSE_LATENCY, average(responseLatencies)));
        metrics.put(DAMAGE_DURING_OPPORTUNITIES.getKey(), new MetricValue(DAMAGE_DURING_OPPORTUNITIES, damageDuringOpportunities));
        metrics.put(DOWNTIME.getKey(), new MetricValue(DOWNTIME, downtimeValue));
        metrics.put(MECHANIC_CONFIDENCE.getKey(), new MetricValue(MECHANIC_CONFIDENCE, mechanicConfidenceValue));

        final Map<String, Integer> tickLossCategories = new LinkedHashMap<>();
        tickLossCategories.put("Response latency", completedLatencyTicks);
        tickLossCategories.put("Failed response windows", failedWindowTicks);
        final TickLossBreakdown tickLossBreakdown = new TickLossBreakdown(completedLatencyTicks + failedWindowTicks, tickLossCategories);

        return new VardorvisReportData(
            metrics,
            buildTimeline(opportunities),
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
        final VardorvisReportData reportData = analyze(normalizedSession, activityData, opportunityMarkers);

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

    private static List<ResolvedOpportunity> resolveOpportunities(List<OpportunityMarker> markers)
    {
        if (markers == null || markers.isEmpty())
        {
            return Collections.emptyList();
        }

        final Map<String, OpportunityMarker> openMarkers = new LinkedHashMap<>();
        final List<ResolvedOpportunity> resolved = new ArrayList<>();
        for (OpportunityMarker marker : markers)
        {
            if (marker.getStatus() == OpportunityStatus.OPEN)
            {
                openMarkers.put(marker.getOpportunityInstanceId(), marker);
                continue;
            }

            final OpportunityMarker open = openMarkers.get(marker.getOpportunityInstanceId());
            if (open != null)
            {
                resolved.add(new ResolvedOpportunity(open, marker));
            }
        }
        resolved.sort(Comparator.comparingInt(ResolvedOpportunity::endTick).thenComparing(ResolvedOpportunity::type));
        return Collections.unmodifiableList(resolved);
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
            for (String detail : evidenceDetails(opportunity.terminal.getEvidence()))
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
                opportunity.terminal.getStatus().name(),
                opportunity.endTick(),
                opportunity.terminal.getTime().getWallTimeMillis(),
                opportunity.latencyTicks(),
                opportunity.latencyMillis(),
                evidenceDetails(opportunity.terminal.getEvidence())));
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
        evidence.add("Vardorvis damage attribution counts only local-player damage inside verified mechanic windows.");
        evidence.add("Verification status: " + safeText(activityData.getAttributes().get("verificationStatus")));
        evidence.add("Verified mechanics: " + safeText(activityData.getAttributes().get("verifiedMechanics")));
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
            "Best execution: Ranged head response " + formatTicks(minimum(responseLatencies)) + " ticks",
            "Damage during opportunities: " + damageDuringOpportunities);
    }

    private static String displayName(ActivitySession session)
    {
        final String displayName = safeText(session.getMetadata().get("displayName"));
        return displayName.isEmpty() ? "Vardorvis" : displayName;
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

    private static final class ResolvedOpportunity
    {
        private final OpportunityMarker open;
        private final OpportunityMarker terminal;

        private ResolvedOpportunity(OpportunityMarker open, OpportunityMarker terminal)
        {
            this.open = open;
            this.terminal = terminal;
        }

        private String type()
        {
            return terminal.getOpportunityType();
        }

        private OpportunityStatus status()
        {
            return terminal.getStatus();
        }

        private int latencyTicks()
        {
            return terminal.getTime().getGameTick() - open.getTime().getGameTick();
        }

        private long latencyMillis()
        {
            return terminal.getTime().getWallTimeMillis() - open.getTime().getWallTimeMillis();
        }

        private int endTick()
        {
            return terminal.getTime().getGameTick();
        }
    }
}
