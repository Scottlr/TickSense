package com.ticksense.analytics;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import org.junit.Test;

public class TickLossCategoriesTest
{
    @Test
    public void buildsOrderedCategoryMap()
    {
        final Map<String, Integer> categories = TickLossCategories.builder()
            .put("Idle", 2)
            .putRounded("Movement", 2.6D)
            .build();

        assertEquals(Integer.valueOf(2), categories.get("Idle"));
        assertEquals(Integer.valueOf(3), categories.get("Movement"));
    }
}
