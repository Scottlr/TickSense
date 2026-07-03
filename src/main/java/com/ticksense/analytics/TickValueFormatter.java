package com.ticksense.analytics;

import java.util.Locale;

public final class TickValueFormatter
{
    private TickValueFormatter()
    {
    }

    public static String formatTicks(double value)
    {
        if (Math.rint(value) == value)
        {
            return String.valueOf((int) Math.rint(value));
        }
        return String.format(Locale.US, "%.1f", value);
    }
}
