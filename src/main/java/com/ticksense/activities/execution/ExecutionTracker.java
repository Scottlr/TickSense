package com.ticksense.activities.execution;

import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.OpportunityLifecycle;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.EventTime;
import com.ticksense.telemetry.TelemetryEvent;

public interface ExecutionTracker
{
    String id();

    default boolean supports(ActivityContext context, ActivitySession session)
    {
        return true;
    }

    void startActivity(ActivityId activityId);

    void ensureOpportunityLifecycle(OpportunityLifecycle opportunityLifecycle);

    default void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
    }

    void expireTimedOut(EventTime time);

    void cancelOpenOpportunities(EventTime time, String detail);

    void reset();
}
