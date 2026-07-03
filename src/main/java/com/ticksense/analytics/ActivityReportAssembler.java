package com.ticksense.analytics;

import com.ticksense.activities.ActivityReportData;
import com.ticksense.core.ActivitySession;
import java.util.Objects;

public final class ActivityReportAssembler
{
    private ActivityReportAssembler()
    {
    }

    public static ActivityReport assemble(
        ActivitySession session,
        ActivityReportData activityData,
        String fallbackDisplayName,
        ActivityAnalysisData analysisData)
    {
        final ActivitySession normalizedSession = Objects.requireNonNull(session, "session");
        Objects.requireNonNull(activityData, "activityData");
        final ActivityAnalysisData normalizedAnalysisData = Objects.requireNonNull(analysisData, "analysisData");

        return new ActivityReport(
            ActivityReport.SCHEMA_VERSION,
            normalizedSession.getActivityId() + "-report",
            normalizedSession.getActivityId(),
            normalizedSession.getActivityType(),
            ReportMetadata.displayName(normalizedSession, fallbackDisplayName),
            normalizedSession.getEndTime().getWallTimeMillis(),
            normalizedSession.getEndTime().getGameTick() - normalizedSession.getStartTime().getGameTick(),
            normalizedSession.getEndTime().getWallTimeMillis() - normalizedSession.getStartTime().getWallTimeMillis(),
            normalizedSession.getFinishReason(),
            ReportMetadata.confidence(normalizedSession),
            normalizedAnalysisData.getEvidenceSummary(),
            normalizedAnalysisData.getMetrics(),
            normalizedAnalysisData.getOpportunityTimeline(),
            normalizedAnalysisData.getTickLossBreakdown(),
            normalizedAnalysisData.getSummaryLines());
    }
}
