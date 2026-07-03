package com.ticksense.replay;

import com.ticksense.activities.ActivityDiagnostic;
import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.ActivityModule;
import com.ticksense.activities.ActivityModuleCatalog;
import com.ticksense.activities.ActivityRegistry;
import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.ActivityStrategy;
import com.ticksense.activities.ActivityStrategyEngine;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.analytics.ActivityReport;
import com.ticksense.analytics.ReportBuilder;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.ActivityType;
import com.ticksense.telemetry.TelemetryEnvelope;
import com.ticksense.telemetry.TelemetryJson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class TimelineReplayRunner
{
    private final ActivityRegistry registry;
    private final Map<ActivityType, ReportBuilder> reportBuilders;

    public TimelineReplayRunner()
    {
        this(ActivityModuleCatalog.enabledModules(ActivityModuleCatalog.productionModules()));
    }

    public TimelineReplayRunner(List<ActivityModule> modules)
    {
        this(registryFor(modules), ActivityModuleCatalog.reportBuilders(modules));
    }

    public TimelineReplayRunner(ActivityRegistry registry)
    {
        this(registry, ActivityModuleCatalog.reportBuilders(ActivityModuleCatalog.productionModules()));
    }

    public TimelineReplayRunner(ActivityRegistry registry, Map<ActivityType, ReportBuilder> reportBuilders)
    {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.reportBuilders = new LinkedHashMap<>(Objects.requireNonNull(reportBuilders, "reportBuilders"));
    }

    public ActivityReport run(String resourcePath) throws IOException
    {
        return replay(resourcePath).requireSingleReport();
    }

    public ReplayResult replay(String resourcePath) throws IOException
    {
        return replay(loadEvents(resourcePath));
    }

    public static List<TelemetryEnvelope> loadEvents(String resourcePath) throws IOException
    {
        return readResource(resourcePath);
    }

    public ReplayResult replay(List<TelemetryEnvelope> events)
    {
        final List<TelemetryEnvelope> replayEvents = immutableList(events);
        final List<ActivityMarker> activityMarkers = new ArrayList<>();
        final List<OpportunityMarker> opportunityMarkers = new ArrayList<>();
        final ActivityStrategyEngine engine = new ActivityStrategyEngine(registry, activityMarkers::add, opportunityMarkers::add, true);

        for (TelemetryEnvelope event : replayEvents)
        {
            engine.accept(event);
        }

        final List<ActivitySession> completedSessions = engine.getCompletedSessions();
        final List<ActivityReportData> completedData = engine.getCompletedActivityData();
        return new ReplayResult(
            replayEvents,
            completedSessions,
            completedData,
            activityMarkers,
            opportunityMarkers,
            engine.getDiagnostics(),
            buildReports(completedSessions, completedData, opportunityMarkers));
    }

    private List<ActivityReport> buildReports(
        List<ActivitySession> completedSessions,
        List<ActivityReportData> completedData,
        List<OpportunityMarker> opportunityMarkers)
    {
        if (completedSessions.isEmpty())
        {
            return Collections.emptyList();
        }

        final Map<ActivityId, ActivityReportData> reportDataByActivityId = new LinkedHashMap<>();
        for (ActivityReportData reportData : completedData)
        {
            reportDataByActivityId.put(reportData.getActivityId(), reportData);
        }

        final List<ActivityReport> reports = new ArrayList<>();
        for (ActivitySession session : completedSessions)
        {
            final ActivityReportData reportData = reportDataByActivityId.get(session.getActivityId());
            if (reportData == null)
            {
                throw new IllegalStateException("Missing report data for activity " + session.getActivityId());
            }
            reports.add(buildReport(session, reportData, markersFor(session.getActivityId(), opportunityMarkers)));
        }
        return Collections.unmodifiableList(reports);
    }

    private ActivityReport buildReport(
        ActivitySession session,
        ActivityReportData reportData,
        List<OpportunityMarker> opportunityMarkers)
    {
        final ReportBuilder builder = reportBuilders.get(session.getActivityType());
        if (builder == null)
        {
            throw new IllegalArgumentException("No replay report builder registered for " + session.getActivityType());
        }
        return builder.build(session, reportData, opportunityMarkers);
    }

    private static List<OpportunityMarker> markersFor(ActivityId activityId, List<OpportunityMarker> allMarkers)
    {
        final List<OpportunityMarker> matching = new ArrayList<>();
        for (OpportunityMarker marker : allMarkers)
        {
            if (activityId.equals(marker.getActivityId()))
            {
                matching.add(marker);
            }
        }
        return Collections.unmodifiableList(matching);
    }

    private static List<TelemetryEnvelope> readResource(String resourcePath) throws IOException
    {
        final InputStream stream = TimelineReplayRunner.class.getClassLoader().getResourceAsStream(resourcePath);
        if (stream == null)
        {
            throw new IOException("Replay resource not found: " + resourcePath);
        }

        final List<TelemetryEnvelope> events = new ArrayList<>();
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8))
        {
            final StringBuilder currentLine = new StringBuilder();
            int next;
            while ((next = reader.read()) != -1)
            {
                if (next == '\r')
                {
                    continue;
                }
                if (next == '\n')
                {
                    appendLine(events, currentLine);
                    currentLine.setLength(0);
                    continue;
                }
                currentLine.append((char) next);
            }
            appendLine(events, currentLine);
        }
        return Collections.unmodifiableList(events);
    }

    private static void appendLine(List<TelemetryEnvelope> events, StringBuilder currentLine)
    {
        final String line = currentLine.toString().trim();
        if (!line.isEmpty())
        {
            events.add(TelemetryJson.fromJsonLine(line));
        }
    }

    private static List<TelemetryEnvelope> immutableList(List<TelemetryEnvelope> events)
    {
        if (events == null || events.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<TelemetryEnvelope> copied = new ArrayList<>(events.size());
        for (TelemetryEnvelope event : events)
        {
            copied.add(Objects.requireNonNull(event, "event"));
        }
        return Collections.unmodifiableList(copied);
    }

    private static ActivityRegistry registryFor(List<ActivityModule> modules)
    {
        return ActivityRegistry.builder()
            .registerFactory(ActivityModuleCatalog.strategyFactory(modules))
            .build();
    }

    public static final class ReplayResult
    {
        private final List<TelemetryEnvelope> events;
        private final List<ActivitySession> completedSessions;
        private final List<ActivityReportData> completedActivityData;
        private final List<ActivityMarker> activityMarkers;
        private final List<OpportunityMarker> opportunityMarkers;
        private final List<ActivityDiagnostic> diagnostics;
        private final List<ActivityReport> reports;

        private ReplayResult(
            List<TelemetryEnvelope> events,
            List<ActivitySession> completedSessions,
            List<ActivityReportData> completedActivityData,
            List<ActivityMarker> activityMarkers,
            List<OpportunityMarker> opportunityMarkers,
            List<ActivityDiagnostic> diagnostics,
            List<ActivityReport> reports)
        {
            this.events = immutableCopy(events);
            this.completedSessions = immutableCopy(completedSessions);
            this.completedActivityData = immutableCopy(completedActivityData);
            this.activityMarkers = immutableCopy(activityMarkers);
            this.opportunityMarkers = immutableCopy(opportunityMarkers);
            this.diagnostics = immutableCopy(diagnostics);
            this.reports = immutableCopy(reports);
        }

        public List<TelemetryEnvelope> getEvents()
        {
            return events;
        }

        public List<ActivitySession> getCompletedSessions()
        {
            return completedSessions;
        }

        public List<ActivityReportData> getCompletedActivityData()
        {
            return completedActivityData;
        }

        public List<ActivityMarker> getActivityMarkers()
        {
            return activityMarkers;
        }

        public List<OpportunityMarker> getOpportunityMarkers()
        {
            return opportunityMarkers;
        }

        public List<ActivityDiagnostic> getDiagnostics()
        {
            return diagnostics;
        }

        public List<ActivityReport> getReports()
        {
            return reports;
        }

        public Optional<ActivityReport> getFirstReport()
        {
            return reports.isEmpty() ? Optional.empty() : Optional.of(reports.get(0));
        }

        public ActivityReport requireSingleReport()
        {
            if (reports.size() != 1)
            {
                throw new AssertionError("Expected exactly one replay report but found " + reports.size());
            }
            return reports.get(0);
        }

        private static <T> List<T> immutableCopy(List<T> values)
        {
            if (values == null || values.isEmpty())
            {
                return Collections.emptyList();
            }
            return Collections.unmodifiableList(new ArrayList<>(values));
        }
    }
}
