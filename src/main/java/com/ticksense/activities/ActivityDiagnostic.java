package com.ticksense.activities;

import com.ticksense.common.TextValues;
import com.ticksense.common.ImmutableCollections;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import java.util.List;
import java.util.Objects;

public final class ActivityDiagnostic
{
    private final ActivityType activityType;
    private final double confidence;
    private final String decision;
    private final String reason;
    private final EventTime time;
    private final List<String> evidence;

    public ActivityDiagnostic(
        ActivityType activityType,
        double confidence,
        String decision,
        String reason,
        EventTime time,
        List<String> evidence)
    {
        this.activityType = Objects.requireNonNull(activityType, "activityType");
        this.confidence = requireConfidence(confidence);
        this.decision = TextValues.requireText(decision, "decision");
        this.reason = TextValues.trimmedOrEmpty(reason);
        this.time = Objects.requireNonNull(time, "time");
        this.evidence = ImmutableCollections.immutableList(evidence);
    }

    public ActivityType getActivityType()
    {
        return activityType;
    }

    public double getConfidence()
    {
        return confidence;
    }

    public String getDecision()
    {
        return decision;
    }

    public String getReason()
    {
        return reason;
    }

    public EventTime getTime()
    {
        return time;
    }

    public List<String> getEvidence()
    {
        return evidence;
    }

    private static double requireConfidence(double value)
    {
        if (Double.isNaN(value) || value < 0.0D || value > 1.0D)
        {
            throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
        }
        return value;
    }
}
