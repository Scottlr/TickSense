package com.ticksense.storage;

public final class RetentionPolicy
{
    private final int maxRawTimelineDays;
    private final int maxReportCount;
    private final boolean keepReportsForever;

    public RetentionPolicy(int maxRawTimelineDays, int maxReportCount, boolean keepReportsForever)
    {
        if (maxRawTimelineDays < 0)
        {
            throw new IllegalArgumentException("maxRawTimelineDays must not be negative");
        }
        if (maxReportCount < 0)
        {
            throw new IllegalArgumentException("maxReportCount must not be negative");
        }
        this.maxRawTimelineDays = maxRawTimelineDays;
        this.maxReportCount = maxReportCount;
        this.keepReportsForever = keepReportsForever;
    }

    public int getMaxRawTimelineDays()
    {
        return maxRawTimelineDays;
    }

    public int getMaxReportCount()
    {
        return maxReportCount;
    }

    public boolean isKeepReportsForever()
    {
        return keepReportsForever;
    }
}
