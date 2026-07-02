package com.ticksense.runelite;

import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.ActivityMarkerSink;
import com.ticksense.activities.ActivityRegistry;
import com.ticksense.activities.ActivityStrategyEngine;
import com.ticksense.activities.ActivityStrategyFactory;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.OpportunitySink;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ReportGenerationService;
import com.ticksense.storage.JsonlTimelineRepository;
import com.ticksense.storage.ReportRepository;
import com.ticksense.storage.TimelineRepository;
import com.ticksense.telemetry.TelemetryBus;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetrySink;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TickSenseServices implements AutoCloseable
{
    private final TelemetryBus telemetryBus;
    private final TimelineRepository timelineRepository;
    private final ReportRepository reportRepository;
    private final ActivityStrategyEngine strategyEngine;
    private final ReportGenerationService reportGenerationService;
    private final TelemetrySink timelineTelemetrySink;

    private boolean started;

    public static TickSenseServices createForSession(
        TelemetryBus telemetryBus,
        String sessionId,
        ReportRepository reportRepository,
        ActivityStrategyFactory strategyFactory,
        boolean diagnosticsEnabled) throws IOException
    {
        return create(
            telemetryBus,
            new JsonlTimelineRepository(sessionId),
            reportRepository,
            strategyFactory,
            diagnosticsEnabled);
    }

    public static TickSenseServices create(
        TelemetryBus telemetryBus,
        TimelineRepository timelineRepository,
        ReportRepository reportRepository,
        ActivityStrategyFactory strategyFactory,
        boolean diagnosticsEnabled)
    {
        final TelemetryBus normalizedTelemetryBus = Objects.requireNonNull(telemetryBus, "telemetryBus");
        final TimelineRepository normalizedTimelineRepository = Objects.requireNonNull(timelineRepository, "timelineRepository");
        final ReportRepository normalizedReportRepository = Objects.requireNonNull(reportRepository, "reportRepository");
        final ActivityRegistry registry = ActivityRegistry.builder()
            .registerFactory(Objects.requireNonNull(strategyFactory, "strategyFactory"))
            .build();

        final ReportGenerationService[] reportServiceHolder = new ReportGenerationService[1];
        final ActivityMarkerSink activityMarkerSink = marker -> appendActivityMarker(normalizedTimelineRepository, marker, reportServiceHolder);
        final OpportunitySink opportunitySink = marker -> appendOpportunityMarker(normalizedTimelineRepository, marker);
        final ActivityStrategyEngine strategyEngine = new ActivityStrategyEngine(registry, activityMarkerSink, opportunitySink, diagnosticsEnabled);
        final ReportGenerationService reportGenerationService =
            new ReportGenerationService(normalizedTimelineRepository, normalizedReportRepository, strategyEngine);
        reportServiceHolder[0] = reportGenerationService;

        return new TickSenseServices(
            normalizedTelemetryBus,
            normalizedTimelineRepository,
            normalizedReportRepository,
            strategyEngine,
            reportGenerationService);
    }

    private TickSenseServices(
        TelemetryBus telemetryBus,
        TimelineRepository timelineRepository,
        ReportRepository reportRepository,
        ActivityStrategyEngine strategyEngine,
        ReportGenerationService reportGenerationService)
    {
        this.telemetryBus = Objects.requireNonNull(telemetryBus, "telemetryBus");
        this.timelineRepository = Objects.requireNonNull(timelineRepository, "timelineRepository");
        this.reportRepository = Objects.requireNonNull(reportRepository, "reportRepository");
        this.strategyEngine = Objects.requireNonNull(strategyEngine, "strategyEngine");
        this.reportGenerationService = Objects.requireNonNull(reportGenerationService, "reportGenerationService");
        this.timelineTelemetrySink = this::appendTelemetryEvent;
    }

    public void start()
    {
        if (started)
        {
            return;
        }
        telemetryBus.addSink(timelineTelemetrySink);
        telemetryBus.addSink(strategyEngine);
        started = true;
    }

    public ReportRepository getReportRepository()
    {
        return reportRepository;
    }

    public ActivityStrategyEngine getStrategyEngine()
    {
        return strategyEngine;
    }

    @Override
    public void close() throws IOException
    {
        telemetryBus.removeSink(strategyEngine);
        telemetryBus.removeSink(timelineTelemetrySink);
        started = false;
        timelineRepository.close();
    }

    private void appendTelemetryEvent(TelemetryEnvelope envelope)
    {
        try
        {
            timelineRepository.append(envelope);
        }
        catch (IOException ex)
        {
            log.warn("TickSense could not append telemetry event {}", envelope.getEventId(), ex);
        }
    }

    private static void appendActivityMarker(
        TimelineRepository timelineRepository,
        ActivityMarker marker,
        ReportGenerationService[] reportServiceHolder)
    {
        try
        {
            timelineRepository.appendActivityMarker(marker);
            if ("FINISHED".equals(marker.getMarkerType()) && reportServiceHolder[0] != null)
            {
                final Optional<ActivityReport> report = reportServiceHolder[0].generateForFinishedMarker(marker);
                if (!report.isPresent())
                {
                    log.debug("No report generated for finished activity {}", marker.getActivityId());
                }
            }
        }
        catch (IOException ex)
        {
            log.warn("TickSense could not persist activity marker {}", marker.getMarkerId(), ex);
        }
    }

    private static void appendOpportunityMarker(TimelineRepository timelineRepository, OpportunityMarker marker)
    {
        try
        {
            timelineRepository.appendOpportunityMarker(marker);
        }
        catch (IOException ex)
        {
            log.warn("TickSense could not persist opportunity marker {}", marker.getMarkerId(), ex);
        }
    }
}
