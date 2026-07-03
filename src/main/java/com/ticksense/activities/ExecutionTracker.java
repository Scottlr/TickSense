package com.ticksense.activities;

import com.ticksense.core.ActivityId;
import com.ticksense.core.EventTime;

public interface ExecutionTracker
{
    String id();

    void startActivity(ActivityId activityId);

    void ensureOpportunityLifecycle(OpportunityLifecycle opportunityLifecycle);

    void expireTimedOut(EventTime time);

    void cancelOpenOpportunities(EventTime time, String detail);

    void reset();
}
