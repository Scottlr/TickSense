package com.ticksense.activities;

import com.ticksense.core.ActivityId;
import com.ticksense.core.EventTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class OpportunityMarker
{
    private final String markerId;
    private final String opportunityInstanceId;
    private final ActivityId activityId;
    private final String opportunityType;
    private final OpportunityStatus status;
    private final EventTime time;
    private final Map<String, String> context;
    private final List<OpportunityEvidence> evidence;

    public OpportunityMarker(
        String markerId,
        String opportunityInstanceId,
        ActivityId activityId,
        String opportunityType,
        OpportunityStatus status,
        EventTime time,
        Map<String, String> context,
        List<OpportunityEvidence> evidence)
    {
        this.markerId = requireText(markerId, "markerId");
        this.opportunityInstanceId = requireText(opportunityInstanceId, "opportunityInstanceId");
        this.activityId = Objects.requireNonNull(activityId, "activityId");
        this.opportunityType = requireText(opportunityType, "opportunityType");
        this.status = Objects.requireNonNull(status, "status");
        this.time = Objects.requireNonNull(time, "time");
        this.context = immutableMap(context);
        this.evidence = immutableList(evidence);
    }

    public String getMarkerId()
    {
        return markerId;
    }

    public String getOpportunityInstanceId()
    {
        return opportunityInstanceId;
    }

    public ActivityId getActivityId()
    {
        return activityId;
    }

    public String getOpportunityType()
    {
        return opportunityType;
    }

    public OpportunityStatus getStatus()
    {
        return status;
    }

    public EventTime getTime()
    {
        return time;
    }

    public Map<String, String> getContext()
    {
        return context;
    }

    public List<OpportunityEvidence> getEvidence()
    {
        return evidence;
    }

    private static String requireText(String value, String fieldName)
    {
        final String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static Map<String, String> immutableMap(Map<String, String> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    private static List<OpportunityEvidence> immutableList(List<OpportunityEvidence> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }
}
