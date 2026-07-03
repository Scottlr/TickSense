package com.ticksense.activities;

import com.ticksense.core.ActivitySession;
import com.ticksense.core.FinishReason;
import com.ticksense.telemetry.TelemetryEvent;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public final class DisabledActivityStrategy implements ActivityStrategy
{
    private final ActivityDefinition definition;

    public DisabledActivityStrategy(ActivityDefinition definition)
    {
        this.definition = Objects.requireNonNull(definition, "definition");
    }

    @Override
    public ActivityDefinition getDefinition()
    {
        return definition;
    }

    @Override
    public ActivityCandidate evaluateActivation(ActivityContext context, TelemetryEvent event)
    {
        return null;
    }

    @Override
    public void onStart(ActivityContext context, ActivitySession session)
    {
    }

    @Override
    public void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event, OpportunitySink sink)
    {
    }

    @Override
    public Optional<FinishReason> evaluateTermination(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        return Optional.empty();
    }

    @Override
    public ActivityReportData buildActivityData(ActivityContext context, ActivitySession session)
    {
        return new ActivityReportData(session.getActivityId(), session.getActivityType(), Collections.emptyMap());
    }
}
