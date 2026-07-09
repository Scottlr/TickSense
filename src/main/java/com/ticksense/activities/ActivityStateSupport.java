package com.ticksense.activities;

import com.ticksense.core.ActivityId;
import com.ticksense.core.EventTime;
import java.util.Map;

public interface ActivityStateSupport
{
    void startActivity(ActivityId activityId);

    void ensureOpportunityLifecycle(OpportunityLifecycle opportunityLifecycle);

    void cancelOpenOpportunities(EventTime time, String detail);

    Map<String, String> snapshotAttributes();

    void resetForNextSession();
}
