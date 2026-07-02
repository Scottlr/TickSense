package com.ticksense.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class FinishReason
{
    private final FinishReasonType type;
    private final EventTime time;
    private final double confidence;
    private final String explanation;
    private final List<String> evidence;

    public FinishReason(
        FinishReasonType type,
        EventTime time,
        double confidence,
        String explanation,
        List<String> evidence)
    {
        this.type = Objects.requireNonNull(type, "type");
        this.time = Objects.requireNonNull(time, "time");
        this.confidence = requireConfidence(confidence);
        this.explanation = explanation == null ? "" : explanation;
        this.evidence = immutableCopy(evidence);
    }

    public FinishReasonType getType()
    {
        return type;
    }

    public EventTime getTime()
    {
        return time;
    }

    public double getConfidence()
    {
        return confidence;
    }

    public String getExplanation()
    {
        return explanation;
    }

    public List<String> getEvidence()
    {
        return evidence;
    }

    private static double requireConfidence(double confidence)
    {
        if (Double.isNaN(confidence) || confidence < 0.0D || confidence > 1.0D)
        {
            throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
        }
        return confidence;
    }

    private static List<String> immutableCopy(List<String> source)
    {
        if (source == null || source.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(source));
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof FinishReason))
        {
            return false;
        }
        final FinishReason that = (FinishReason) other;
        return Double.compare(that.confidence, confidence) == 0
            && type == that.type
            && time.equals(that.time)
            && explanation.equals(that.explanation)
            && evidence.equals(that.evidence);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, time, confidence, explanation, evidence);
    }
}
