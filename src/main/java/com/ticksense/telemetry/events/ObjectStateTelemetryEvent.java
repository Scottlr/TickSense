package com.ticksense.telemetry.events;

import com.ticksense.core.EventTime;
import com.ticksense.core.WorldLocation;
import com.ticksense.telemetry.AbstractTelemetryEvent;
import com.ticksense.telemetry.TelemetryCategory;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ObjectStateTelemetryEvent extends AbstractTelemetryEvent
{
    public static final String TYPE = "object.state";

    private final int objectId;
    private final String objectName;
    private final WorldLocation location;
    private final String objectType;
    private final List<String> actions;
    private final String stateChange;

    public ObjectStateTelemetryEvent(
        EventTime time,
        Map<String, String> tags,
        int objectId,
        String objectName,
        WorldLocation location,
        String objectType,
        List<String> actions,
        String stateChange)
    {
        super(TYPE, TelemetryCategory.OBJECT_STATE, time, tags);
        this.objectId = objectId;
        this.objectName = safeText(objectName);
        this.location = Objects.requireNonNull(location, "location");
        this.objectType = safeText(objectType);
        this.actions = immutableList(actions);
        this.stateChange = safeText(stateChange);
    }

    public int getObjectId()
    {
        return objectId;
    }

    public String getObjectName()
    {
        return objectName;
    }

    public WorldLocation getLocation()
    {
        return location;
    }

    public String getObjectType()
    {
        return objectType;
    }

    public List<String> getActions()
    {
        return actions;
    }

    public String getStateChange()
    {
        return stateChange;
    }
}
