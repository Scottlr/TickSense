package com.ticksense.storage;

import com.ticksense.activities.ActivityMarker;
import com.ticksense.activities.OpportunityMarker;
import com.ticksense.core.ActivityId;
import com.ticksense.telemetry.TelemetryEnvelope;
import java.io.IOException;
import java.util.List;

public interface TimelineRepository extends AutoCloseable
{
    void append(TelemetryEnvelope event) throws IOException;

    void appendActivityMarker(ActivityMarker marker) throws IOException;

    void appendOpportunityMarker(OpportunityMarker marker) throws IOException;

    CompletedActivityTimeline readActivityTimeline(ActivityId activityId) throws IOException;

    List<TimelineRecord> readActivityRecords(ActivityId activityId) throws IOException;

    @Override
    void close() throws IOException;
}
