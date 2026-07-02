package com.ticksense.activities;

import com.ticksense.core.ActivityType;
import java.util.Objects;

public final class ActivityDefinition
{
    private final ActivityType activityType;
    private final String displayName;
    private final int priority;
    private final double activationThreshold;
    private final boolean bossActivity;

    public ActivityDefinition(
        ActivityType activityType,
        String displayName,
        int priority,
        double activationThreshold,
        boolean bossActivity)
    {
        this.activityType = Objects.requireNonNull(activityType, "activityType");
        this.displayName = requireText(displayName, "displayName");
        this.priority = priority;
        this.activationThreshold = requireThreshold(activationThreshold);
        this.bossActivity = bossActivity;
    }

    public ActivityType getActivityType()
    {
        return activityType;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public int getPriority()
    {
        return priority;
    }

    public double getActivationThreshold()
    {
        return activationThreshold;
    }

    public boolean isBossActivity()
    {
        return bossActivity;
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

    private static double requireThreshold(double threshold)
    {
        if (Double.isNaN(threshold) || threshold < 0.0D || threshold > 1.0D)
        {
            throw new IllegalArgumentException("activationThreshold must be between 0.0 and 1.0");
        }
        return threshold;
    }
}
