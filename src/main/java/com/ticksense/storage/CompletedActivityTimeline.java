package com.ticksense.storage;

import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.core.ActivityId;
import com.ticksense.telemetry.TelemetryEnvelope;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CompletedActivityTimeline
{
    private final ActivityId activityId;
    private final List<TelemetryEnvelope> telemetryEvents;
    private final List<ActivityMarker> activityMarkers;
    private final List<OpportunityMarker> opportunityMarkers;

    public CompletedActivityTimeline(
        ActivityId activityId,
        List<TelemetryEnvelope> telemetryEvents,
        List<ActivityMarker> activityMarkers,
        List<OpportunityMarker> opportunityMarkers)
    {
        this.activityId = Objects.requireNonNull(activityId, "activityId");
        this.telemetryEvents = immutableList(telemetryEvents);
        this.activityMarkers = immutableList(activityMarkers);
        this.opportunityMarkers = immutableList(opportunityMarkers);
    }

    public ActivityId getActivityId()
    {
        return activityId;
    }

    public List<TelemetryEnvelope> getTelemetryEvents()
    {
        return telemetryEvents;
    }

    public List<ActivityMarker> getActivityMarkers()
    {
        return activityMarkers;
    }

    public List<OpportunityMarker> getOpportunityMarkers()
    {
        return opportunityMarkers;
    }

    private static <T> List<T> immutableList(List<T> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }
}
