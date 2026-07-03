package com.ticksense.activities.execution;

import com.ticksense.activities.EvidenceStrength;
import com.ticksense.activities.OpportunityDefinition;
import com.ticksense.activities.OpportunityEvidence;
import com.ticksense.activities.OpportunityInstance;
import com.ticksense.activities.OpportunityLifecycle;
import com.ticksense.activities.OpportunityStatus;
import com.ticksense.common.TextValues;
import com.ticksense.core.ActivityId;
import com.ticksense.core.ActivityType;
import com.ticksense.core.EventTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractExecutionTracker implements ExecutionTracker
{
    private final String id;
    private ActivityId activeActivityId;
    private OpportunityLifecycle opportunityLifecycle;

    protected AbstractExecutionTracker(String id)
    {
        this.id = TextValues.requireText(id, "id");
    }

    @Override
    public final String id()
    {
        return id;
    }

    @Override
    public void startActivity(ActivityId activityId)
    {
        activeActivityId = activityId;
    }

    @Override
    public void ensureOpportunityLifecycle(OpportunityLifecycle nextLifecycle)
    {
        if (opportunityLifecycle == null)
        {
            opportunityLifecycle = nextLifecycle;
        }
    }

    @Override
    public void expireTimedOut(EventTime time)
    {
        if (opportunityLifecycle != null)
        {
            opportunityLifecycle.expireTimedOut(time);
        }
    }

    @Override
    public void cancelOpenOpportunities(EventTime time, String detail)
    {
        if (ready())
        {
            opportunityLifecycle.cancelOpenOpportunities(activeActivityId, time, evidence(time, "activity.finish", detail));
        }
    }

    @Override
    public void reset()
    {
        activeActivityId = null;
        opportunityLifecycle = null;
    }

    protected final boolean ready()
    {
        return activeActivityId != null && opportunityLifecycle != null;
    }

    protected final OpportunityInstance startOpportunity(
        OpportunityDefinition definition,
        EventTime startTime,
        Map<String, String> context)
    {
        if (!ready())
        {
            return null;
        }
        return opportunityLifecycle.start(activeActivityId, definition, startTime, context);
    }

    protected final void complete(OpportunityInstance instance, EventTime time, String sourceEventType, String detail)
    {
        if (ready() && isOpen(instance))
        {
            opportunityLifecycle.complete(instance.getInstanceId(), time, evidence(time, sourceEventType, detail));
        }
    }

    protected final void fail(OpportunityInstance instance, EventTime time, String sourceEventType, String detail)
    {
        if (ready() && isOpen(instance))
        {
            opportunityLifecycle.fail(instance.getInstanceId(), time, evidence(time, sourceEventType, detail));
        }
    }

    protected final OpportunityDefinition definition(
        String idSuffix,
        String displayName,
        ActivityType activityType,
        long timeoutMillis,
        String... expectedResponses)
    {
        return new OpportunityDefinition(
            id + "." + TextValues.requireText(idSuffix, "idSuffix"),
            displayName,
            activityType,
            timeoutMillis,
            Arrays.asList(expectedResponses));
    }

    protected static Map<String, String> context(String... pairs)
    {
        final Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pairs.length; i += 2)
        {
            values.put(pairs[i], pairs[i + 1]);
        }
        return values;
    }

    protected static List<OpportunityEvidence> evidence(EventTime time, String sourceEventType, String detail)
    {
        return Collections.singletonList(new OpportunityEvidence(time, sourceEventType, EvidenceStrength.CONFIRMING, detail));
    }

    protected static boolean isOpen(OpportunityInstance instance)
    {
        return instance != null && instance.getStatus() == OpportunityStatus.OPEN;
    }
}
