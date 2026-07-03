package com.ticksense.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ticksense.activities.gemmining.GemMiningModule;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ReportSummary;
import com.ticksense.replay.TimelineReplayRunner;
import com.ticksense.runelite.TickSenseConfig;
import com.ticksense.runelite.TickSenseServices;
import com.ticksense.storage.DeleteAllDataService;
import com.ticksense.storage.JsonReportRepository;
import com.ticksense.storage.JsonlTimelineRepository;
import com.ticksense.storage.TickSenseDataPaths;
import com.ticksense.storage.debug.DebugEventKind;
import com.ticksense.storage.debug.DebugEventRecord;
import com.ticksense.storage.debug.DebugEventRecorder;
import com.ticksense.telemetry.TelemetryBus;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.ui.NotifyingReportRepository;
import com.ticksense.ui.TickSensePanel;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;
import org.junit.Test;

public class MvpPipelineTest
{
    @Test
    public void replaysGemMiningTimelineThroughFullReportPipeline() throws Exception
    {
        final TickSenseDataPaths paths = new TickSenseDataPaths(Files.createTempDirectory("ticksense-mvp-pipeline"));
        final DebugEventRecorder debugRecorder = new DebugEventRecorder(paths.getTickSenseRoot().resolve("debug"), () -> "mvp-gem");
        debugRecorder.startSession(true, 25, 5);
        final NotifyingReportRepository reportRepository = new NotifyingReportRepository(new JsonReportRepository(paths, new Gson()));
        final TickSensePanel panel = new TickSensePanel(
            reportRepository,
            new DeleteAllDataService(paths),
            null,
            debugConfig(false));
        panel.initialize();

        final TelemetryBus telemetryBus = new TelemetryBus();
        final TickSenseServices services = TickSenseServices.create(
            telemetryBus,
            new JsonlTimelineRepository(paths, "mvp-gem", new Gson(), Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC)),
            reportRepository,
            Collections.singletonList(new GemMiningModule()),
            true,
            debugRecorder);

        try
        {
            telemetryBus.addSink(debugRecorder);
            services.start();
            for (TelemetryEnvelope event : TimelineReplayRunner.loadEvents("replays/gem-mining-basic.jsonl"))
            {
                telemetryBus.accept(event);
            }
            SwingUtilities.invokeAndWait(() -> { });

            final List<ReportSummary> reports = reportRepository.listRecent(10);
            assertEquals(1, reports.size());
            assertEquals(1, panel.getReportListPanel().getReportCount());

            final ActivityReport report = reportRepository.findById(reports.get(0).getReportId()).get();
            assertEquals("Gem Mining", report.getDetectedActivityName());
            assertTrue(report.getConfidence() >= 0.75D);

            final List<DebugEventKind> debugKinds = debugEventKinds(paths.getTickSenseRoot().resolve("debug"));
            assertTrue(debugKinds.contains(DebugEventKind.NORMALIZED_TELEMETRY));
            assertTrue(debugKinds.contains(DebugEventKind.ACTIVITY_DIAGNOSTIC));
            assertTrue(debugKinds.contains(DebugEventKind.ACTIVITY_MARKER));
            assertTrue(debugKinds.contains(DebugEventKind.OPPORTUNITY_MARKER));
        }
        finally
        {
            telemetryBus.removeSink(debugRecorder);
            services.close();
            debugRecorder.close();
        }
    }

    @Test
    public void doesNotCreateNormalReportForLowConfidenceActivity() throws Exception
    {
        final TickSenseDataPaths paths = new TickSenseDataPaths(Files.createTempDirectory("ticksense-mvp-low-confidence"));
        final NotifyingReportRepository reportRepository = new NotifyingReportRepository(new JsonReportRepository(paths, new Gson()));
        final TickSensePanel panel = new TickSensePanel(
            reportRepository,
            new DeleteAllDataService(paths),
            null,
            debugConfig(false));
        panel.initialize();

        final TelemetryBus telemetryBus = new TelemetryBus();
        final TickSenseServices services = TickSenseServices.create(
            telemetryBus,
            new JsonlTimelineRepository(paths, "mvp-low", new Gson(), Clock.fixed(Instant.parse("2026-07-03T00:05:00Z"), ZoneOffset.UTC)),
            reportRepository,
            Collections.singletonList(new GemMiningModule()),
            true);

        try
        {
            services.start();
            for (TelemetryEnvelope event : TimelineReplayRunner.loadEvents("replays/ambiguous-low-confidence-no-report.jsonl"))
            {
                telemetryBus.accept(event);
            }
            SwingUtilities.invokeAndWait(() -> { });

            assertTrue(reportRepository.listRecent(10).isEmpty());
            assertEquals(0, panel.getReportListPanel().getReportCount());
        }
        finally
        {
            services.close();
        }
    }

    private static TickSenseConfig debugConfig(boolean diagnosticsEnabled)
    {
        return new TickSenseConfig()
        {
            @Override
            public boolean debugActivityDiagnostics()
            {
                return diagnosticsEnabled;
            }
        };
    }

    private static List<DebugEventKind> debugEventKinds(java.nio.file.Path debugDirectory) throws IOException
    {
        final Gson gson = new Gson();
        try (Stream<java.nio.file.Path> files = Files.list(debugDirectory))
        {
            final java.nio.file.Path debugFile = files
                .filter(path -> path.getFileName().toString().endsWith(".jsonl"))
                .findFirst()
                .get();
            return Files.readAllLines(debugFile).stream()
                .map(line -> gson.fromJson(line, DebugEventRecord.class).getKind())
                .collect(Collectors.toList());
        }
    }
}
