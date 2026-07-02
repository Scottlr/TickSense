package com.ticksense.analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

public class TrendAnalyzerTest
{
    @Test
    public void comparesOnlyMatchingMetricKeysAndActivityTypes()
    {
        final TrendAnalyzer analyzer = new TrendAnalyzer();
        final TrendAnalyzer.TrendSummary summary = analyzer.summarize(Arrays.asList(
            summary("gem-1", ActivityType.GEM_MINING, 1_000L, metric("latency", 5.0D), tickLoss("Idle", 2)),
            summary("gem-2", ActivityType.GEM_MINING, 2_000L, metric("latency", 3.0D), tickLoss("Idle", 1)),
            summary("gem-3", ActivityType.GEM_MINING, 3_000L, metric("redundantClicks", 7.0D), tickLoss("Movement", 1)),
            summary("arax-1", ActivityType.ARAXXOR, 4_000L, metric("latency", 9.0D), tickLoss("Spider engagement", 3))));

        assertEquals(2, summary.getActivityTrends().size());
        final TrendAnalyzer.ActivityTrend gemTrend = summary.getActivityTrends().stream()
            .filter(trend -> trend.getActivityType() == ActivityType.GEM_MINING)
            .findFirst()
            .get();
        assertTrue(gemTrend.getMetricTrends().containsKey("latency"));
        assertFalse(gemTrend.getMetricTrends().containsKey("redundantClicks"));
        assertTrue(gemTrend.getRepeatedTickLossCategories().contains("Idle"));
        assertFalse(gemTrend.getRepeatedTickLossCategories().contains("Spider engagement"));
    }

    @Test
    public void flagsSmallSampleSizes()
    {
        final TrendAnalyzer analyzer = new TrendAnalyzer();
        final TrendAnalyzer.TrendSummary summary = analyzer.summarize(Arrays.asList(
            summary("gem-1", ActivityType.GEM_MINING, 1_000L, metric("latency", 5.0D), Collections.<String, Integer>emptyMap()),
            summary("gem-2", ActivityType.GEM_MINING, 2_000L, metric("latency", 4.0D), Collections.<String, Integer>emptyMap())));

        assertEquals(1, summary.getActivityTrends().size());
        assertTrue(summary.getActivityTrends().get(0).isSmallSampleSize());
    }

    private static ReportSummary summary(
        String reportId,
        ActivityType activityType,
        long createdAtMillis,
        Map<String, Double> metricValues,
        Map<String, Integer> tickLossCategories)
    {
        return new ReportSummary(
            1,
            reportId,
            ActivityId.of("activity-" + reportId),
            activityType,
            activityType.name(),
            createdAtMillis,
            10,
            6_000L,
            "FINISHED",
            0.95D,
            "fixture",
            Collections.singletonList("summary"),
            metricValues,
            tickLossCategories);
    }

    private static Map<String, Double> metric(String key, double value)
    {
        final Map<String, Double> metrics = new LinkedHashMap<>();
        metrics.put(key, value);
        return metrics;
    }

    private static Map<String, Integer> tickLoss(String key, int value)
    {
        final Map<String, Integer> categories = new LinkedHashMap<>();
        categories.put(key, value);
        return categories;
    }
}
