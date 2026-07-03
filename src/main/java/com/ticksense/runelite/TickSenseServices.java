package com.ticksense.runelite;

import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.ActivityMarkerTypes;
import com.ticksense.activities.ActivityRegistry;
import com.ticksense.activities.ActivityStrategyEngine;
import com.ticksense.activities.ActivityStrategyFactory;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.common.ImmutableCollections;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ReportGenerationService;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.FinishReason;
import com.ticksense.storage.JsonlTimelineRepository;
import com.ticksense.storage.ReportRepository;
import com.ticksense.storage.TimelineRepository;
import com.ticksense.telemetry.TelemetryBus;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetrySink;
import com.ticksense.telemetry.events.RegionInstanceTelemetryEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final List<TelemetryEnvelope> recentTelemetry = new ArrayList<>();
    private final Map<String, OpportunityMarker> openOpportunityMarkers = new LinkedHashMap<>();

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

        final MarkerPersistenceSinks markerSinks = new MarkerPersistenceSinks(normalizedTimelineRepository);
        final ActivityStrategyEngine strategyEngine = new ActivityStrategyEngine(
            registry,
            markerSinks::appendActivityMarker,
            markerSinks::appendOpportunityMarker,
            diagnosticsEnabled);
        final ReportGenerationService reportGenerationService =
            new ReportGenerationService(normalizedTimelineRepository, normalizedReportRepository, strategyEngine);
        markerSinks.setReportGenerationService(reportGenerationService);

        final TickSenseServices services = new TickSenseServices(
            normalizedTelemetryBus,
            normalizedTimelineRepository,
            normalizedReportRepository,
            strategyEngine,
            reportGenerationService);
        markerSinks.setServices(services);
        return services;
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

    public synchronized List<TelemetryEnvelope> getRecentTelemetry()
    {
        return ImmutableCollections.immutableList(recentTelemetry);
    }

    public synchronized List<OpportunityMarker> getOpenOpportunityMarkers()
    {
        return ImmutableCollections.immutableList(openOpportunityMarkers.values());
    }

    public synchronized Optional<FinishReason> getLastFinishReason()
    {
        final List<ActivitySession> completedSessions = strategyEngine.getCompletedSessions();
        if (completedSessions.isEmpty())
        {
            return Optional.empty();
        }
        return Optional.ofNullable(completedSessions.get(completedSessions.size() - 1).getFinishReason());
    }

    public synchronized Optional<RegionInstanceTelemetryEvent> getLastRegionEvent()
    {
        for (int i = recentTelemetry.size() - 1; i >= 0; i--)
        {
            if (recentTelemetry.get(i).getEvent() instanceof RegionInstanceTelemetryEvent)
            {
                return Optional.of((RegionInstanceTelemetryEvent) recentTelemetry.get(i).getEvent());
            }
        }
        return Optional.empty();
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
        trackTelemetry(envelope);
        try
        {
            timelineRepository.append(envelope);
        }
        catch (IOException ex)
        {
            log.warn("TickSense could not append telemetry event {}", envelope.getEventId(), ex);
        }
    }

    private synchronized void trackTelemetry(TelemetryEnvelope envelope)
    {
        if (recentTelemetry.size() == 50)
        {
            recentTelemetry.remove(0);
        }
        recentTelemetry.add(envelope);
    }

    private synchronized void trackOpportunityMarker(OpportunityMarker marker)
    {
        if (marker.getStatus() == com.ticksense.activities.OpportunityStatus.OPEN)
        {
            openOpportunityMarkers.put(marker.getOpportunityInstanceId(), marker);
        }
        else
        {
            openOpportunityMarkers.remove(marker.getOpportunityInstanceId());
        }
    }

    private static final class MarkerPersistenceSinks
    {
        private final TimelineRepository timelineRepository;
        private ReportGenerationService reportGenerationService;
        private TickSenseServices services;

        private MarkerPersistenceSinks(TimelineRepository timelineRepository)
        {
            this.timelineRepository = Objects.requireNonNull(timelineRepository, "timelineRepository");
        }

        private void setReportGenerationService(ReportGenerationService reportGenerationService)
        {
            this.reportGenerationService = Objects.requireNonNull(reportGenerationService, "reportGenerationService");
        }

        private void setServices(TickSenseServices services)
        {
            this.services = Objects.requireNonNull(services, "services");
        }

        private void appendActivityMarker(ActivityMarker marker)
        {
            try
            {
                timelineRepository.appendActivityMarker(marker);
                if (ActivityMarkerTypes.isFinished(marker) && reportGenerationService != null)
                {
                    final Optional<ActivityReport> report = reportGenerationService.generateForFinishedMarker(marker);
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

        private void appendOpportunityMarker(OpportunityMarker marker)
        {
            try
            {
                timelineRepository.appendOpportunityMarker(marker);
                if (services != null)
                {
                    services.trackOpportunityMarker(marker);
                }
            }
            catch (IOException ex)
            {
                log.warn("TickSense could not persist opportunity marker {}", marker.getMarkerId(), ex);
            }
        }
    }
}
