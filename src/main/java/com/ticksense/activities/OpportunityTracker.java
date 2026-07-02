package com.ticksense.activities;

import com.ticksense.core.ActivityId;
import com.ticksense.core.EventTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public final class OpportunityTracker
{
    private static final OpportunitySink NO_OP_SINK = marker -> { };

    private final OpportunitySink sink;
    private final AtomicLong instanceSequence = new AtomicLong();
    private final AtomicLong markerSequence = new AtomicLong();
    private final Map<String, OpportunityInstance> instances = new LinkedHashMap<>();

    public OpportunityTracker()
    {
        this(NO_OP_SINK);
    }

    public OpportunityTracker(OpportunitySink sink)
    {
        this.sink = Objects.requireNonNull(sink, "sink");
    }

    public OpportunityInstance start(
        ActivityId activityId,
        OpportunityDefinition definition,
        EventTime startTime,
        Map<String, String> context)
    {
        final OpportunityInstance instance = new OpportunityInstance(
            "opportunity-" + instanceSequence.incrementAndGet(),
            Objects.requireNonNull(activityId, "activityId"),
            Objects.requireNonNull(definition, "definition"),
            Objects.requireNonNull(startTime, "startTime"),
            context);
        instances.put(instance.getInstanceId(), instance);
        emitMarker(instance, OpportunityStatus.OPEN, startTime);
        return instance;
    }

    public OpportunityInstance complete(String instanceId, EventTime endTime, List<OpportunityEvidence> evidence)
    {
        return transition(instanceId, OpportunityStatus.COMPLETED, endTime, evidence);
    }

    public OpportunityInstance fail(String instanceId, EventTime endTime, List<OpportunityEvidence> evidence)
    {
        return transition(instanceId, OpportunityStatus.FAILED, endTime, evidence);
    }

    public OpportunityInstance cancel(String instanceId, EventTime endTime, List<OpportunityEvidence> evidence)
    {
        return transition(instanceId, OpportunityStatus.CANCELLED, endTime, evidence);
    }

    public OpportunityInstance expire(String instanceId, EventTime endTime, List<OpportunityEvidence> evidence)
    {
        return transition(instanceId, OpportunityStatus.EXPIRED, endTime, evidence);
    }

    public List<OpportunityInstance> expireTimedOut(EventTime now)
    {
        final EventTime currentTime = Objects.requireNonNull(now, "now");
        final List<OpportunityInstance> expired = new ArrayList<>();
        for (OpportunityInstance instance : instances.values())
        {
            if (!instance.isOpen())
            {
                continue;
            }
            final long timeoutMillis = instance.getDefinition().getDefaultTimeoutMillis();
            if (timeoutMillis <= 0L)
            {
                continue;
            }
            if (currentTime.getWallTimeMillis() - instance.getStartTime().getWallTimeMillis() >= timeoutMillis)
            {
                if (instance.transition(OpportunityStatus.EXPIRED, currentTime, Collections.emptyList()))
                {
                    emitMarker(instance, OpportunityStatus.EXPIRED, currentTime);
                    expired.add(instance);
                }
            }
        }
        return Collections.unmodifiableList(expired);
    }

    public List<OpportunityInstance> cancelOpenOpportunities(
        ActivityId activityId,
        EventTime endTime,
        List<OpportunityEvidence> evidence)
    {
        final ActivityId normalizedActivityId = Objects.requireNonNull(activityId, "activityId");
        final EventTime normalizedEndTime = Objects.requireNonNull(endTime, "endTime");
        final List<OpportunityInstance> cancelled = new ArrayList<>();
        for (OpportunityInstance instance : instances.values())
        {
            if (instance.isOpen() && instance.getActivityId().equals(normalizedActivityId))
            {
                if (instance.transition(OpportunityStatus.CANCELLED, normalizedEndTime, evidence))
                {
                    emitMarker(instance, OpportunityStatus.CANCELLED, normalizedEndTime);
                    cancelled.add(instance);
                }
            }
        }
        return Collections.unmodifiableList(cancelled);
    }

    public OpportunityInstance get(String instanceId)
    {
        final OpportunityInstance instance = instances.get(Objects.requireNonNull(instanceId, "instanceId"));
        if (instance == null)
        {
            throw new IllegalArgumentException("Unknown opportunity instance: " + instanceId);
        }
        return instance;
    }

    private OpportunityInstance transition(
        String instanceId,
        OpportunityStatus status,
        EventTime endTime,
        List<OpportunityEvidence> evidence)
    {
        final OpportunityInstance instance = get(instanceId);
        if (instance.transition(status, Objects.requireNonNull(endTime, "endTime"), evidence))
        {
            emitMarker(instance, status, endTime);
        }
        return instance;
    }

    private void emitMarker(OpportunityInstance instance, OpportunityStatus status, EventTime time)
    {
        sink.accept(new OpportunityMarker(
            "marker-" + markerSequence.incrementAndGet(),
            instance.getInstanceId(),
            instance.getActivityId(),
            instance.getDefinition().getId(),
            status,
            time,
            instance.getContext(),
            instance.getEvidence()));
    }
}
