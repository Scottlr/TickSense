package com.ticksense.analytics;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TickLossCategories
{
    private final Map<String, Integer> categories = new LinkedHashMap<>();

    private TickLossCategories()
    {
    }

    public static TickLossCategories builder()
    {
        return new TickLossCategories();
    }

    public TickLossCategories put(String label, int ticks)
    {
        categories.put(label, ticks);
        return this;
    }

    public TickLossCategories putRounded(String label, double ticks)
    {
        return put(label, (int) Math.round(ticks));
    }

    public Map<String, Integer> build()
    {
        return new LinkedHashMap<>(categories);
    }
}
