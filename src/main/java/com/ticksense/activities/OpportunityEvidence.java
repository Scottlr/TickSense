package com.ticksense.activities;

import com.ticksense.core.EventTime;
import java.util.Objects;

public final class OpportunityEvidence
{
    private final EventTime time;
    private final String sourceEventType;
    private final EvidenceStrength strength;
    private final String detail;

    public OpportunityEvidence(EventTime time, String sourceEventType, EvidenceStrength strength, String detail)
    {
        this.time = Objects.requireNonNull(time, "time");
        this.sourceEventType = ActivityTexts.requireText(sourceEventType, "sourceEventType");
        this.strength = Objects.requireNonNull(strength, "strength");
        this.detail = ActivityTexts.safeText(detail);
    }

    public EventTime getTime()
    {
        return time;
    }

    public String getSourceEventType()
    {
        return sourceEventType;
    }

    public EvidenceStrength getStrength()
    {
        return strength;
    }

    public String getDetail()
    {
        return detail;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof OpportunityEvidence))
        {
            return false;
        }
        final OpportunityEvidence that = (OpportunityEvidence) other;
        return time.equals(that.time)
            && sourceEventType.equals(that.sourceEventType)
            && strength == that.strength
            && detail.equals(that.detail);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(time, sourceEventType, strength, detail);
    }
}
