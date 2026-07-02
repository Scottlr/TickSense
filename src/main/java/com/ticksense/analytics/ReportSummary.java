package com.ticksense.analytics;

import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ReportSummary
{
    private final int schemaVersion;
    private final String reportId;
    private final ActivityId activityId;
    private final ActivityType activityType;
    private final String detectedActivityName;
    private final long createdAtMillis;
    private final int durationTicks;
    private final long durationMillis;
    private final String finishReason;
    private final double confidence;
    private final String evidenceSummaryText;
    private final List<String> summaryLines;

    public ReportSummary(
        int schemaVersion,
        String reportId,
        ActivityId activityId,
        ActivityType activityType,
        String detectedActivityName,
        long createdAtMillis,
        int durationTicks,
        long durationMillis,
        String finishReason,
        double confidence,
        String evidenceSummaryText,
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
        this.finishReason = requireText(finishReason, "finishReason");
        this.confidence = requireConfidence(confidence);
        this.evidenceSummaryText = safeText(evidenceSummaryText);
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

    public String getFinishReason()
    {
        return finishReason;
    }

    public double getConfidence()
    {
        return confidence;
    }

    public String getEvidenceSummaryText()
    {
        return evidenceSummaryText;
    }

    public List<String> getSummaryLines()
    {
        return summaryLines;
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

    private static String safeText(String value)
    {
        return value == null ? "" : value.trim();
    }

    private static List<String> immutableStrings(List<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<String> copied = new ArrayList<>(values.size());
        for (String value : values)
        {
            copied.add(requireText(value, "summaryLines value"));
        }
        return Collections.unmodifiableList(copied);
    }
}
