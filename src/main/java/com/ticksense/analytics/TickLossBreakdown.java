package com.ticksense.analytics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class TickLossBreakdown
{
    private final int totalTickLoss;
    private final Map<String, Integer> categories;

    public TickLossBreakdown(int totalTickLoss, Map<String, Integer> categories)
    {
        this.totalTickLoss = requireNonNegative(totalTickLoss, "totalTickLoss");
        this.categories = immutableCategories(categories);
        validateTotal();
    }

    public int getTotalTickLoss()
    {
        return totalTickLoss;
    }

    public Map<String, Integer> getCategories()
    {
        return categories;
    }

    private void validateTotal()
    {
        int summed = 0;
        for (Integer value : categories.values())
        {
            summed += value;
        }
        if (summed != totalTickLoss)
        {
            throw new IllegalArgumentException("Tick loss categories must add up to totalTickLoss");
        }
    }

    private static int requireNonNegative(int value, String fieldName)
    {
        if (value < 0)
        {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }

    private static Map<String, Integer> immutableCategories(Map<String, Integer> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyMap();
        }

        final Map<String, Integer> copied = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : values.entrySet())
        {
            final String key = Objects.requireNonNull(entry.getKey(), "category key").trim();
            if (key.isEmpty())
            {
                throw new IllegalArgumentException("category key must not be blank");
            }
            copied.put(key, requireNonNegative(Objects.requireNonNull(entry.getValue(), "category value"), "category value"));
        }
        return Collections.unmodifiableMap(copied);
    }
}
