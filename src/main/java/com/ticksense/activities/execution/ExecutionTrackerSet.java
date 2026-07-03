package com.ticksense.activities.execution;

import com.ticksense.activities.ActivityContext;
import com.ticksense.activities.OpportunityLifecycle;
import com.ticksense.common.ImmutableCollections;
import com.ticksense.common.TextValues;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivitySession;
import com.ticksense.core.EventTime;
import com.ticksense.telemetry.TelemetryEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class ExecutionTrackerSet implements ExecutionTracker
{
    private final String id;
    private final List<ExecutionTracker> trackers;

    public ExecutionTrackerSet(String id, Collection<? extends ExecutionTracker> trackers)
    {
        this.id = TextValues.requireText(id, "id");
        this.trackers = ImmutableCollections.immutableList(trackers);
    }

    public static ExecutionTrackerSet of(String id, ExecutionTracker... trackers)
    {
        return new ExecutionTrackerSet(id, Arrays.asList(trackers));
    }

    @Override
    public String id()
    {
        return id;
    }

    @Override
    public boolean supports(ActivityContext context, ActivitySession session)
    {
        for (ExecutionTracker tracker : trackers)
        {
            if (tracker.supports(context, session))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void startActivity(ActivityId activityId)
    {
        for (ExecutionTracker tracker : trackers)
        {
            tracker.startActivity(activityId);
        }
    }

    @Override
    public void ensureOpportunityLifecycle(OpportunityLifecycle opportunityLifecycle)
    {
        for (ExecutionTracker tracker : trackers)
        {
            tracker.ensureOpportunityLifecycle(opportunityLifecycle);
        }
    }

    @Override
    public void onEvent(ActivityContext context, ActivitySession session, TelemetryEvent event)
    {
        for (ExecutionTracker tracker : trackers)
        {
            if (tracker.supports(context, session))
            {
                tracker.onEvent(context, session, event);
            }
        }
    }

    @Override
    public void expireTimedOut(EventTime time)
    {
        for (ExecutionTracker tracker : trackers)
        {
            tracker.expireTimedOut(time);
        }
    }

    @Override
    public void cancelOpenOpportunities(EventTime time, String detail)
    {
        for (ExecutionTracker tracker : trackers)
        {
            tracker.cancelOpenOpportunities(time, detail);
        }
    }

    @Override
    public void reset()
    {
        for (ExecutionTracker tracker : trackers)
        {
            tracker.reset();
        }
    }

    public List<ExecutionTracker> trackers()
    {
        return trackers;
    }
}
