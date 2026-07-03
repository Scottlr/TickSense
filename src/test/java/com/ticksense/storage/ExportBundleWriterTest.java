package com.ticksense.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ticksense.activities.ActivityDiagnostic;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.MetricDefinition;
import com.ticksense.analytics.MetricUnit;
import com.ticksense.analytics.MetricValue;
import com.ticksense.analytics.OpportunityTimelineEntry;
import com.ticksense.analytics.TickLossBreakdown;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EntityRef;
import com.ticksense.core.EventTime;
import com.ticksense.core.FinishReason;
import com.ticksense.core.FinishReasonType;
import com.ticksense.core.WorldLocation;
import com.ticksense.storage.debug.DebugEventKind;
import com.ticksense.storage.debug.DebugEventRecord;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.events.GameTickTelemetryEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;
import org.junit.Test;

public class ExportBundleWriterTest
{
    @Test
    public void writesExpectedBundleFiles() throws IOException
    {
        final TickSenseDataPaths paths = new TickSenseDataPaths(Files.createTempDirectory("ticksense-export-bundle"));
        final JsonReportRepository reportRepository = new JsonReportRepository(paths, new Gson());
        final ActivityReport report = report("report-export");
        reportRepository.save(report);

        final JsonlTimelineRepository timelineRepository = new JsonlTimelineRepository(
            paths,
            "session-export",
            new Gson(),
            Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC));
        timelineRepository.append(telemetryEnvelope("export-event", 10));
        timelineRepository.appendActivityMarker(new com.ticksense.activities.ActivityMarker(
            "marker-start",
            report.getActivityId(),
            report.getActivityType(),
            "STARTED",
            new EventTime(1_000L, 1_000_000L, 10, 10L, 1),
            Collections.singletonMap("displayName", "Gem Mining")));
        timelineRepository.appendActivityMarker(new com.ticksense.activities.ActivityMarker(
            "marker-finish",
            report.getActivityId(),
            report.getActivityType(),
            "FINISHED",
            new EventTime(2_000L, 2_000_000L, 12, 12L, 2),
            Collections.singletonMap("finishReasonType", "LEFT_REGION")));
        timelineRepository.close();
        final Path debugDirectory = paths.getTickSenseRoot().resolve("debug");
        Files.createDirectories(debugDirectory);
        Files.write(
            debugDirectory.resolve("session-export.jsonl"),
            Arrays.asList(
                new Gson().toJson(DebugEventRecord.of(
                    DebugEventKind.ADAPTER_OBSERVATION,
                    "session-export",
                    "ProjectileMoved",
                    new EventTime(1_500L, 1_500_000L, 11, 11L, 1),
                    "{\"projectileId\":1234}")),
                new Gson().toJson(DebugEventRecord.of(
                    DebugEventKind.ADAPTER_OBSERVATION,
                    "session-export",
                    "ProjectileMoved",
                    new EventTime(3_000L, 3_000_000L, 30, 30L, 3),
                    "{\"projectileId\":9999}"))),
            StandardCharsets.UTF_8);

        final ExportBundleWriter writer = new ExportBundleWriter(
            paths,
            reportRepository,
            new Gson(),
            debugConfigSnapshot(),
            () -> Collections.singletonList(new ActivityDiagnostic(
                ActivityType.GEM_MINING,
                0.80D,
                "STARTED",
                "",
                new EventTime(1_000L, 1_000_000L, 10, 10L, 1),
                Collections.singletonList("fixture"))),
            Clock.fixed(Instant.parse("2026-07-03T01:00:00Z"), ZoneOffset.UTC));

        final Path zipPath = writer.writeBundle(report.getReportId(), paths.getExportsDirectory());

        assertTrue(Files.exists(zipPath));
        try (ZipFile zipFile = new ZipFile(zipPath.toFile()))
        {
            assertTrue(zipFile.getEntry("bundle.json") != null);
            assertTrue(zipFile.getEntry("plugin-config.json") != null);
            assertTrue(zipFile.getEntry("report.json") != null);
            assertTrue(zipFile.getEntry("activity.json") != null);
            assertTrue(zipFile.getEntry("timeline.jsonl") != null);
            assertTrue(zipFile.getEntry("diagnostics.jsonl") != null);
            assertTrue(zipFile.getEntry("debug-events.jsonl") != null);
            final String debugEvents = zipEntryText(zipFile, "debug-events.jsonl");
            assertTrue(debugEvents.contains("1234"));
            assertFalse(debugEvents.contains("9999"));
        }
    }

    @Test
    public void omitsDisallowedIdentifiers() throws IOException
    {
        final TickSenseDataPaths paths = new TickSenseDataPaths(Files.createTempDirectory("ticksense-export-redaction"));
        final JsonReportRepository reportRepository = new JsonReportRepository(paths, new Gson());
        final ActivityReport report = report("report-redaction");
        reportRepository.save(report);

        final JsonlTimelineRepository timelineRepository = new JsonlTimelineRepository(
            paths,
            "session-redaction",
            new Gson(),
            Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC));
        timelineRepository.appendActivityMarker(new com.ticksense.activities.ActivityMarker(
            "marker-start",
            report.getActivityId(),
            report.getActivityType(),
            "STARTED",
            new EventTime(1_000L, 1_000_000L, 10, 10L, 1),
            Collections.singletonMap("displayName", "Gem Mining")));
        timelineRepository.appendActivityMarker(new com.ticksense.activities.ActivityMarker(
            "marker-finish",
            report.getActivityId(),
            report.getActivityType(),
            "FINISHED",
            new EventTime(2_000L, 2_000_000L, 12, 12L, 2),
            Collections.singletonMap("finishReasonType", "LEFT_REGION")));
        timelineRepository.close();

        final ExportBundleWriter writer = new ExportBundleWriter(
            paths,
            reportRepository,
            new Gson(),
            debugConfigSnapshot(),
            Collections::emptyList,
            Clock.fixed(Instant.parse("2026-07-03T01:00:00Z"), ZoneOffset.UTC));

        final Path zipPath = writer.writeBundle(report.getReportId(), paths.getExportsDirectory());
        final String zipText = new String(Files.readAllBytes(zipPath), StandardCharsets.ISO_8859_1);

        assertFalse(zipText.contains("playerName"));
        assertFalse(zipText.contains("accountId"));
        assertFalse(zipText.contains("chatMessage"));
    }

    @Test
    public void failsWithoutWritingPartialZipOnMissingTimeline() throws IOException
    {
        final TickSenseDataPaths paths = new TickSenseDataPaths(Files.createTempDirectory("ticksense-export-missing"));
        final JsonReportRepository reportRepository = new JsonReportRepository(paths, new Gson());
        final ActivityReport report = report("report-missing");
        reportRepository.save(report);

        final ExportBundleWriter writer = new ExportBundleWriter(
            paths,
            reportRepository,
            new Gson(),
            debugConfigSnapshot(),
            Collections::emptyList,
            Clock.fixed(Instant.parse("2026-07-03T01:00:00Z"), ZoneOffset.UTC));

        boolean threw = false;
        try
        {
            writer.writeBundle(report.getReportId(), paths.getExportsDirectory());
        }
        catch (IOException expected)
        {
            threw = true;
        }

        assertTrue(threw);
        assertFalse(Files.exists(paths.getExportsDirectory().resolve("ticksense-bundle-" + report.getReportId() + ".zip")));
    }

    private static TelemetryEnvelope telemetryEnvelope(String eventId, int tick)
    {
        return new TelemetryEnvelope(
            1,
            eventId,
            "session-export",
            new GameTickTelemetryEvent(
                new EventTime(1_000L, 1_000_000L, tick, tick, tick),
                Collections.singletonMap("fixture", "export"),
                tick));
    }

    private static String zipEntryText(ZipFile zipFile, String entryName) throws IOException
    {
        final java.io.InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(entryName));
        try
        {
            final java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, read);
            }
            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        }
        finally
        {
            inputStream.close();
        }
    }

    private static ExportConfigSnapshotProvider debugConfigSnapshot()
    {
        return () ->
        {
            final Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("debugEventRecorder", true);
            snapshot.put("debugActivityDiagnostics", true);
            snapshot.put("maxDebugFileSizeMb", 25);
            snapshot.put("maxDebugSessions", 5);
            return snapshot;
        };
    }

    private static ActivityReport report(String reportId)
    {
        final Map<String, MetricValue> metrics = new LinkedHashMap<>();
        metrics.put(
            "latency",
            new MetricValue(
                new MetricDefinition("latency", "Latency", MetricUnit.MILLISECONDS, "Latency"),
                120.0D));

        return new ActivityReport(
            ActivityReport.SCHEMA_VERSION,
            reportId,
            ActivityId.of("activity-" + reportId),
            ActivityType.GEM_MINING,
            "Gem Mining",
            1_000L,
            12,
            7_200L,
            new FinishReason(
                FinishReasonType.LEFT_REGION,
                new EventTime(2_000L, 2_000_000L, 12, 12L, 2),
                0.90D,
                "Left the region",
                Collections.singletonList("Fixture evidence")),
            0.95D,
            Collections.singletonList("Gem mining identified"),
            metrics,
            Collections.singletonList(new OpportunityTimelineEntry(
                "GEM_ROCK",
                "Mined gem rock",
                "COMPLETED",
                10,
                1_000L,
                1,
                600L,
                Collections.singletonList("Same tick"))),
            new TickLossBreakdown(1, Collections.singletonMap("Idle", 1)),
            Arrays.asList("Gem Mining", "Total tick loss: 1"));
    }
}
