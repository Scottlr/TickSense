package com.ticksense.analytics;

import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.FinishReason;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ActivityReport
{
    public static final int SCHEMA_VERSION = 1;

    private final int schemaVersion;
    private final String reportId;
    private final ActivityId activityId;
    private final ActivityType activityType;
    private final String detectedActivityName;
    private final long createdAtMillis;
    private final int durationTicks;
    private final long durationMillis;
    private final FinishReason finishReason;
    private final double confidence;
    private final List<String> evidenceSummary;
    private final Map<String, MetricValue> metrics;
    private final List<OpportunityTimelineEntry> opportunities;
    private final TickLossBreakdown tickLossBreakdown;
    private final List<String> summaryLines;

    public ActivityReport(
        int schemaVersion,
        String reportId,
        ActivityId activityId,
        ActivityType activityType,
        String detectedActivityName,
        long createdAtMillis,
        int durationTicks,
        long durationMillis,
        FinishReason finishReason,
        double confidence,
        List<String> evidenceSummary,
        Map<String, MetricValue> metrics,
        List<OpportunityTimelineEntry> opportunities,
        TickLossBreakdown tickLossBreakdown,
        List<String> summaryLines)
    {
        this.schemaVersion = requireSchemaVersion(schemaVersion);
        this.reportId = requireText(reportId, "reportId");
        this.activityId = Objects.requireNonNull(activityId, "activityId");
        this.activityType = Objects.requireNonNull(activityType, "activityType");
        this.detectedActivityName = requireText(detectedActivityName, "detectedActivityName");
        this.createdAtMillis = requireNonNegative(createdAtMillis, "createdAtMillis");
        this.durationTicks = requireNonNegative(durationTicks, "durationTicks");
        this.durationMillis = requireNonNegative(durationMillis, "durationMillis");
        this.finishReason = Objects.requireNonNull(finishReason, "finishReason");
        this.confidence = requireConfidence(confidence);
        this.evidenceSummary = immutableStrings(evidenceSummary);
        this.metrics = immutableMetrics(metrics);
        this.opportunities = immutableOpportunities(opportunities);
        this.tickLossBreakdown = Objects.requireNonNull(tickLossBreakdown, "tickLossBreakdown");
        this.summaryLines = immutableStrings(summaryLines);
    }

    public int getSchemaVersion()
    {
        return schemaVersion;
    }

    public String getReportId()
    {
        return reportId;
    }

    public ActivityId getActivityId()
    {
        return activityId;
    }

    public ActivityType getActivityType()
    {
        return activityType;
    }

    public String getDetectedActivityName()
    {
        return detectedActivityName;
    }

    public long getCreatedAtMillis()
    {
        return createdAtMillis;
    }

    public int getDurationTicks()
    {
        return durationTicks;
    }

    public long getDurationMillis()
    {
        return durationMillis;
    }

    public FinishReason getFinishReason()
    {
        return finishReason;
    }

    public double getConfidence()
    {
        return confidence;
    }

    public List<String> getEvidenceSummary()
    {
        return evidenceSummary;
    }

    public Map<String, MetricValue> getMetrics()
    {
        return metrics;
    }

    public List<OpportunityTimelineEntry> getOpportunities()
    {
        return opportunities;
    }

    public TickLossBreakdown getTickLossBreakdown()
    {
        return tickLossBreakdown;
    }

    public List<String> getSummaryLines()
    {
        return summaryLines;
    }

    public ReportSummary toSummary()
    {
        final Map<String, Double> summaryMetrics = new LinkedHashMap<>();
        for (Map.Entry<String, MetricValue> entry : metrics.entrySet())
        {
            summaryMetrics.put(entry.getKey(), entry.getValue().getValue());
        }
        return new ReportSummary(
            schemaVersion,
            reportId,
            activityId,
            activityType,
            detectedActivityName,
            createdAtMillis,
            durationTicks,
            durationMillis,
            finishReason.getType().name(),
            confidence,
            String.join(" | ", evidenceSummary),
            summaryLines,
            summaryMetrics,
            tickLossBreakdown.getCategories());
    }

    private static int requireSchemaVersion(int schemaVersion)
    {
        if (schemaVersion <= 0)
        {
            throw new IllegalArgumentException("schemaVersion must be positive");
        }
        return schemaVersion;
    }

    private static int requireNonNegative(int value, String fieldName)
    {
        if (value < 0)
        {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }

    private static long requireNonNegative(long value, String fieldName)
    {
        if (value < 0L)
        {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }

    private static double requireConfidence(double value)
    {
        if (Double.isNaN(value) || value < 0.0D || value > 1.0D)
        {
            throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
        }
        return value;
    }

    private static String requireText(String value, String fieldName)
    {
        final String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static List<String> immutableStrings(List<String> source)
    {
        if (source == null || source.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<String> copied = new ArrayList<>(source.size());
        for (String value : source)
        {
            copied.add(requireText(value, "list value"));
        }
        return Collections.unmodifiableList(copied);
    }

    private static Map<String, MetricValue> immutableMetrics(Map<String, MetricValue> source)
    {
        if (source == null || source.isEmpty())
        {
            return Collections.emptyMap();
        }

        final Map<String, MetricValue> copied = new LinkedHashMap<>();
        for (Map.Entry<String, MetricValue> entry : source.entrySet())
        {
            copied.put(requireText(entry.getKey(), "metric key"), Objects.requireNonNull(entry.getValue(), "metricValue"));
        }
        return Collections.unmodifiableMap(copied);
    }

    private static List<OpportunityTimelineEntry> immutableOpportunities(List<OpportunityTimelineEntry> source)
    {
        if (source == null || source.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<OpportunityTimelineEntry> copied = new ArrayList<>(source.size());
        for (OpportunityTimelineEntry entry : source)
        {
            copied.add(Objects.requireNonNull(entry, "opportunity"));
        }
        return Collections.unmodifiableList(copied);
    }
}
