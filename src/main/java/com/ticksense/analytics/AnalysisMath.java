package com.ticksense.analytics;

import com.ticksense.activities.ActivityReportData;
import com.ticksense.common.TextValues;
import java.util.List;

public final class AnalysisMath
{
    private AnalysisMath()
    {
    }

    public static double average(List<Double> values)
    {
        if (values == null || values.isEmpty())
        {
            return 0.0D;
        }
        return sum(values) / values.size();
    }

    public static double sum(List<Double> values)
    {
        double sum = 0.0D;
        if (values != null)
        {
            for (Double value : values)
            {
                sum += value;
            }
        }
        return sum;
    }

    public static double minimum(List<Double> values)
    {
        double minimum = values.get(0);
        for (Double value : values)
        {
            minimum = Math.min(minimum, value);
        }
        return minimum;
    }

    public static double maximum(List<Double> values)
    {
        double maximum = values.get(0);
        for (Double value : values)
        {
            maximum = Math.max(maximum, value);
        }
        return maximum;
    }

    public static int intAttribute(ActivityReportData activityData, String key)
    {
        final String raw = TextValues.trimmedOrEmpty(activityData.getAttributes().get(key));
        if (raw.isEmpty())
        {
            return 0;
        }
        return Integer.parseInt(raw);
    }
}
