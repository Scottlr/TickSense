package com.ticksense.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.MetricDefinition;
import com.ticksense.analytics.MetricUnit;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.OpportunityTimelineEntry;
import com.ticksense.analytics.ReportSummary;
import com.ticksense.analytics.TickLossBreakdown;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import com.ticksense.core.FinishReason;
import com.ticksense.core.FinishReasonType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;

public class JsonReportRepositoryTest
{
    @Test
    public void savesAndFindsReportById() throws IOException
    {
        final TickSenseDataPaths paths = repositoryPaths();
        final JsonReportRepository repository = repository(paths);
        final ActivityReport report = report("report-1", 1_000L, "Araxxor Kill");

        repository.save(report);
        final Optional<ActivityReport> loaded = repository.findById(report.getReportId());

        assertTrue(loaded.isPresent());
        assertEquals(ActivityReport.SCHEMA_VERSION, loaded.get().getSchemaVersion());
        assertEquals("Araxxor Kill", loaded.get().getDetectedActivityName());
        assertEquals(FinishReasonType.BOSS_DEAD, loaded.get().getFinishReason().getType());
        assertEquals(0.92D, loaded.get().getConfidence(), 0.0D);
        assertEquals(Arrays.asList("Araxxor identified", "Boss despawned"), loaded.get().getEvidenceSummary());
        assertTrue(Files.exists(paths.getReportsDirectory().resolve("report-1.json")));
        assertTrue(Files.exists(paths.getReportIndexFile()));
    }

    @Test
    public void listsRecentReportsFromIndex() throws IOException
    {
        final TickSenseDataPaths paths = repositoryPaths();
        final JsonReportRepository repository = repository(paths);
        repository.save(report("report-older", 1_000L, "Older report"));
        repository.save(report("report-newer", 2_000L, "Newer report"));

        Files.write(
            paths.getReportsDirectory().resolve("report-newer.json"),
            Collections.singletonList("{not valid json"),
            StandardCharsets.UTF_8);

        final List<ReportSummary> summaries = repository.listRecent(2);

        assertEquals(2, summaries.size());
        assertEquals("report-newer", summaries.get(0).getReportId());
        assertEquals("report-older", summaries.get(1).getReportId());
    }

    @Test
    public void rebuildsMissingIndexFromReports() throws IOException
    {
        final TickSenseDataPaths paths = repositoryPaths();
        final JsonReportRepository repository = repository(paths);
        repository.save(report("report-a", 1_000L, "First report"));
        repository.save(report("report-b", 2_000L, "Second report"));
        Files.delete(paths.getReportIndexFile());

        final List<ReportSummary> summaries = repository.listRecent(10);

        assertEquals(2, summaries.size());
        assertTrue(Files.exists(paths.getReportIndexFile()));
        assertEquals("report-b", summaries.get(0).getReportId());
    }

    @Test
    public void rebuildsCorruptIndexFromReports() throws IOException
    {
        final TickSenseDataPaths paths = repositoryPaths();
        final JsonReportRepository repository = repository(paths);
        repository.save(report("report-a", 1_000L, "First report"));
        Files.write(paths.getReportIndexFile(), Collections.singletonList("not json"), StandardCharsets.UTF_8);

        final List<ReportSummary> summaries = repository.listRecent(10);

        assertEquals(1, summaries.size());
        assertEquals("report-a", summaries.get(0).getReportId());
    }

    private JsonReportRepository repository(TickSenseDataPaths paths)
    {
        return new JsonReportRepository(paths, new Gson());
    }

    private TickSenseDataPaths repositoryPaths() throws IOException
    {
        final Path root = Files.createTempDirectory("ticksense-reports");
        return new TickSenseDataPaths(root);
    }

    private static ActivityReport report(String reportId, long createdAtMillis, String detectedActivityName)
    {
        final Map<String, MetricValue> metrics = new LinkedHashMap<>();
        metrics.put(
            "responseLatencyMillis",
            new MetricValue(
                new MetricDefinition("responseLatencyMillis", "Response latency", MetricUnit.MILLISECONDS, "Response latency"),
                284.0D));

        return new ActivityReport(
            ActivityReport.SCHEMA_VERSION,
            reportId,
            ActivityId.of("activity-" + reportId),
            ActivityType.ARAXXOR,
            detectedActivityName,
            createdAtMillis,
            219,
            131_400L,
            new FinishReason(
                FinishReasonType.BOSS_DEAD,
                new EventTime(createdAtMillis, createdAtMillis * 1_000_000L, 219, 6_570L, 438),
                0.98D,
                "Boss despawned",
                Collections.singletonList("Loot appeared")),
            0.92D,
            Arrays.asList("Araxxor identified", "Boss despawned"),
            metrics,
            Collections.singletonList(new OpportunityTimelineEntry(
                "SPIDER_ENGAGEMENT",
                "Attacked spider",
                "COMPLETED",
                120,
                createdAtMillis,
                2,
                1_200L,
                Collections.singletonList("Clicked same tick"))),
            new TickLossBreakdown(9, tickLoss()),
            Arrays.asList(
                detectedActivityName,
                "Duration: 2:11.4 / 219 ticks",
                "Total tick loss: 9"));
    }

    private static Map<String, Integer> tickLoss()
    {
        final Map<String, Integer> tickLoss = new LinkedHashMap<>();
        tickLoss.put("Spider engagement", 5);
        tickLoss.put("Boss re-engagement", 3);
        tickLoss.put("Unattributed downtime", 1);
        return tickLoss;
    }
}
