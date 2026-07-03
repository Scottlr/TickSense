package com.ticksense.analytics;

import com.ticksense.common.TextValues;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OpportunityTimelineEntry
{
    private final String opportunityType;
    private final String label;
    private final String status;
    private final int gameTick;
    private final long wallTimeMillis;
    private final Integer latencyTicks;
    private final Long latencyMillis;
    private final List<String> evidenceSummary;

    public OpportunityTimelineEntry(
        String opportunityType,
        String label,
        String status,
        int gameTick,
        long wallTimeMillis,
        Integer latencyTicks,
        Long latencyMillis,
        List<String> evidenceSummary)
    {
        this.opportunityType = TextValues.requireText(opportunityType, "opportunityType");
        this.label = TextValues.requireText(label, "label");
        this.status = TextValues.requireText(status, "status");
        this.gameTick = requireNonNegative(gameTick, "gameTick");
        this.wallTimeMillis = requireNonNegative(wallTimeMillis, "wallTimeMillis");
        this.latencyTicks = latencyTicks == null ? null : requireNonNegative(latencyTicks, "latencyTicks");
        this.latencyMillis = latencyMillis == null ? null : requireNonNegative(latencyMillis, "latencyMillis");
        this.evidenceSummary = AnalyticsCollections.immutableTextList(evidenceSummary, "evidenceSummary");
    }

    public String getOpportunityType()
    {
        return opportunityType;
    }

    public String getLabel()
    {
        return label;
    }

    public String getStatus()
    {
        return status;
    }

    public int getGameTick()
    {
        return gameTick;
    }

    public long getWallTimeMillis()
    {
        return wallTimeMillis;
    }

    public Integer getLatencyTicks()
    {
        return latencyTicks;
    }

    public Long getLatencyMillis()
    {
        return latencyMillis;
    }

    public List<String> getEvidenceSummary()
    {
        return evidenceSummary;
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
}
