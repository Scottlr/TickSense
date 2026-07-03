package com.ticksense.activities;

import com.ticksense.common.ImmutableCollections;

import com.ticksense.core.ActivityId;
import com.ticksense.core.EventTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class OpportunityInstance
{
    private static final long OPEN_LATENCY_SENTINEL = -1L;

    private final String instanceId;
    private final ActivityId activityId;
    private final OpportunityDefinition definition;
    private final EventTime startTime;
    private final Map<String, String> context;
    private final List<OpportunityEvidence> evidence = new ArrayList<>();
    private final List<OpportunityEvidence> evidenceView = java.util.Collections.unmodifiableList(evidence);

    private EventTime endTime;
    private OpportunityStatus status = OpportunityStatus.OPEN;

    public OpportunityInstance(
        String instanceId,
        ActivityId activityId,
        OpportunityDefinition definition,
        EventTime startTime,
        Map<String, String> context)
    {
        this.instanceId = ActivityTexts.requireText(instanceId, "instanceId");
        this.activityId = Objects.requireNonNull(activityId, "activityId");
        this.definition = Objects.requireNonNull(definition, "definition");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.context = ImmutableCollections.immutableMap(context);
    }

    public String getInstanceId()
    {
        return instanceId;
    }

    public ActivityId getActivityId()
    {
        return activityId;
    }

    public OpportunityDefinition getDefinition()
    {
        return definition;
    }

    public EventTime getStartTime()
    {
        return startTime;
    }

    public Map<String, String> getContext()
    {
        return context;
    }

    public List<OpportunityEvidence> getEvidence()
    {
        return evidenceView;
    }

    public EventTime getEndTime()
    {
        return endTime;
    }

    public OpportunityStatus getStatus()
    {
        return status;
    }

    public long latencyMillis()
    {
        return endTime == null ? OPEN_LATENCY_SENTINEL : endTime.getWallTimeMillis() - startTime.getWallTimeMillis();
    }

    public int latencyTicks()
    {
        return endTime == null ? (int) OPEN_LATENCY_SENTINEL : endTime.getGameTick() - startTime.getGameTick();
    }

    boolean isOpen()
    {
        return status == OpportunityStatus.OPEN;
    }

    boolean transition(OpportunityStatus nextStatus, EventTime nextEndTime, List<OpportunityEvidence> newEvidence)
    {
        Objects.requireNonNull(nextStatus, "nextStatus");
        Objects.requireNonNull(nextEndTime, "nextEndTime");
        if (nextStatus == OpportunityStatus.OPEN)
        {
            throw new IllegalArgumentException("nextStatus must be terminal");
        }
        if (!isOpen())
        {
            return false;
        }

        if (newEvidence != null && !newEvidence.isEmpty())
        {
            evidence.addAll(newEvidence);
        }
        endTime = nextEndTime;
        status = nextStatus;
        return true;
    }
}
