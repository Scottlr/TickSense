package com.ticksense.storage;

import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.core.ActivityId;
import com.ticksense.core.EventTime;
import com.ticksense.telemetry.TelemetryEnvelope;
import java.util.Objects;

public final class TimelineRecord
{
    private final TimelineRecordType type;
    private final TelemetryEnvelope telemetryEvent;
    private final ActivityMarker activityMarker;
    private final OpportunityMarker opportunityMarker;

    private TimelineRecord(
        TimelineRecordType type,
        TelemetryEnvelope telemetryEvent,
        ActivityMarker activityMarker,
        OpportunityMarker opportunityMarker)
    {
        this.type = Objects.requireNonNull(type, "type");
        this.telemetryEvent = telemetryEvent;
        this.activityMarker = activityMarker;
        this.opportunityMarker = opportunityMarker;
    }

    public static TimelineRecord telemetry(TelemetryEnvelope telemetryEvent)
    {
        return new TimelineRecord(TimelineRecordType.TELEMETRY_EVENT, Objects.requireNonNull(telemetryEvent, "telemetryEvent"), null, null);
    }

    public static TimelineRecord activityMarker(ActivityMarker activityMarker)
    {
        return new TimelineRecord(TimelineRecordType.ACTIVITY_MARKER, null, Objects.requireNonNull(activityMarker, "activityMarker"), null);
    }

    public static TimelineRecord opportunityMarker(OpportunityMarker opportunityMarker)
    {
        return new TimelineRecord(TimelineRecordType.OPPORTUNITY_MARKER, null, null, Objects.requireNonNull(opportunityMarker, "opportunityMarker"));
    }

    public TimelineRecordType getType()
    {
        return type;
    }

    public TelemetryEnvelope getTelemetryEvent()
    {
        return telemetryEvent;
    }

    public ActivityMarker getActivityMarker()
    {
        return activityMarker;
    }

    public OpportunityMarker getOpportunityMarker()
    {
        return opportunityMarker;
    }

    public EventTime getTime()
    {
        switch (type)
        {
            case TELEMETRY_EVENT:
                return telemetryEvent.getEvent().getTime();
            case ACTIVITY_MARKER:
                return activityMarker.getTime();
            case OPPORTUNITY_MARKER:
                return opportunityMarker.getTime();
            default:
                throw new IllegalStateException("Unsupported timeline record type: " + type);
        }
    }

    public ActivityId getActivityId()
    {
        switch (type)
        {
            case ACTIVITY_MARKER:
                return activityMarker.getActivityId();
            case OPPORTUNITY_MARKER:
                return opportunityMarker.getActivityId();
            default:
                return null;
        }
    }
}
