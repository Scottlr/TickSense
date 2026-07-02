package com.ticksense.activities;

import com.ticksense.core.ActivitySession;
import com.ticksense.core.FinishReason;
import com.ticksense.telemetry.TelemetryEvent;
import java.util.Optional;

public interface ActivityStrategy
{
    ActivityDefinition getDefinition();

    ActivityCandidate evaluateActivation(ActivityContext context, TelemetryEvent event);

    void onStart(ActivityContext context, ActivitySession session);

    void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event, OpportunitySink sink);

    Optional<FinishReason> evaluateTermination(ActivityContext context, ActivitySession session, TelemetryEvent event);

    ActivityReportData buildActivityData(ActivityContext context, ActivitySession session);
}
