package com.ticksense.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.ticksense.activities.ActivityStrategyFactory;
import com.ticksense.activities.gemmining.GemMiningStrategy;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ReportSummary;
import com.ticksense.replay.TimelineReplayRunner;
import com.ticksense.runelite.TickSenseConfig;
import com.ticksense.runelite.TickSenseServices;
import com.ticksense.storage.DeleteAllDataService;
import com.ticksense.storage.JsonReportRepository;
import com.ticksense.storage.JsonlTimelineRepository;
import com.ticksense.storage.TickSenseDataPaths;
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
import javax.swing.SwingUtilities;
import org.junit.Test;

public class MvpPipelineTest
{
    @Test
    public void replaysGemMiningTimelineThroughFullReportPipeline() throws Exception
    {
        final TickSenseDataPaths paths = new TickSenseDataPaths(Files.createTempDirectory("ticksense-mvp-pipeline"));
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
            gemMiningFactory(),
            true);

        try
        {
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
        }
        finally
        {
            services.close();
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
            gemMiningFactory(),
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

    private static ActivityStrategyFactory gemMiningFactory()
    {
        return () -> Collections.singletonList(new GemMiningStrategy());
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
}
