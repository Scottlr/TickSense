package com.ticksense.analytics;

import com.ticksense.activities.ActivityReportData;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.core.ActivitySession;
import java.util.List;

public interface ReportBuilder
{
    ActivityReport build(
        ActivitySession session,
        ActivityReportData activityData,
        List<OpportunityMarker> opportunityMarkers);
}
