package com.ticksense.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.MetricDefinition;
import com.ticksense.analytics.MetricUnit;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.OpportunityTimelineEntry;
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
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

public class ReportIndexMaintenanceServiceTest
{
    @Test
    public void rebuildsIndexFromReports() throws IOException
    {
        final TickSenseDataPaths paths = new TickSenseDataPaths(Files.createTempDirectory("ticksense-index-maint"));
        final JsonReportRepository repository = new JsonReportRepository(paths, new Gson());
        repository.save(report("report-a", 1_000L));
        repository.save(report("report-b", 2_000L));
        Files.delete(paths.getReportIndexFile());

        final ReportIndexMaintenanceService service = new ReportIndexMaintenanceService(
            paths,
            repository,
            Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC));

        assertEquals(2, service.rebuildIndex().size());
        assertTrue(Files.exists(paths.getReportIndexFile()));
    }

    @Test
    public void retentionDeletesOnlyTickSenseFiles() throws IOException
    {
        final Path tempRoot = Files.createTempDirectory("ticksense-retention-maint");
        final TickSenseDataPaths paths = new TickSenseDataPaths(tempRoot.resolve("ticksense"));
        final JsonReportRepository repository = new JsonReportRepository(paths, new Gson());
        repository.save(report("report-old", 1_000L));
        repository.save(report("report-new", 2_000L));

        Files.createDirectories(paths.getTimelinesDirectory());
        final Path oldTimeline = paths.getTimelinesDirectory().resolve("old.jsonl");
        final Path recentTimeline = paths.getTimelinesDirectory().resolve("recent.jsonl");
        Files.write(oldTimeline, Collections.singletonList("{}"), StandardCharsets.UTF_8);
        Files.write(recentTimeline, Collections.singletonList("{}"), StandardCharsets.UTF_8);
        Files.setLastModifiedTime(oldTimeline, FileTime.from(Instant.parse("2026-05-01T00:00:00Z")));
        Files.setLastModifiedTime(recentTimeline, FileTime.from(Instant.parse("2026-07-02T00:00:00Z")));

        final Path outsideFile = tempRoot.resolve("outside.json");
        Files.write(outsideFile, Collections.singletonList("keep"), StandardCharsets.UTF_8);

        final ReportIndexMaintenanceService service = new ReportIndexMaintenanceService(
            paths,
            repository,
            Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC));

        final ReportIndexMaintenanceService.RetentionResult result = service.applyRetention(new RetentionPolicy(30, 1, false));

        assertEquals(1, result.getDeletedTimelineCount());
        assertEquals(1, result.getDeletedReportCount());
        assertTrue(Files.exists(recentTimeline));
        assertFalse(Files.exists(oldTimeline));
        assertTrue(Files.exists(outsideFile));
        assertFalse(Files.exists(paths.getReportsDirectory().resolve("report-old.json")));
        assertTrue(Files.exists(paths.getReportsDirectory().resolve("report-new.json")));
    }

    private static ActivityReport report(String reportId, long createdAtMillis)
    {
        final Map<String, MetricValue> metrics = new LinkedHashMap<>();
        metrics.put(
            "latency",
            new MetricValue(
                new MetricDefinition("latency", "Latency", MetricUnit.MILLISECONDS, "Latency"),
                createdAtMillis));

        return new ActivityReport(
            ActivityReport.SCHEMA_VERSION,
            reportId,
            ActivityId.of("activity-" + reportId),
            ActivityType.GEM_MINING,
            "Gem Mining",
            createdAtMillis,
            12,
            7_200L,
            new FinishReason(
                FinishReasonType.LEFT_REGION,
                new EventTime(createdAtMillis, createdAtMillis * 1_000_000L, 12, 12L, 2),
                0.90D,
                "Left region",
                Collections.singletonList("Fixture evidence")),
            0.95D,
            Collections.singletonList("Gem mining identified"),
            metrics,
            Collections.singletonList(new OpportunityTimelineEntry(
                "GEM_ROCK",
                "Mined rock",
                "COMPLETED",
                10,
                createdAtMillis,
                1,
                600L,
                Collections.singletonList("Same tick"))),
            new TickLossBreakdown(1, Collections.singletonMap("Idle", 1)),
            Arrays.asList("Gem Mining", "Total tick loss: 1"));
    }
}
