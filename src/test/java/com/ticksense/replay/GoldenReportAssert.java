package com.ticksense.replay;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.OpportunityTimelineEntry;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GoldenReportAssert
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private GoldenReportAssert()
    {
    }

    public static void matches(String resourcePath, ActivityReport report) throws IOException
    {
        final JsonElement expected = loadGolden(resourcePath);
        final JsonElement actual = new JsonParser().parse(GSON.toJson(canonicalize(report)));
        assertEquals("Golden report mismatch for " + resourcePath, expected, actual);
    }

    private static JsonElement loadGolden(String resourcePath) throws IOException
    {
        final InputStream stream = GoldenReportAssert.class.getClassLoader().getResourceAsStream(resourcePath);
        if (stream == null)
        {
            throw new IOException("Golden resource not found: " + resourcePath);
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8))
        {
            return new JsonParser().parse(reader);
        }
    }

    private static Map<String, Object> canonicalize(ActivityReport report)
    {
        final Map<String, Object> root = new LinkedHashMap<>();
        root.put("schemaVersion", report.getSchemaVersion());
        root.put("activityType", report.getActivityType().name());
        root.put("detectedActivityName", report.getDetectedActivityName());
        root.put("durationTicks", report.getDurationTicks());
        root.put("durationMillis", report.getDurationMillis());
        root.put("finishReason", finishReason(report));
        root.put("confidence", report.getConfidence());
        root.put("evidenceSummary", report.getEvidenceSummary());
        root.put("metrics", metrics(report));
        root.put("opportunities", opportunities(report));
        root.put("tickLossBreakdown", tickLossBreakdown(report));
        root.put("summaryLines", report.getSummaryLines());
        return root;
    }

    private static Map<String, Object> finishReason(ActivityReport report)
    {
        final Map<String, Object> finishReason = new LinkedHashMap<>();
        finishReason.put("type", report.getFinishReason().getType().name());
        finishReason.put("confidence", report.getFinishReason().getConfidence());
        finishReason.put("explanation", report.getFinishReason().getExplanation());
        finishReason.put("evidence", report.getFinishReason().getEvidence());
        finishReason.put("gameTick", report.getFinishReason().getTime().getGameTick());
        return finishReason;
    }

    private static Map<String, Object> metrics(ActivityReport report)
    {
        final Map<String, Object> metrics = new LinkedHashMap<>();
        for (Map.Entry<String, MetricValue> entry : report.getMetrics().entrySet())
        {
            final MetricValue metricValue = entry.getValue();
            final Map<String, Object> metric = new LinkedHashMap<>();
            metric.put("displayName", metricValue.getDefinition().getDisplayName());
            metric.put("unit", metricValue.getDefinition().getUnit().name());
            metric.put("lowerValueBetter", metricValue.getDefinition().isLowerValueBetter());
            metric.put("value", metricValue.getValue());
            metrics.put(entry.getKey(), metric);
        }
        return metrics;
    }

    private static List<Map<String, Object>> opportunities(ActivityReport report)
    {
        final List<Map<String, Object>> opportunities = new ArrayList<>();
        for (OpportunityTimelineEntry entry : report.getOpportunities())
        {
            final Map<String, Object> opportunity = new LinkedHashMap<>();
            opportunity.put("type", entry.getOpportunityType());
            opportunity.put("label", entry.getLabel());
            opportunity.put("status", entry.getStatus());
            opportunity.put("gameTick", entry.getGameTick());
            opportunity.put("latencyTicks", entry.getLatencyTicks());
            opportunity.put("latencyMillis", entry.getLatencyMillis());
            opportunity.put("evidenceSummary", entry.getEvidenceSummary());
            opportunities.add(opportunity);
        }
        return opportunities;
    }

    private static Map<String, Object> tickLossBreakdown(ActivityReport report)
    {
        final Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("totalTickLoss", report.getTickLossBreakdown().getTotalTickLoss());
        breakdown.put("categories", report.getTickLossBreakdown().getCategories());
        return breakdown;
    }
}
