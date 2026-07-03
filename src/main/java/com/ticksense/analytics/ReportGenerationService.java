package com.ticksense.analytics;

import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.ActivityStrategyEngine;
import com.ticksense.activities.construction.ConstructionAnalyzer;
import com.ticksense.activities.inferno.InfernoAnalyzer;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.activities.gemmining.GemMiningAnalyzer;
import com.ticksense.activities.vardorvis.VardorvisAnalyzer;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.ActivityType;
import com.ticksense.storage.CompletedActivityTimeline;
import com.ticksense.storage.ReportRepository;
import com.ticksense.storage.TimelineRepository;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ReportGenerationService
{
    private final TimelineRepository timelineRepository;
    private final ReportRepository reportRepository;
    private final ActivityStrategyEngine strategyEngine;

    public ReportGenerationService(
        TimelineRepository timelineRepository,
        ReportRepository reportRepository,
        ActivityStrategyEngine strategyEngine)
    {
        this.timelineRepository = Objects.requireNonNull(timelineRepository, "timelineRepository");
        this.reportRepository = Objects.requireNonNull(reportRepository, "reportRepository");
        this.strategyEngine = Objects.requireNonNull(strategyEngine, "strategyEngine");
    }

    public Optional<ActivityReport> generateForFinishedMarker(ActivityMarker marker) throws IOException
    {
        final ActivityMarker normalizedMarker = Objects.requireNonNull(marker, "marker");
        if (!"FINISHED".equals(normalizedMarker.getMarkerType()))
        {
            return Optional.empty();
        }

        final ActivityId activityId = normalizedMarker.getActivityId();
        final ActivitySession session = findCompletedSession(activityId)
            .orElseThrow(() -> new IOException("Missing completed session for activity " + activityId));
        final ActivityReportData activityData = findCompletedActivityData(activityId)
            .orElseThrow(() -> new IOException("Missing completed activity data for activity " + activityId));
        final CompletedActivityTimeline timeline = timelineRepository.readActivityTimeline(activityId);

        final ActivityReport report = buildReport(session, activityData, timeline.getOpportunityMarkers());
        reportRepository.save(report);
        return Optional.of(report);
    }

    private ActivityReport buildReport(
        ActivitySession session,
        ActivityReportData activityData,
        List<OpportunityMarker> opportunityMarkers)
    {
        if (session.getActivityType() == ActivityType.GEM_MINING)
        {
            return new GemMiningAnalyzer().buildReport(session, activityData, opportunityMarkers);
        }
        if (session.getActivityType() == ActivityType.CONSTRUCTION)
        {
            return new ConstructionAnalyzer().buildReport(session, activityData, opportunityMarkers);
        }
        if (session.getActivityType() == ActivityType.VARDORVIS)
        {
            return new VardorvisAnalyzer().buildReport(session, activityData, opportunityMarkers);
        }
        if (session.getActivityType() == ActivityType.INFERNO)
        {
            return new InfernoAnalyzer().buildReport(session, activityData, opportunityMarkers);
        }
        throw new IllegalArgumentException("No report generator registered for " + session.getActivityType());
    }

    private Optional<ActivitySession> findCompletedSession(ActivityId activityId)
    {
        return findByActivityId(strategyEngine.getCompletedSessions(), activityId, ActivitySession::getActivityId);
    }

    private Optional<ActivityReportData> findCompletedActivityData(ActivityId activityId)
    {
        return findByActivityId(strategyEngine.getCompletedActivityData(), activityId, ActivityReportData::getActivityId);
    }

    private static <T> Optional<T> findByActivityId(
        List<T> values,
        ActivityId activityId,
        java.util.function.Function<T, ActivityId> activityIdExtractor)
    {
        final List<T> normalizedValues = values == null ? Collections.emptyList() : values;
        for (int i = normalizedValues.size() - 1; i >= 0; i--)
        {
            final T value = normalizedValues.get(i);
            if (activityId.equals(activityIdExtractor.apply(value)))
            {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
