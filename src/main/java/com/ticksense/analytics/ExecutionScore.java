package com.ticksense.analytics;

public final class ExecutionScore
{
    private static final double MIN_SCORE = 0.0D;
    private static final double MAX_SCORE = 100.0D;

    private final double value;

    public ExecutionScore(double value)
    {
        this.value = clamp(value);
    }

    public double getValue()
    {
        return value;
    }

    public double getNormalizedValue()
    {
        return value / MAX_SCORE;
    }

    private static double clamp(double value)
    {
        if (Double.isNaN(value) || Double.isInfinite(value))
        {
            throw new IllegalArgumentException("score must be finite");
        }
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, value));
    }
}
