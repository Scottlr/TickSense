package com.ticksense.storage;

import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.core.ActivityId;
import com.ticksense.common.ImmutableCollections;
import com.ticksense.telemetry.TelemetryEnvelope;
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
        this.telemetryEvents = ImmutableCollections.immutableList(telemetryEvents);
        this.activityMarkers = ImmutableCollections.immutableList(activityMarkers);
        this.opportunityMarkers = ImmutableCollections.immutableList(opportunityMarkers);
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
}
