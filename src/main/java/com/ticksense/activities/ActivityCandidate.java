package com.ticksense.activities;

import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ActivityCandidate
{
    private final ActivityId activityId;
    private final ActivityType activityType;
    private final double confidence;
    private final List<String> evidenceSummary;
    private final EventTime firstEvidenceTime;
    private final boolean suppressed;
    private final String suppressionReason;

    public ActivityCandidate(
        ActivityId activityId,
        ActivityType activityType,
        double confidence,
        List<String> evidenceSummary,
        EventTime firstEvidenceTime,
        boolean suppressed,
        String suppressionReason)
    {
        this.activityId = Objects.requireNonNull(activityId, "activityId");
        this.activityType = Objects.requireNonNull(activityType, "activityType");
        this.confidence = requireConfidence(confidence);
        this.evidenceSummary = immutableList(evidenceSummary);
        this.firstEvidenceTime = Objects.requireNonNull(firstEvidenceTime, "firstEvidenceTime");
        this.suppressed = suppressed;
        this.suppressionReason = safeText(suppressionReason);
    }

    public ActivityId getActivityId()
    {
        return activityId;
    }

    public ActivityType getActivityType()
    {
        return activityType;
    }

    public double getConfidence()
    {
        return confidence;
    }

    public List<String> getEvidenceSummary()
    {
        return evidenceSummary;
    }

    public EventTime getFirstEvidenceTime()
    {
        return firstEvidenceTime;
    }

    public boolean isSuppressed()
    {
        return suppressed;
    }

    public String getSuppressionReason()
    {
        return suppressionReason;
    }

    public boolean isStrong(double threshold)
    {
        if (Double.isNaN(threshold) || threshold < 0.0D || threshold > 1.0D)
        {
            throw new IllegalArgumentException("threshold must be between 0.0 and 1.0");
        }
        return confidence >= threshold;
    }

    private static double requireConfidence(double value)
    {
        if (Double.isNaN(value) || value < 0.0D || value > 1.0D)
        {
            throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
        }
        return value;
    }

    private static List<String> immutableList(List<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    private static String safeText(String value)
    {
        return value == null ? "" : value.trim();
    }
}
